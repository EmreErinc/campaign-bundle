package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.finartz.intern.campaignlogic.security.Errors.*;

@Slf4j
@Service
public class BaseServiceImpl implements BaseService {
  private AccountRepository accountRepository;
  private SellerRepository sellerRepository;
  private CampaignRepository campaignRepository;
  private ItemRepository itemRepository;
  private SalesRepository salesRepository;
  private CartRepository cartRepository;

  @Autowired
  public BaseServiceImpl(AccountRepository accountRepository,
                         SellerRepository sellerRepository,
                         CampaignRepository campaignRepository,
                         ItemRepository itemRepository,
                         SalesRepository salesRepository,
                         CartRepository cartRepository) {
    this.accountRepository = accountRepository;
    this.sellerRepository = sellerRepository;
    this.campaignRepository = campaignRepository;
    this.itemRepository = itemRepository;
    this.salesRepository = salesRepository;
    this.cartRepository = cartRepository;
  }

  @Override
  public Role getRoleByAccountId(int accountId) {
    Optional<AccountEntity> optionalAccountEntity = accountRepository.findById(accountId);
    if (!optionalAccountEntity.isPresent()){
      throw new ApplicationContextException(ACCOUNT_NOT_FOUND);
    }
    return optionalAccountEntity.get().getRole();
  }

  @Override
  public Integer getSellerIdByAccountId(int accountId) {
    Optional<SellerEntity> optionalSellerEntity = sellerRepository.findByAccountId(accountId);
    if (!optionalSellerEntity.isPresent()){
      throw new ApplicationContextException(SELLER_NOT_FOUND);
    }

    return optionalSellerEntity.get().getId();
  }

  @Override
  public Optional<CampaignEntity> getCampaignByItemId(int itemId) {
    return campaignRepository.findByItemId(itemId);
  }

  @Override
  public Integer getSellerIdByItemId(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()){
      throw new ApplicationContextException(CAMPAIGN_NOT_FOUND);
    }
    return optionalItemEntity.get().getSellerId();
  }

  @Override
  public Boolean campaignIsAvailableGetByItemId(int itemId) {
    Optional<CampaignEntity> campaignEntity = campaignRepository.findByItemId(itemId);
    Long current = Instant.now().toEpochMilli();
    if (!campaignEntity.isPresent()) {
      //throw new ApplicationContextException(CAMPAIGN_NOT_FOUND);
      log.info("Ürüne ait kampanya bulunamadı.");
      return true;
    }
    return (current > campaignEntity.get().getStartAt() && current < campaignEntity.get().getEndAt());
  }

  @Override
  public Boolean campaignIsAvailableGetById(int campaignId) {
    return campaignRepository
        .findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(campaignId,
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli())
        .isPresent();
  }

  @Override
  public CampaignEntity getCampaignEntity(int campaignId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findById(campaignId);
    if (!optionalCampaignEntity.isPresent()){
      throw new ApplicationContextException(CAMPAIGN_NOT_FOUND);
    }
    return optionalCampaignEntity.get();
  }

  @Override
  public Boolean stockIsAvailable(int itemId, int expectedSaleAndGiftCount) {
    Integer stock = getItemStock(itemId);
    return (stock - expectedSaleAndGiftCount >= 0);
  }

  @Override
  public Boolean itemOnCampaign(int itemId) {
    return campaignRepository
        .findByItemId(itemId)
        .isPresent();
  }

  @Override
  public ItemEntity getItemEntity(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()){
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return optionalItemEntity.get();
  }

  @Override
  public Integer getItemStock(int itemId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByItemId(itemId);
    int sumOfSales = 0;
    int sumOfGifts = 0;

    //if no item sales
    if (optionalSalesEntities.isPresent()) {
      sumOfSales = optionalSalesEntities
          .get()
          .stream()
          .filter(salesEntity -> salesEntity.getSaleCount() != null)
          .collect(Collectors.toList())
          .stream()
          .mapToInt(SalesEntity::getSaleCount).sum();
      sumOfGifts = optionalSalesEntities
          .get()
          .stream()
          .filter(salesEntity -> salesEntity.getGiftCount() != null)
          .collect(Collectors.toList())
          .stream()
          .mapToInt(SalesEntity::getGiftCount).sum();
    }
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()){
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return optionalItemEntity.get().getStock() - (sumOfSales + sumOfGifts);
  }

  @Override
  public Boolean campaignLimitIsAvailableForAccount(int accountId, int itemId) {
    if (!campaignRepository.existsByItemId(itemId)) {
      return true;
    }

    Optional<Integer> campaignItemUsageCount = getCampaignItemUsageCount(accountId, itemId);
    if (!campaignItemUsageCount.isPresent()){
      return true;
      //throw new ApplicationContextException(CAMPAIGN_BADGE_NOT_FOUND);
    }

    return campaignItemUsageCount.get() < getCampaignLimit(itemId);
  }

  @Override
  public Optional<Integer> getCampaignItemUsageCount(int accountId, int itemId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerIdAndItemId(accountId, itemId);
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);
    if (!optionalCampaignEntity.isPresent()){
      throw new ApplicationContextException(CAMPAIGN_NOT_FOUND);
    }

    AtomicInteger sumOfSales = new AtomicInteger();
    optionalSalesEntities.ifPresent(salesEntities ->
        sumOfSales.set(salesEntities
            .stream()
            .filter(salesEntity -> salesEntity.getSaleCount() != null)
            .collect(Collectors.toList())
            .stream()
            .mapToInt(SalesEntity::getSaleCount).sum())
    );

    int cartLimit = optionalCampaignEntity.get().getCartLimit();
    if (cartLimit == 0 || sumOfSales.intValue() == 0) {
      return Optional.empty();
    }
    return Optional.of(sumOfSales.get() / cartLimit);
  }

  @Override
  public Optional<Integer> getCampaignUsageCount(int accountId, int campaignId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerId(accountId);
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findById(campaignId);

    return optionalCampaignEntity.map(campaignEntity ->
        optionalSalesEntities.map(salesEntities ->
            salesEntities
                .stream()
                .filter(salesEntity -> salesEntity.getItemId().equals(campaignEntity.getItemId()))
                .collect(Collectors.toList())
                .size()
        )
    ).orElse(Optional.of(0));
  }

  @Override
  public Integer getCampaignCartLimit(int itemId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);
    if (!optionalCampaignEntity.isPresent()) {
      return 0;
    }
    return optionalCampaignEntity.get().getCartLimit();
  }

  @Override
  public Integer getCampaignLimit(int itemId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);
    if (!optionalCampaignEntity.isPresent()) {
      return 0;
    }
    return optionalCampaignEntity.get().getCampaignLimit();
  }

  @Override
  public CartEntity getCartEntityById(String cartId) {
    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (!optionalCartEntity.isPresent()) {
      throw new ApplicationContextException(CART_NOT_FOUND);
    }
    return optionalCartEntity.get();
  }

  @Override
  public void saveAsSoldCart(CartEntity cartEntity) {
    cartRepository
        .saveAsSold(SoldCartEntity.builder()
            .relatedCartId(cartEntity.getId())
            .accountId(cartEntity.getAccountId())
            .cartItems(cartEntity.getCartItems())
            .build());

    cartRepository
        .updateCart(CartEntity.builder()
            .id(cartEntity.getId())
            .accountId(cartEntity.getAccountId())
            .cartItems(new ArrayList<>())
            .build());
  }

  @Override
  public Double getItemPrice(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()) {
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return optionalItemEntity.get().getPrice();
  }

  @Override
  public Badge getBadgeByItemId(int itemId) {
    return extractCampaignEntityBadge(campaignRepository.findByItemId(itemId));
  }

  @Override
  public Badge getBadgeByCampaignId(int campaignId) {
    return extractCampaignEntityBadge(campaignRepository.findById(campaignId));
  }

  private Badge extractCampaignEntityBadge(Optional<CampaignEntity> optionalCampaignEntity) {
    return optionalCampaignEntity
        .map(campaignEntity ->
            Badge.builder()
                .requirement(campaignEntity.getRequirementCount())
                .gift(campaignEntity.getExpectedGiftCount())
                .build())
        .orElseGet(() ->
            Badge.builder()
                .build());
  }

  @Override
  public List<CampaignEntity> getUsedCampaignsByUserId(int userId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerId(userId);
    List<CampaignEntity> campaignEntities = new ArrayList<>();

    optionalSalesEntities.ifPresent(salesEntities ->
        salesEntities
            .forEach(salesEntity ->
                getCampaignByItemId(salesEntity.getItemId())
                    .ifPresent(campaignEntities::add)));

    return campaignEntities;
  }

  @Override
  public Boolean userAvailableForCampaign(int accountId, int campaignId) {
    return getCampaignUsageCount(accountId, campaignId)
        .map(campaignUsageCount ->
            campaignRepository.findById(campaignId).get().getCampaignLimit() <= campaignUsageCount)
        .orElse(false);
  }

  @Override
  public CampaignSummary prepareCampaignEntityToList(int accountId, CampaignEntity campaignEntity) {
    Badge badge = Badge.builder().build();
    if (!userAvailableForCampaign(accountId, campaignEntity.getId())) {
      badge = getBadgeByCampaignId(campaignEntity.getId());
    }
    return Converters.campaignEntityToCampaignSummary(campaignEntity, badge);
  }

  @Override
  public Boolean itemOnCart(String cartId, int itemId) {
    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (!optionalCartEntity.isPresent()){
      throw new ApplicationContextException(CART_NOT_FOUND);
    }

    return optionalCartEntity
        .get()
        .getCartItems()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemId));
  }

  @Override
  public Integer getItemCountOnCart(String cartId, int itemId) {
    return getCartEntityById(cartId)
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
        .findFirst()
        .map(CartItem::getSaleCount).orElse(0);
  }
}
package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.entity.SalesEntity;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

  public Role getRoleByAccountId(int accountId) {
    return accountRepository.findById(accountId).get().getRole();
  }

  public Optional<Integer> getSellerIdByAccountId(int accountId) {
    return Optional.of(sellerRepository.findByAccountId(accountId).get().getId());
  }

  @Override
  public Optional<CampaignEntity> getCampaignByItemId(int itemId) {
    return campaignRepository.findByItemId(itemId);
  }

  @Override
  public Optional<Integer> getSellerIdByItemId(int itemId) {
    return Optional.of(itemRepository.findById(itemId).get().getSellerId());
  }

  @Override
  public boolean campaignIsAvailable(int itemId) {
    Optional<CampaignEntity> campaignEntity = campaignRepository.findByItemId(itemId);
    Long current = Instant.now().toEpochMilli();
    if (!campaignEntity.isPresent()) {
      throw new ApplicationContextException("Kampanya Bulunamadı.");
    }

    return (current > campaignEntity.get().getStartAt() && current < campaignEntity.get().getEndAt());
  }

  @Override
  public boolean stockIsAvailable(int itemId, int expectedSaleAndGiftCount) {
    Integer stock = itemRepository.findById(itemId).get().getStock();
    return (stock - expectedSaleAndGiftCount >= 0);
  }

  @Override
  public Integer getItemStock(int itemId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByItemId(itemId);
    if (!optionalSalesEntities.isPresent()) {
      throw new ApplicationContextException("Ürün Bulunamadı.");
    }
    int sumOfSales = optionalSalesEntities
        .get()
        .stream()
        .filter(salesEntity -> salesEntity.getSaleCount() != null)
        .collect(Collectors.toList())
        .stream()
        .mapToInt(SalesEntity::getSaleCount).sum();
    int sumOfGifts = optionalSalesEntities
        .get()
        .stream()
        .filter(salesEntity -> salesEntity.getGiftCount() != null)
        .collect(Collectors.toList())
        .stream()
        .mapToInt(SalesEntity::getGiftCount).sum();
    int stock = itemRepository.findById(itemId).get().getStock();

    return stock - (sumOfSales + sumOfGifts);
  }

  @Override
  public Optional<Boolean> campaignLimitIsAvailableForAccount(int accountId, int itemId) {
    if (!campaignRepository.existsByItemId(itemId).get()) {
      return Optional.of(true);
    }

    return Optional.of(getCampaignItemUsageCount(accountId, itemId).get() < getCampaignLimit(itemId));
  }

  @Override
  public Optional<Integer> getCampaignItemUsageCount(int accountId, int itemId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerIdAndItemId(accountId, itemId);
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);

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
  public CartEntity getCartById(String cartId) {
    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (!optionalCartEntity.isPresent()) {
      throw new ApplicationContextException("Sepet Bulunamadı.");
    }
    return optionalCartEntity.get();
  }

  @Override
  public Double getItemPrice(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()) {
      throw new ApplicationContextException("Ürün Bulunamadı.");
    }
    return optionalItemEntity.get().getPrice();
  }

  @Override
  public Optional<Badge> getBadgeByItemId(int itemId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);

    return optionalCampaignEntity
        .map(campaignEntity ->
            Optional.of(Badge.builder()
                .requirement(campaignEntity.getRequirementCount())
                .gift(campaignEntity.getExpectedGiftCount())
                .build()))
        .orElseGet(() ->
            Optional.of(Badge.builder().build()));
  }

  @Override
  public Optional<Badge> getBadgeByCampaignId(int campaignId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findById(campaignId);

    return optionalCampaignEntity
        .map(campaignEntity ->
            Optional.of(Badge.builder()
                .requirement(campaignEntity.getRequirementCount())
                .gift(campaignEntity.getExpectedGiftCount())
                .build()))
        .orElseGet(() ->
            Optional.of(Badge.builder().build()));
  }

  @Override
  public List<CampaignEntity> getUsedCampaignsByUserId(int userId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerId(userId);
    //if (!optionalSalesEntities.isPresent()) {
    //  throw new ApplicationContextException("Satış Bulunamadı.");
    //}
    List<CampaignEntity> campaignEntities = new ArrayList<>();

    optionalSalesEntities.ifPresent(salesEntities ->
        salesEntities
            .forEach(salesEntity ->
                getCampaignByItemId(salesEntity.getItemId())
                    .map(campaignEntities::add)
                    .orElse(false)));

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
      badge = getBadgeByCampaignId(campaignEntity.getId()).get();
    }
    return Converters.campaignEntityToCampaignSummary(campaignEntity, badge);
  }
}
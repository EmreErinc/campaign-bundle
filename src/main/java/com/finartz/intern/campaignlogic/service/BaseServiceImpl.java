package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.entity.SalesEntity;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    if (!campaignEntity.isPresent()){
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
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.getByItemId(itemId);
    if (!optionalSalesEntities.isPresent()){
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
  public Optional<Boolean> campaignLimitIsAvailable(int accountId, int itemId){
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerIdAndItemId(accountId, itemId);
    if (!optionalSalesEntities.isPresent()){
      return Optional.of(false);
    }
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);
    if (!optionalCampaignEntity.isPresent()){
      throw new ApplicationContextException("Kampanya Bulunamadı.");
    }
    int sumOfSales = optionalSalesEntities
        .get()
        .stream()
        .filter(salesEntity -> salesEntity.getSaleCount() != null)
        .collect(Collectors.toList())
        .stream()
        .mapToInt(SalesEntity::getSaleCount).sum();

    int campaignLimit = optionalCampaignEntity.get().getCampaignLimit();
    int cartLimit = optionalCampaignEntity.get().getCartLimit();
    return Optional.of(sumOfSales / cartLimit <= campaignLimit);
  }

  @Override
  public Integer getCampaignUsageLimit(int accountId, int itemId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerIdAndItemId(accountId, itemId);
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);

    int campaignLimit = optionalCampaignEntity.get().getCampaignLimit();
    int cartLimit = optionalCampaignEntity.get().getCartLimit();

    return optionalSalesEntities.map(List::size).orElse(0);
  }

  @Override
  public Integer getCampaignCartLimit(int itemId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);
    if (!optionalCampaignEntity.isPresent()){
      return 0;
    }
    return optionalCampaignEntity.get().getCartLimit();
  }

  @Override
  public CartEntity getCartById(String cartId) {
    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (!optionalCartEntity.isPresent()){
      throw new ApplicationContextException("Sepet Bulunamadı.");
    }
    return optionalCartEntity.get();
  }

  @Override
  public Double getItemPrice(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()){
      throw new ApplicationContextException("Ürün Bulunamadı.");
    }
    return optionalItemEntity.get().getPrice();
  }

  @Override
  public Optional<Badge> getBadgeByItemId(int itemId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByItemId(itemId);

    return Optional.of(Badge.builder()
        .requirement(optionalCampaignEntity.get().getRequirementCount())
        .gift(optionalCampaignEntity.get().getExpectedGiftCount())
        .build());
  }
}

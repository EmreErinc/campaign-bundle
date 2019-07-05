package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.SalesEntity;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class BaseServiceImpl implements BaseService {
  private AccountRepository accountRepository;
  private SellerRepository sellerRepository;
  private CampaignRepository campaignRepository;
  private ItemRepository itemRepository;
  private SalesRepository salesRepository;

  @Autowired
  public BaseServiceImpl(AccountRepository accountRepository,
                         SellerRepository sellerRepository,
                         CampaignRepository campaignRepository,
                         ItemRepository itemRepository,
                         SalesRepository salesRepository) {
    this.accountRepository = accountRepository;
    this.sellerRepository = sellerRepository;
    this.campaignRepository = campaignRepository;
    this.itemRepository = itemRepository;
    this.salesRepository = salesRepository;
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
    Optional<CampaignEntity> campaignEntity = campaignRepository.findById(itemId);
    Long current = Instant.now().toEpochMilli();

    return (current > campaignEntity.get().getStartAt() && current < campaignEntity.get().getEndAt());
  }

  @Override
  public boolean stockIsAvailable(int itemId, int expectedSaleAndGiftCount) {
    Integer stock = itemRepository.findById(itemId).get().getStock();
    return (stock - expectedSaleAndGiftCount >= 0);
  }

  @Override
  public Integer getItemStock(int itemId) {
    return itemRepository.findById(itemId).get().getStock();
  }

  @Override
  public Optional<Boolean> campaignLimitIsAvailable(int accountId, int itemId){
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerIdAndItemId(accountId, itemId);
    if (!optionalSalesEntities.isPresent()){
      return Optional.of(false);
    }

    Integer campaignLimit = campaignRepository.findByItemId(itemId).get().getCampaignLimit();
    return Optional.of(optionalSalesEntities.get().size() <= campaignLimit);
  }

  @Override
  public Integer getCampaignUsageLimit(int accountId, int itemId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerIdAndItemId(accountId, itemId);
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
}

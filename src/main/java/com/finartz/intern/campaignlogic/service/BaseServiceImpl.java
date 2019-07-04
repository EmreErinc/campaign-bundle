package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.AccountRepository;
import com.finartz.intern.campaignlogic.repository.CampaignRepository;
import com.finartz.intern.campaignlogic.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BaseServiceImpl implements BaseService {
  private AccountRepository accountRepository;
  private SellerRepository sellerRepository;
  private CampaignRepository campaignRepository;

  @Autowired
  public BaseServiceImpl(AccountRepository accountRepository,
                         SellerRepository sellerRepository,
                         CampaignRepository campaignRepository) {
    this.accountRepository = accountRepository;
    this.sellerRepository = sellerRepository;
    this.campaignRepository = campaignRepository;
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
}

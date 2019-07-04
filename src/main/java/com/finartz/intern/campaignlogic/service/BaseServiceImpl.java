package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.AccountRepository;
import com.finartz.intern.campaignlogic.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BaseServiceImpl implements BaseService {
  private AccountRepository accountRepository;
  private SellerRepository sellerRepository;

  @Autowired
  public BaseServiceImpl(AccountRepository accountRepository,
                         SellerRepository sellerRepository) {
    this.accountRepository = accountRepository;
    this.sellerRepository = sellerRepository;
  }

  public Role getRoleByAccountId(String accountId) {
    return accountRepository.findById(Integer.valueOf(accountId)).get().getRole();
  }

  public Optional<Integer> getSellerIdByAccountId(String accountId) {
    return Optional.of(sellerRepository.findByAccountId(Integer.valueOf(accountId)).get().getId());
  }
}

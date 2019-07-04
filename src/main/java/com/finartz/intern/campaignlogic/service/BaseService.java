package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.value.Role;

import java.util.Optional;

public interface BaseService {
  Role getRoleByAccountId(String accountId);

  Optional<Integer> getSellerIdByAccountId(String accountId);
}

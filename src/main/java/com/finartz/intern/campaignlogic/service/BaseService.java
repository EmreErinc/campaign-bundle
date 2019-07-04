package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.value.Role;

import java.util.Optional;

public interface BaseService {
  Role getRoleByAccountId(int accountId);

  Optional<Integer> getSellerIdByAccountId(int accountId);

  Optional<CampaignEntity> getCampaignByItemId(int itemId);
}

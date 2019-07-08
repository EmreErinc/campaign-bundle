package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.Role;

import java.util.Optional;

public interface BaseService {
  Role getRoleByAccountId(int accountId);

  Optional<Integer> getSellerIdByAccountId(int accountId);

  Optional<CampaignEntity> getCampaignByItemId(int itemId);

  Optional<Integer> getSellerIdByItemId(int itemId);

  boolean campaignIsAvailable(int itemId);

  boolean stockIsAvailable(int itemId, int expectedSaleAndGiftCount);

  Integer getItemStock(int itemId);

  Optional<Boolean> campaignLimitIsAvailable(int accountId, int itemId);

  Integer getCampaignUsageLimit(int accountId, int itemId);

  Integer getCampaignCartLimit(int itemId);

  CartEntity getCartById(String cartId);

  Double getItemPrice(int itemId);

  Optional<Badge> getBadgeByItemId(int itemId);
}
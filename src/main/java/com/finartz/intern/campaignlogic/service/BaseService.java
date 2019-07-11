package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.model.value.Role;

import java.util.List;
import java.util.Optional;

public interface BaseService {
  Role getRoleByAccountId(int accountId);

  Optional<Integer> getSellerIdByAccountId(int accountId);

  Optional<Integer> getSellerIdByItemId(int itemId);

  Boolean stockIsAvailable(int itemId, int expectedSaleAndGiftCount);

  Integer getItemStock(int itemId);

  Double getItemPrice(int itemId);

  Integer getItemCountOnCart(String cartId, int itemId);

  Boolean userAvailableForCampaign(int accountId, int campaignId);

  Boolean campaignIsAvailable(int itemId);

  Optional<CampaignEntity> getCampaignByItemId(int itemId);

  List<CampaignEntity> getUsedCampaignsByUserId(int userId);

  Optional<Boolean> campaignLimitIsAvailableForAccount(int accountId, int itemId);

  Optional<Integer> getCampaignItemUsageCount(int accountId, int itemId);

  Optional<Integer> getCampaignUsageCount(int accountId, int itemId);

  Integer getCampaignCartLimit(int itemId);

  Integer getCampaignLimit(int itemId);

  Optional<Badge> getBadgeByItemId(int itemId);

  Optional<Badge> getBadgeByCampaignId(int campaignId);

  CampaignSummary prepareCampaignEntityToList(int accountId, CampaignEntity campaignEntity);

  CartEntity getCartById(String cartId);
}
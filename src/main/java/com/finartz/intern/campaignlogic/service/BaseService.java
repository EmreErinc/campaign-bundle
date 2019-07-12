package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.model.value.Role;

import java.util.List;
import java.util.Optional;

public interface BaseService {
  Role getRoleByAccountId(int accountId);

  Integer getSellerIdByAccountId(int accountId);

  Integer getSellerIdByItemId(int itemId);

  Boolean stockIsAvailable(int itemId, int expectedSaleAndGiftCount);

  Boolean itemOnCampaign(int itemId);

  ItemEntity getItemEntity(int itemId);

  Integer getItemStock(int itemId);

  Double getItemPrice(int itemId);

  Integer getItemCountOnCart(String cartId, int itemId);

  Boolean userAvailableForCampaign(int accountId, int campaignId);

  Boolean campaignIsAvailable(int itemId);

  CampaignEntity getCampaignEntity(int campaignId);

  Optional<CampaignEntity> getCampaignByItemId(int itemId);

  List<CampaignEntity> getUsedCampaignsByUserId(int userId);

  Boolean campaignLimitIsAvailableForAccount(int accountId, int itemId);

  Optional<Integer> getCampaignItemUsageCount(int accountId, int itemId);

  Optional<Integer> getCampaignUsageCount(int accountId, int itemId);

  Integer getCampaignCartLimit(int itemId);

  Integer getCampaignLimit(int itemId);

  Badge getBadgeByItemId(int itemId);

  Badge getBadgeByCampaignId(int campaignId);

  CampaignSummary prepareCampaignEntityToList(int accountId, CampaignEntity campaignEntity);

  Boolean itemOnCart(String cartId, int itemId);

  CartEntity getCartEntityById(String cartId);
}
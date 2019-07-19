package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.entity.VariantEntity;
import com.finartz.intern.campaignlogic.model.value.*;

import java.util.List;
import java.util.Optional;

public interface BaseService {
  Role getRoleByAccountId(int accountId);

  Integer getSellerIdByAccountId(int accountId);

  Integer getSellerIdByItemId(int itemId);

  Boolean isStockAvailable(int itemId, int expectedSaleAndGiftCount);

  Boolean isItemHasCampaign(int itemId);

  ItemEntity getItemEntity(int itemId);

  Integer getItemStock(int itemId);

  Double getItemPrice(int itemId);

  Integer getItemCountOnCart(String cartId, int itemId);

  Boolean userAvailableForCampaign(int accountId, int campaignId);

  Boolean isCampaignAvailableGetByItemId(int itemId);

  Boolean isCampaignAvailableGetById(int campaignId);

  CampaignEntity getCampaignEntity(int campaignId);

  Optional<CampaignEntity> getCampaignByItemId(int itemId);

  List<CampaignEntity> getUsedCampaignsByUserId(int userId);

  Boolean isCampaignLimitAvailableForAccount(int accountId, int itemId);

  Optional<Integer> getCampaignItemUsageCount(int accountId, int itemId);

  Integer getCampaignLimit(int itemId);

  Badge getBadgeByItemId(int itemId);

  Badge getBadgeByCampaignId(int campaignId);

  CampaignSummary prepareCampaignEntityToList(int accountId, CampaignEntity campaignEntity);

  Boolean isItemOnCart(String cartId, int itemId);

  CartEntity getCartEntityById(String cartId);

  void saveAsSoldCart(CartEntity cartEntity);

  Variant addVariant(VariantEntity variantEntity);

  Optional<List<Variant>> getItemVariants(int itemId);

  Optional<Variant> getItemVariant(int itemId, int variantId);

  List<VariantSpec> getItemVariantSpecs(int itemId, int variantId);

  Integer getItemVariantStock(int variantId);
}
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

  Integer getSellerIdByProductId(int itemId);

  Boolean isStockAvailable(int itemId, int expectedSaleAndGiftCount);

  Boolean isItemHasCampaign(int itemId);

  ItemEntity getItemEntity(int itemId);

  Integer getProductStock(int itemId);

  Double getProductPrice(int itemId);

  Integer getProductCountOnCart(String cartId, int itemId, Optional<Integer> optionalVariantId);

  Integer getTotalProductCountOnCart(String cartId, int itemId);

  Boolean userAvailableForCampaign(int accountId, int campaignId);

  Boolean isCampaignAvailableGetByItemId(int itemId);

  Boolean isCampaignAvailableGetById(int campaignId);

  CampaignEntity getCampaignEntity(int campaignId);

  Optional<CampaignEntity> getCampaignByProductId(int itemId);

  List<CampaignEntity> getUsedCampaignsByUserId(int userId);

  Boolean isCampaignLimitAvailableForAccount(int accountId, int itemId);

  Optional<Integer> getCampaignProductUsageCount(int accountId, int itemId);

  Integer getCampaignLimit(int itemId);

  Badge getBadgeByProductId(int itemId);

  Badge getBadgeByCampaignId(int campaignId);

  CampaignSummary prepareCampaignEntityToList(int accountId, CampaignEntity campaignEntity);

  Boolean isProductOnCart(String cartId, int itemId);

  CartEntity getCartEntityById(String cartId);

  void saveAsSoldCart(CartEntity cartEntity);

  Variant addVariant(VariantEntity variantEntity);

  Optional<List<Variant>> getProductVariants(int itemId);

  Optional<Variant> getProductVariant(int itemId, int variantId);

  List<VariantSpec> getProductVariantSpecs(int itemId, int variantId);

  Integer getProductVariantStock(int variantId);
}
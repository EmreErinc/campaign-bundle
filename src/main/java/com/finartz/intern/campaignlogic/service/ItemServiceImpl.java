package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.repository.*;
import com.finartz.intern.campaignlogic.security.Errors;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.finartz.intern.campaignlogic.security.Errors.ITEM_NOT_FOUND;

@Slf4j
@Service
public class ItemServiceImpl extends BaseServiceImpl implements ItemService {
  private ItemRepository itemRepository;

  @Autowired
  public ItemServiceImpl(ItemRepository itemRepository,
                         AccountRepository accountRepository,
                         SellerRepository sellerRepository,
                         CampaignRepository campaignRepository,
                         SalesRepository salesRepository,
                         CartRepository cartRepository,
                         VariantRepository variantRepository,
                         VariantSpecRepository variantSpecRepository,
                         SpecDataRepository specDataRepository,
                         SpecDetailRepository specDetailRepository) {
    super(accountRepository,
        sellerRepository,
        campaignRepository,
        itemRepository,
        salesRepository,
        cartRepository,
        variantRepository,
        variantSpecRepository,
        specDataRepository,
        specDetailRepository);
    this.itemRepository = itemRepository;
  }

  @Override
  public ItemResponse addItem(int accountId, AddItemRequest request) {
    if (!getRoleByAccountId(accountId).equals(Role.SELLER)) {
      throw new ApplicationContextException(Errors.NOT_PERMISSION);
    }

    Integer expectedSellerId = getSellerIdByAccountId(accountId);

    ItemEntity itemEntity = itemRepository
        .save(Converters
            .addItemRequestToItemEntity(request, expectedSellerId));

    List<Variant> variants = new ArrayList<>();

    if (request.getVariants() != null) {
      request
          .getVariants()
          .forEach(addVariantRequest -> {
            Variant variant = addVariant(Converters.prepareItemVariant(itemEntity.getId(), addVariantRequest));
            variants.add(variant);
          });
    }

    return Converters
        .itemEntityToItemResponse(itemEntity, variants);
  }

  @Override
  public ItemDetail getItem(Optional<Integer> accountId, String itemId) {
    ItemDetail itemDetail = Converters
        .itemEntityToItemDetail(getItemEntity(Integer.valueOf(itemId)),
            Badge.builder().build(),
            getProductVariants(Integer.valueOf(itemId)).orElse(new ArrayList<>()));

    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByProductId(Integer.valueOf(itemId));
    if (optionalCampaignEntity.isPresent()) {
      Badge badge = Badge.builder()
          .gift(optionalCampaignEntity.get().getGiftCount())
          .requirement(optionalCampaignEntity.get().getRequirementCount())
          .build();
      itemDetail.setBadge(badge);

      accountId.ifPresent(id -> checkCampaignAvailabilityAndSetBadge(accountId, itemId, itemDetail));
    }
    return itemDetail;
  }

  private void checkCampaignAvailabilityAndSetBadge(Optional<Integer> accountId, String itemId, ItemDetail itemDetail) {
    if (accountId.isPresent() && !isCampaignLimitAvailableForAccount(accountId.get(), Integer.valueOf(itemId))) {
      itemDetail.setBadge(Badge.builder().build());
    }
  }

  @Override
  public List<ItemSummary> searchItemList(Optional<Integer> accountId, Optional<String> searchText) {
    return Lists.newArrayList(ItemSummary.builder().build());
  }

  @Override
  public List<ItemSummary> getItemList(Optional<Integer> accountId, Optional<String> text) {
    //for all items
    List<ItemSummary> itemSummaries = new ArrayList<>();
    itemRepository
        .findAll()
        .forEach(itemEntity -> itemSummaries
            .add(Converters.itemEntityToItemSummary(itemEntity, getBadgeByProductId(itemEntity.getId()))));

    accountId.ifPresent(id -> eliminateUsedCampaignItems(itemSummaries, id)
        .forEach(itemSummary ->
            itemSummaries
                .stream()
                .filter(itemSum -> itemSum.getId().equals(itemSummary.getId()))
                .findFirst()
                .get()
                //set as null badge
                .setBadge(Badge.builder().build())));

    return itemSummaries;
  }

  @Override
  public List<ItemSummary> getSellerItems(Optional<Integer> accountId, String sellerId) {
    List<ItemSummary> itemSummaries = new ArrayList<>();

    Optional<List<ItemEntity>> optionalItemEntities = itemRepository.findBySellerId(Integer.valueOf(sellerId));
    if (!optionalItemEntities.isPresent()) {
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }

    optionalItemEntities
        .get()
        .forEach(itemEntity -> itemSummaries
            .add(Converters.itemEntityToItemSummary(itemEntity, getBadgeByProductId(itemEntity.getId()))));

    accountId.ifPresent(id -> eliminateUsedCampaignItems(itemSummaries, id)
        .forEach(itemSummary ->
            itemSummaries
                .stream()
                .filter(itemSum ->
                    itemSum.getId()
                        .equals(itemSummary.getId()))
                .findFirst()
                .get()
                //set as null badge
                .setBadge(Badge.builder().build())));
    return itemSummaries;
  }

  private List<ItemSummary> eliminateUsedCampaignItems(List<ItemSummary> itemSummaries, int accountId) {
    //shows user's used campaigns
    List<CampaignEntity> usedCampaigns = getUsedCampaignsByUserId(accountId);
    return itemSummaries
        .stream()
        .filter(itemSummary ->
            usedCampaigns
                .stream()
                //checks campaign limit status
                .anyMatch(campaignEntity -> !isCampaignLimitAvailableForAccount(accountId, itemSummary.getId()))
        )
        .collect(Collectors.toList());
  }
}
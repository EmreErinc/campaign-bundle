package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.ItemDetail;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl extends BaseServiceImpl implements ItemService {
  private ItemRepository itemRepository;

  @Autowired
  public ItemServiceImpl(ItemRepository itemRepository,
                         AccountRepository accountRepository,
                         SellerRepository sellerRepository,
                         CampaignRepository campaignRepository,
                         SalesRepository salesRepository,
                         CartRepository cartRepository) {
    super(accountRepository, sellerRepository, campaignRepository, itemRepository, salesRepository, cartRepository);
    this.itemRepository = itemRepository;
  }

  @Override
  public ItemResponse addItem(int accountId, AddItemRequest request) {

    if (!getRoleByAccountId(accountId).equals(Role.SELLER)) {
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    Optional<Integer> sellerId = getSellerIdByAccountId(accountId);
    if (!sellerId.isPresent()) {
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    ItemEntity itemEntity = itemRepository
        .save(Converters
            .addItemRequestToItemEntity(request, sellerId.get()));

    return Converters
        .itemEntityToItemResponse(itemEntity);
  }

  @Override
  public ItemDetail getItem(Optional<Integer> accountId, String itemId) {
    ItemDetail itemDetail = Converters
        .itemEntityToItemDetail(
            itemRepository
                .findById(Integer.valueOf(itemId)).get(), Badge.builder().build());

    if (accountId.isPresent()) {
      Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(Integer.valueOf(itemId));

      if (optionalCampaignEntity.isPresent() && campaignLimitIsAvailable(accountId.get(), Integer.valueOf(itemId)).get()) {
        Badge badge = Badge.builder()
            .gift(optionalCampaignEntity.get().getExpectedGiftCount())
            .requirement(optionalCampaignEntity.get().getRequirementCount())
            .build();
        itemDetail.setBadge(badge);
      }
    }
    return itemDetail;
  }

  @Override
  public List<ItemSummary> searchItemList(Optional<Integer> accountId, Optional<String> searchText) {
    return null;
  }

  /*@Override
  public List<ItemSummary> getItemList(Optional<String> accountId, Optional<String> searchText) {
    //TODO accountId ye ilişkin kampanyadan yararlanma durumuları kontrol edilip ona göre response dönmeli

    return null;
  }*/

  @Override
  public List<ItemSummary> getItemList(Optional<Integer> accountId, Optional<String> text) {

    List<ItemSummary> itemSummaries = new ArrayList<>();
    itemRepository
        .findAll()
        .forEach(itemEntity -> itemSummaries
            .add(Converters.itemEntityToItemSummary(itemEntity)));

    itemSummaries.stream().forEach(itemSummary -> {
      itemSummary.setBadge(getBadgeByItemId(Integer.valueOf(itemSummary.getId())).get());
    });

    if (accountId.isPresent()) {
      //TODO ilgili account a göre kontrol yapılacak
    }
    return itemSummaries;
  }

  @Override
  public List<ItemSummary> getSellerItems(Optional<Integer> accountId, String sellerId) {
    if (accountId.isPresent()) {
      //TODO accountId ye ilişkin kampanyadan yararlanma durumuları kontrol edilip ona göre response dönmeli
    }

    return Converters
        .itemEntitiesToItemSummaries(
            itemRepository
                .findBySellerId(Integer.valueOf(sellerId)).get());
  }
}

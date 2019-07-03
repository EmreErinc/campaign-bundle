package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.value.ItemDetail;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public abstract class ItemServiceImpl implements ItemService, BaseService {
  private ItemRepository itemRepository;

  @Autowired
  public ItemServiceImpl(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Override
  public ItemResponse addItem(String accountId, AddItemRequest request) {

    if (!getRoleByAccountId(accountId).equals(Role.SELLER)){
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    Optional<String> sellerId = getSellerIdByAccountId(accountId);
    if(!sellerId.isPresent()){
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    return Converters
        .itemEntityToItemResponse(
            itemRepository
                .save(Converters
                    .addItemRequestToItemEntity(request, sellerId.get())));

  }

  @Override
  public ItemDetail getItem(Optional<String> accountId, String itemId) {
    //TODO accountId ye ilişkin kampanyadan yararlanma durumuları kontrol edilip ona göre response dönmeli

    return Converters.itemEntityToItemDetail(itemRepository.findById(itemId).get());
  }

  /*@Override
  public List<ItemSummary> getItemList(Optional<String> accountId, Optional<String> searchText) {
    //TODO accountId ye ilişkin kampanyadan yararlanma durumuları kontrol edilip ona göre response dönmeli

    return null;
  }*/

  @Override
  public List<ItemSummary> getSellerItems(Optional<String> accountId, String sellerId){
    //TODO accountId ye ilişkin kampanyadan yararlanma durumuları kontrol edilip ona göre response dönmeli

    return Converters.itemEntitiesToItemSummaries(itemRepository.findBySellerId(sellerId).get());
  }
}

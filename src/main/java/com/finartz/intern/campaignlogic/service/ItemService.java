package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.value.ItemDetail;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;

import java.util.List;
import java.util.Optional;

public interface ItemService {
  ItemResponse addItem(int accountId, AddItemRequest request);

  ItemDetail getItem(Optional<Integer> accountId, String itemId);

  List<ItemSummary> searchItemList(Optional<Integer> accountId, Optional<String> searchText);

  List<ItemSummary> getSellerItems(Optional<Integer> accountId, String sellerId);

  List<ItemSummary> getItemList(Optional<Integer> accountId, Optional<String> text);
}

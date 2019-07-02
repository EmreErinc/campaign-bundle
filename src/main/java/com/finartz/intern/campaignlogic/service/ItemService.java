package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.value.ItemDetail;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;

import java.util.List;
import java.util.Optional;

public interface ItemService {
  boolean addItem(String userId, AddItemRequest request);

  ItemDetail getItem(Optional<String> userId, String itemId);

  List<ItemSummary> getItemList(Optional<String> searchText);
}

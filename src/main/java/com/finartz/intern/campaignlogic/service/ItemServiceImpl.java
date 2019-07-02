package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.value.ItemDetail;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {
  @Override
  public boolean addItem(String userId, AddItemRequest request) {
    return false;
  }

  @Override
  public ItemDetail getItem(Optional<String> userId, String itemId) {
    return null;
  }

  @Override
  public List<ItemSummary> getItemList(Optional<String> searchText) {
    return null;
  }
}

package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddStockRequest;
import com.finartz.intern.campaignlogic.model.response.StockResponse;
import com.finartz.intern.campaignlogic.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService {
  private ItemRepository itemRepository;

  @Autowired
  public StockServiceImpl(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Override
  public StockResponse addStock(AddStockRequest request) {
    int currentStock = itemRepository.findById(request.getItemId()).get().getStock();
    int newStock = currentStock + request.getStock();
    itemRepository.addStock(newStock, request.getItemId());
    return StockResponse.builder()
        .stock(itemRepository.findById(request.getItemId()).get().getStock().toString())
        .build();
  }

  @Override
  public StockResponse getStockCount(String itemId) {
    return StockResponse.builder()
        .stock(itemRepository.findById(Integer.valueOf(itemId)).get().getStock().toString())
        .build();
  }
}

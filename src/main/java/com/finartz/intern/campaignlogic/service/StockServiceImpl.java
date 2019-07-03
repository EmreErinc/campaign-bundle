package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddStockRequest;
import com.finartz.intern.campaignlogic.model.response.StockResponse;
import com.finartz.intern.campaignlogic.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService {
  private StockRepository stockRepository;

  @Autowired
  public StockServiceImpl(StockRepository stockRepository) {
    this.stockRepository = stockRepository;
  }

  @Override
  public StockResponse addStock(AddStockRequest request) {
    String currentStock = stockRepository.findByItemId(request.getItemId()).getStock().toString();
    int newStock = Integer.valueOf(currentStock) + Integer.valueOf(request.getStock());
    stockRepository.addStock(String.valueOf(newStock), request.getItemId());
    return StockResponse.builder()
        .stock(stockRepository.findByItemId(request.getItemId()).getStock().toString())
        .build();
  }

  @Override
  public StockResponse getStockCount(String itemId) {
    return StockResponse.builder()
        .stock(stockRepository.findByItemId(itemId).getStock().toString())
        .build();
  }
}

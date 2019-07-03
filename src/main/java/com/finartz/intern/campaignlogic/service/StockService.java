package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddStockRequest;
import com.finartz.intern.campaignlogic.model.response.StockResponse;

public interface StockService {
  StockResponse addStock(AddStockRequest request);

  StockResponse getStockCount(String itemId);
}

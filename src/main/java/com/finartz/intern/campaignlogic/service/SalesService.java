package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.SaleRequest;
import com.finartz.intern.campaignlogic.model.response.SaleResponse;

public interface SalesService {
  SaleResponse addSale(String accountId, SaleRequest request);
}

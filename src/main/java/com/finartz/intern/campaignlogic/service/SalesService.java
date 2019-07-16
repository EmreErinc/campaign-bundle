package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.response.SaleResponse;

public interface SalesService {
  SaleResponse addSale(int accountId, String cartId);
}

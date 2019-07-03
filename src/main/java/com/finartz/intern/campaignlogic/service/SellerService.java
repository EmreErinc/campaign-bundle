package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;

public interface SellerService {
  SellerResponse addSeller(String accountId, AddSellerRequest request);

  SellerResponse getSeller(String sellerId);
}

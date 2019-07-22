package com.finartz.intern.campaignlogic.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartControlResponse {
  private Boolean isAvailableForContinue;
  private String causeMessage;
  private Integer productId;
  private Integer variantId;
  private Integer desiredSaleCount;
}

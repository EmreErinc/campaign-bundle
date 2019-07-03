package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleRequest {
  private Integer itemId;
  private Integer sellerId;
  private Integer count;
  private Double price;
}

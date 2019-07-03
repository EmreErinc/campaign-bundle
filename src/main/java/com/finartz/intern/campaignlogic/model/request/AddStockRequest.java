package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddStockRequest {
  private String itemId;
  private String stock;
}

package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemGenerateResponse {
  private int productId;
  private int campaignId;
  private double price;
}

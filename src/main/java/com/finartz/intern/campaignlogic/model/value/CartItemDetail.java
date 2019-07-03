package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDetail {
  private String itemId;
  private Double addedAt;
  private Double updatedAt;
  private Integer count;
}

package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItem {
  private String itemId;
  private Double addedAt;
  private Double updatedAt;
  private Integer count;
}

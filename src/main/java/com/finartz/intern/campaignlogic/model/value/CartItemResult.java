package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemResult {
  private String itemId;
  private Double price;
  private Integer count;
}

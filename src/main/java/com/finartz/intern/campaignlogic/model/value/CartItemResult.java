package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResult extends CartItem{
  private String itemId;
  private Double price;
  private Integer count;
}

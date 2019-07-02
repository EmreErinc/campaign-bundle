package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemResultWithCampaign extends CartItem{
  private String itemId;
  private Double price;
  private Integer mainCount;
  private Integer giftCount;
  private Integer totalItemCount;
  private Badge badge;
}

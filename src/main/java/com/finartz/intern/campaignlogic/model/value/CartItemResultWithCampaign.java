package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResultWithCampaign extends CartItem{
  private String itemId;
  private Double price;
  private Integer mainCount;
  private Integer giftCount;
  private Integer totalItemCount;
  private Badge badge;
}

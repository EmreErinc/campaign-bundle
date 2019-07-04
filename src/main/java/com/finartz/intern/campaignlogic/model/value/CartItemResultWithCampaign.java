package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResultWithCampaign extends CartItem {
  private Integer expectedGiftCount;
  private Integer totalItemCount;
  private Badge badge;

  private CartItemResult cartItemResult;
}

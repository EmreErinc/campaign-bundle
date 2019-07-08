package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuitableSaleAndGiftCount {
  private int saleCount;
  private int giftCount;
}

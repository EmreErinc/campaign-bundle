package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class CartDto {
  private Integer accountId;
  private String cartId;
  private Integer productId;
  private Integer desiredCount;
  private Optional<Integer> variantId;
}

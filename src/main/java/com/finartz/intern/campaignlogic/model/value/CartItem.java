package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class CartItem<T extends AdditionalParams> {
  @NotNull(message = "itemId should not be empty")
  private Integer itemId;

  @NotNull(message = "sellerId should not be empty")
  private Integer sellerId;

  @NotNull(message = "count should not be empty")
  private Integer saleCount;

  @NotNull(message = "price should not be empty")
  private Double price;

  private Long addedAt;
  private Long updatedAt;
  private Boolean hasCampaign;
  private T additionalParams;
}

package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class CartItemDto {
  @NotNull(message = "productId should not be empty")
  private Integer productId;

  @NotNull(message = "sellerId should not be empty")
  private Integer sellerId;

  @NotNull(message = "count should not be empty")
  private Integer saleCount;

  @NotNull(message = "price should not be empty")
  private Double price;

  private Boolean hasCampaign;
  private CampaignParams campaignParams;
  private Boolean hasVariant;
  private Variant variant;

  private String message;
}

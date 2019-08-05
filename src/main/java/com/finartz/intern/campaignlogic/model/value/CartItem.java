package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class CartItem {
  @NotNull(message = "productId should not be empty")
  private Integer productId;

  @NotNull(message = "productName should not be empty")
  private String productName;

  @NotNull(message = "sellerId should not be empty")
  private Integer sellerId;

  @NotNull(message = "desiredSaleCount should not be empty")
  private Integer desiredSaleCount;

  @NotNull(message = "desiredCount should not be empty")
  private Integer saleCount;

  @NotNull(message = "price should not be empty")
  private Double price;

  private Long addedAt;
  private Long updatedAt;
  private Boolean hasCampaign;
  private CampaignParams campaignParams;
  private Boolean hasVariant;
  private Variant variant;
  private Integer messageKey;
}

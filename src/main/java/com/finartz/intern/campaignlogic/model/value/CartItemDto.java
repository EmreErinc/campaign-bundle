package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDto {
  private Integer productId;
  private String productName;
  private Integer sellerId;
  private Integer desiredSaleCount;
  private Integer saleCount;
  private Double price;
  private Boolean hasCampaign;
  private CampaignParams campaignParams;
  private Boolean hasVariant;
  private Variant variant;
  private String message;
}

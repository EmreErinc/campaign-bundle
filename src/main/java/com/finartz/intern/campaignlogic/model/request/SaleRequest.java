package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class SaleRequest {

  @NotNull(message = "itemId should not be empty")
  private Integer itemId;

  @NotNull(message = "sellerId should not be empty")
  private Integer sellerId;

  @NotNull(message = "count should not be empty")
  private Integer count;

  @NotNull(message = "price should not be empty")
  private Double price;
}

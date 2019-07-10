package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class AddStockRequest {

  @NotNull(message = "itemId should not be empty")
  private Integer itemId;

  @NotNull(message = "stock count should not be empty")
  private Integer stock;
}
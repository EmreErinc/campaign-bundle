package com.finartz.intern.campaignlogic.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddStockRequest {

  @NotNull(message = "productId should not be empty")
  private Integer productId;

  @NotNull(message = "stock count should not be empty")
  private Integer stock;
}
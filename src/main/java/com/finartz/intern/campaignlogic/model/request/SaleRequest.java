package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class SaleRequest {

  @Valid
  @NotNull(message = "sale list should not be empty")
  private String cartId;
}

package com.finartz.intern.campaignlogic.model.request;

import com.finartz.intern.campaignlogic.model.value.CartItem;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class SaleRequest {

  @Valid
  @NotNull(message = "sale list should not be empty")
  private List<CartItem> items;
}

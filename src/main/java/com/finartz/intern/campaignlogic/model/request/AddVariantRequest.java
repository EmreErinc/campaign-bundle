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
public class AddVariantRequest {
  @NotNull
  private Integer specDetailId;

  @NotNull
  private Integer specDataId;

  @NotNull
  private Integer stock;

  private Double price;
}
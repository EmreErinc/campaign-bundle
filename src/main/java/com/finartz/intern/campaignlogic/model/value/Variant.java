package com.finartz.intern.campaignlogic.model.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Variant {
  @NotNull
  private Specification specification;

  @NotNull
  private String detail;

  private Double price;
}

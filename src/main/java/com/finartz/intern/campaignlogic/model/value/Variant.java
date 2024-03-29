package com.finartz.intern.campaignlogic.model.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Variant {
  private Integer id;
  private Double price;
  private Integer stock;
  private List<VariantSpec> variantSpecs;
}
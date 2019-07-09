package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemSummary {
  private Integer id;
  private Double price;
  private String name;
  private CargoType cargoType;
  private Badge badge;
}
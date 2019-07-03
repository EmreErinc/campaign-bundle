package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemSummary {
  private String id;
  private Double price;
  private String name;
  private CargoType cargoType;
}

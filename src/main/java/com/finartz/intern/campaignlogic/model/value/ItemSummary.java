package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemSummary {
  private String id;
  private Double price;
  private String name;
  private Cargo cargo;
}

package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemDetail {
  private String id;
  private Double price;
  private String name;
  private String description;
  private Cargo cargo;
}
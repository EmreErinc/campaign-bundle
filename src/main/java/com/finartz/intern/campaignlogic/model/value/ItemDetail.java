package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemDetail {
  private String id;
  private Double price;
  private String name;
  private String description;
  private CargoType cargoType;
  private Badge badge;
  private List<Variant> variants;
}

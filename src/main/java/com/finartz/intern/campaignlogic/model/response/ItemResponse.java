package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CargoType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponse {
  private Integer itemId;
  private Double price;
  private String name;
  private String description;
  private CargoType cargoType;
  private Badge badge;
}

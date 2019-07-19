package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CargoType;
import com.finartz.intern.campaignlogic.model.value.Variant;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemResponse {
  private Integer productId;
  private Double price;
  private String name;
  private String description;
  private CargoType cargoType;
  private Badge badge;
  private List<Variant> variants;
}

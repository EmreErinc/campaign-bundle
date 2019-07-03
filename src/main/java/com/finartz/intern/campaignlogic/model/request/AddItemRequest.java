package com.finartz.intern.campaignlogic.model.request;

import com.finartz.intern.campaignlogic.model.value.CargoType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddItemRequest {
  private String name;
  private String description;
  private Double price;
  private CargoType cargoType;
  private String cargoCompany;
}

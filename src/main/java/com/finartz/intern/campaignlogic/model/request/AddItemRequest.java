package com.finartz.intern.campaignlogic.model.request;

import com.finartz.intern.campaignlogic.model.value.CargoType;
import com.finartz.intern.campaignlogic.model.value.Variant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddItemRequest {

  @NotNull(message = "name should not be empty")
  @Size(min = 3, max = 70)
  private String name;

  @NotNull(message = "description should not be empty")
  @Size(min = 3, max = 255)
  private String description;

  @NotNull(message = "price should not be empty")
  private Double price;

  @NotNull(message = "cargoType should not be empty")
  private CargoType cargoType;

  @NotNull(message = "cargoCompany should not be empty")
  @Size(min = 3, max = 50)
  private String cargoCompany;

  @NotNull(message = "stock should not be empty")
  private Integer stock;

  private List<Variant> variants;
}
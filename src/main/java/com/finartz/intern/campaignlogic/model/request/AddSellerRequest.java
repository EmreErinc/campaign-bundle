package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class AddSellerRequest {

  @NotNull(message = "name should not be empty")
  @Size(min = 3, max = 50)
  private String name;

  @NotNull(message = "address should not be empty")
  @Size(min = 3, max = 255)
  private String address;
}
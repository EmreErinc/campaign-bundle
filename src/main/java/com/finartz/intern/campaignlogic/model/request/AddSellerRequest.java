package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class AddSellerRequest {

  @NotNull(message = "name should not be empty")
  private String name;

  @NotNull(message = "address should not be empty")
  private String address;
}

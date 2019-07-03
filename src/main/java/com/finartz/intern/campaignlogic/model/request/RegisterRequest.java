package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class RegisterRequest {
  @NotNull
  private String name;
  private String lastName;
  private String email;
  private String password;
}

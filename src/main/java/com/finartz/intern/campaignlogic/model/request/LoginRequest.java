package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class LoginRequest {

  @NotNull(message = "email should not be empty")
  private String email;

  @NotNull(message = "password should not be empty")
  private String password;
}

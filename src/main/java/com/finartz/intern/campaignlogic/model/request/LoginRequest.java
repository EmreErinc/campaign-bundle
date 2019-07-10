package com.finartz.intern.campaignlogic.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @NotNull(message = "email should not be empty")
  @Size(min = 3, max = 50)
  private String email;

  @NotNull(message = "password should not be empty")
  @Size(min = 3, max = 50)
  private String password;
}
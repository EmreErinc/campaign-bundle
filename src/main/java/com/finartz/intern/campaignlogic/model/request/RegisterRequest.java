package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterRequest {
  private String name;
  private String lastName;
  private String email;
  private String password;
}

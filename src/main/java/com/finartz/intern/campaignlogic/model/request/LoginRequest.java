package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
  private String email;
  private String password;
}

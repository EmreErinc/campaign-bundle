package com.finartz.intern.campaignlogic.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
  private Integer id;
  private String name;
  private String lastName;
  private String token;
}

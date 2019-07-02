package com.finartz.intern.campaignlogic.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {
  private String id;
  private String name;
  private String lastName;
  private String token;
  private String cartId;
}

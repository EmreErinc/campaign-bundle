package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
  private Integer id;
  private String name;
  private String lastName;
  private String token;
  private Role role;
}
package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddSellerRequest {
  private String name;
  private String address;
}

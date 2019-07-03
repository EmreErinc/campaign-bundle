package com.finartz.intern.campaignlogic.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerResponse {
  private String id;
  private String name;
  private String address;
}

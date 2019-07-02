package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddItemRequest {
  private String name;
  private String description;
  private Double price;
}

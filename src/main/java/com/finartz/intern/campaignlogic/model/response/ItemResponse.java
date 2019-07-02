package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.Cargo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemResponse {
  private String id;
  private Double price;
  private String name;
  private String description;
  private Cargo cargo;
  private Badge badge;
}

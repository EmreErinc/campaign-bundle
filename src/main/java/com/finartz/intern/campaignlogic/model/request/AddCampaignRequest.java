package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddCampaignRequest {
  private String id;
  private Double startAt;
  private Double endAt;
  private Integer cartLimit;
  private Integer campaignLimit;
}

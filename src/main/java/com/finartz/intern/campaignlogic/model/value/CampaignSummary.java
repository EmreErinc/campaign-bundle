package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignSummary {
  private String title;
  private Long startAt;
  private Long endAt;
  private Integer cartLimit;
  private Integer campaignLimit;
  private CampaignStatus status;
  private String campaignTitle;
}

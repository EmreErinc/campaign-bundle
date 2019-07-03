package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddCampaignRequest {
  private String id;
  private String itemId;
  private String title;
  private Long startAt;
  private Long endAt;
  private Integer cartLimit;
  private Integer campaignLimit;
  private Integer requirement;
  private Integer gift;
}

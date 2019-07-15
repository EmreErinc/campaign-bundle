package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignResponse {
  private Integer id;
  private Integer itemId;
  private Integer sellerId;
  private String title;
  private Long startAt;
  private Long endAt;
  private Integer cartLimit;
  private Integer campaignLimit;
  private CampaignStatus status;
  private Badge badge;
}

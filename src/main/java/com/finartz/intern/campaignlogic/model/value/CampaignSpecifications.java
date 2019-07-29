package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignSpecifications {
  private int sellerId;
  private int cartLimit;
  private int campaignLimit;
  private int requirementCount;
  private int giftCount;
}

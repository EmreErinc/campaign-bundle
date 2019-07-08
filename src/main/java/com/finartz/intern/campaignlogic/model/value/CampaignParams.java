package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignParams {
  private Integer expectedGiftCount;
  private Integer totalItemCount;
  private Badge badge;
}

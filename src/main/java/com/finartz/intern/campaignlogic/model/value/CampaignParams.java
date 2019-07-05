package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CampaignParams extends AdditionalParams {
  private Integer expectedGiftCount;
  private Integer totalItemCount;
  private Badge badge;
}

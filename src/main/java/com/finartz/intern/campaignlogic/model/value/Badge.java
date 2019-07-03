package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Badge {
  private Integer requirement;
  private Integer gift;
}

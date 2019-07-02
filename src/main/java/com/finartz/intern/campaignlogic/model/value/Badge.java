package com.finartz.intern.campaignlogic.model.value;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Badge {
  private Integer requirement;
  private Integer gift;
}

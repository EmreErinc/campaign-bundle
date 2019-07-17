package com.finartz.intern.campaignlogic.model.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariantSpec {
  private Integer id;
  private String specDetail;
  private String specData;
}

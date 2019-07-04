package com.finartz.intern.campaignlogic.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SaleResponse {
  private List<Integer> saleIds;
}

package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class AddCampaignRequest {

  @NotNull(message = "itemId should not be empty")
  private Integer itemId;

  @NotNull(message = "title should not be empty")
  private String title;

  @NotNull(message = "startAt should not be empty")
  private Long startAt;

  @NotNull(message = "endAt should not be empty")
  private Long endAt;

  @NotNull(message = "cartLimit should not be empty")
  private Integer cartLimit;

  @NotNull(message = "campaignLimit should not be empty")
  private Integer campaignLimit;

  @NotNull(message = "requirement count should not be empty")
  private Integer requirement;

  @NotNull(message = "gift count should not be empty")
  private Integer gift;
}

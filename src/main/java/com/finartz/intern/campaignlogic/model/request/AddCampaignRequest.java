package com.finartz.intern.campaignlogic.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class AddCampaignRequest {

  @NotNull(message = "itemId should not be empty")
  private Integer itemId;

  @NotNull(message = "title should not be empty")
  @Size(min = 3, max = 255)
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

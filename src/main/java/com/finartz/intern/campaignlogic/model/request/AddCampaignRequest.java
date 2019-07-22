package com.finartz.intern.campaignlogic.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddCampaignRequest {

  @NotNull(message = "productId should not be empty")
  private Integer productId;

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

  @NotNull(message = "requirement desiredCount should not be empty")
  private Integer requirement;

  @NotNull(message = "gift desiredCount should not be empty")
  private Integer gift;
}
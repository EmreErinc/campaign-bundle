package com.finartz.intern.campaignlogic.model.entity;

import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.model.value.Badge;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Builder
public class CampaignEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;
  private Long startAt;
  private Long endAt;
  private Long createdAt;
  private Long updatedAt;
  private Integer cartLimit;
  private Integer campaignLimit;
  private CampaignStatus status;
  private Integer requirement;
  private Integer gift;
}

package com.finartz.intern.campaignlogic.model.entity;

import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class CampaignEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer campaignId;

  @Column(name = "item_id")
  @NotNull
  private String itemId;

  @Column(name = "seller_id")
  @NotNull
  private String sellerId;

  @Column(name = "title")
  @NotNull
  private String title;

  @Column(name = "start_at")
  @NotNull
  private Long startAt;

  @Column(name = "end_at")
  @NotNull
  private Long endAt;

  @Column(name = "created_at")
  @NotNull
  private Long createdAt;

  @Column(name = "updated_at")
  private Long updatedAt;

  @Column(name = "cart_limit")
  @NotNull
  private Integer cartLimit;

  @Column(name = "campaign_limit")
  @NotNull
  private Integer campaignLimit;

  @Column(name = "status")
  @NotNull
  @Enumerated(EnumType.STRING)
  private CampaignStatus status;

  @Column(name = "requirement_count")
  @NotNull
  private Integer requirementCount;

  @Column(name = "gift_count")
  @NotNull
  private Integer giftCount;
}

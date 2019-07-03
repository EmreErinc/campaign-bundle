package com.finartz.intern.campaignlogic.model.entity;

import com.finartz.intern.campaignlogic.model.value.SellerStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "seller")
@Data
@Builder
@NoArgsConstructor
public class SellerEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer sellerId;

  @Column(name = "seller_name")
  @NotNull
  private String name;

  @Column(name = "created_at")
  @NotNull
  private Long createdAt;

  @Column(name = "updated_at")
  private Long updatedAt;

  @Column(name = "status")
  @NotNull
  @Enumerated(EnumType.STRING)
  private SellerStatus status;

  @Column(name = "address")
  @NotNull
  private String address;

  @Column(name = "account_id")
  @NotNull
  private Integer accountId;
}

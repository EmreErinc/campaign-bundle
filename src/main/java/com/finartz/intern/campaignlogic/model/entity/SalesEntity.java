package com.finartz.intern.campaignlogic.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "owner_id")
  @NotNull
  private Integer ownerId;

  @Column(name = "item_id")
  @NotNull
  private Integer itemId;

  @Column(name = "sale_count")
  @NotNull
  private Integer saleCount;

  @Column(name = "gift_count")
  private Integer giftCount;

  @Column(name = "sold_at")
  @NotNull
  private Long soldAt;

  @Column(name = "price")
  @NotNull
  private Double price;

  @Column(name = "variant_id")
  @NotNull
  private Integer variantId;
}
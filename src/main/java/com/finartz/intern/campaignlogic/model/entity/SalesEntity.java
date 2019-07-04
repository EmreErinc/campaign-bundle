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

  @Column(name = "count")
  @NotNull
  private Integer count;

  @Column(name = "sold_at")
  @NotNull
  private Long soldAt;

  @Column(name = "price")
  @NotNull
  private Double price;
}

package com.finartz.intern.campaignlogic.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "seller")
@Data
@Builder
@NoArgsConstructor
public class StockEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer stockId;

  @Column(name = "item_id")
  @NotNull
  private String itemId;

  @Column(name = "stock")
  @NotNull
  private Integer stock;
}

package com.finartz.intern.campaignlogic.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "product_id")
  @NotNull
  private Integer productId;

  @Column(name = "price")
  @NotNull
  private Double price;

  @Column(name = "stock")
  @NotNull
  private Integer stock;
}
package com.finartz.intern.campaignlogic.model.entity;

import com.finartz.intern.campaignlogic.model.value.CargoType;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer itemId;

  @Column(name = "seller_id")
  @NotNull
  private Integer sellerId;

  @Column(name = "price")
  @NotNull
  private Double price;

  @Column(name = "name")
  @NotNull
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "cargo_status")
  @Enumerated(EnumType.STRING)
  private CargoType cargoType;

  @Column(name = "cargo_company")
  private String cargoCompany;

  @Column(name = "created_at")
  @NotNull
  private Long createdAt;

  @Column(name = "updated_at")
  private Long updatedAt;
}

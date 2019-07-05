package com.finartz.intern.campaignlogic.model.entity;

import com.finartz.intern.campaignlogic.model.value.CargoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "seller_id")
  @NotNull
  private Integer sellerId;

  @Column(name = "price")
  @NotNull
  private Double price;

  @Column(name = "name")
  @NotNull
  @Size(min = 3, max = 70)
  private String name;

  @Column(name = "description")
  @Size(min = 3, max = 255)
  private String description;

  @Column(name = "cargo_status")
  @Enumerated(EnumType.STRING)
  private CargoType cargoType;

  @Column(name = "cargo_company")
  @Size(min = 3, max = 50)
  private String cargoCompany;

  @Column(name = "created_at")
  @NotNull
  private Long createdAt;

  @Column(name = "updated_at")
  private Long updatedAt;

  @Column(name = "stock")
  @NotNull
  private Integer stock;
}

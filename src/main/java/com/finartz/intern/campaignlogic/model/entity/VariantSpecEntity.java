package com.finartz.intern.campaignlogic.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "variant_spec")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantSpecEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "product_id")
  @NotNull
  private Integer productId;

  @Column(name = "variant_id")
  @NotNull
  private Integer variantId;

  @Column(name = "spec_data_id")
  @NotNull
  private Integer specDataId;
}

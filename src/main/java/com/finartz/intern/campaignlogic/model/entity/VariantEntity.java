package com.finartz.intern.campaignlogic.model.entity;

import com.finartz.intern.campaignlogic.model.value.Specification;
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

  @Column(name = "item_id")
  @NotNull
  private Integer itemId;

  @Column(name = "price")
  @NotNull
  private Double price;

  @Column(name = "specification")
  @NotNull
  private Specification specification;

  @Column(name = "specification_detail")
  @NotNull
  private String specificationDetail;

}

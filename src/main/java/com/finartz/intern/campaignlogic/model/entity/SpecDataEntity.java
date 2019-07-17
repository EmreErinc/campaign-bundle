package com.finartz.intern.campaignlogic.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "spec_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecDataEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "data")
  @NotNull
  private String data;

  @Column(name = "spec_detail_id")
  @NotNull
  private Integer specDetailId;
}

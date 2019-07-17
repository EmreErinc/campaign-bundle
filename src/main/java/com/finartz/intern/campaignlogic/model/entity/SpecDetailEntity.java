package com.finartz.intern.campaignlogic.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "spec_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecDetailEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "detail")
  @NotNull
  private String detail;
}
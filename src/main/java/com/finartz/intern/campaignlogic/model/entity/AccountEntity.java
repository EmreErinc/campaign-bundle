package com.finartz.intern.campaignlogic.model.entity;

import com.finartz.intern.campaignlogic.model.value.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name")
  @NotNull
  @Size(min = 3, max = 50)
  private String name;

  @Column(name = "last_name")
  @NotNull
  @Size(min = 3, max = 50)
  private String lastName;

  @Column(name = "email")
  @NotNull
  @Size(min = 3, max = 50)
  private String email;

  @Column(name = "password")
  @NotNull
  @Size(min = 3, max = 50)
  private String password;

  @Column(name = "created_at")
  @NotNull
  private Long createdAt;

  @Column(name = "updated_at")
  private Long updatedAt;

  @Column(name = "role")
  @NotNull
  @Enumerated(EnumType.STRING)
  private Role role;
}

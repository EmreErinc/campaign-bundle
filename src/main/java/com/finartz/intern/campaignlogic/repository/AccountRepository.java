package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.UserEntity;

import java.util.Optional;

public interface AccountRepository extends BaseRepository<UserEntity, String> {
  Optional<UserEntity> findByEmailAndPassword(String email, String password);
}

package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.AccountEntity;

import java.util.Optional;

public interface AccountRepository extends BaseRepository<AccountEntity, String> {
  Boolean existsByEmail(String s);

  Optional<AccountEntity> findByEmailAndPassword(String email, String password);
}

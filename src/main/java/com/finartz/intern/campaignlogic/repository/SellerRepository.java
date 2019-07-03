package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.SellerEntity;

import java.util.Optional;

public interface SellerRepository extends BaseRepository<SellerEntity, String> {
  Optional<SellerEntity> findByAccountId(String accountId);
}

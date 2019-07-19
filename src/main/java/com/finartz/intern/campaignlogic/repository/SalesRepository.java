package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.SalesEntity;

import java.util.List;
import java.util.Optional;

public interface SalesRepository extends BaseRepository<SalesEntity, String> {
  Optional<List<SalesEntity>> findByOwnerIdAndProductId(int accountId, int itemId);

  Optional<List<SalesEntity>> findByProductId(int itemId);

  Optional<List<SalesEntity>> findByOwnerId(int userId);
}

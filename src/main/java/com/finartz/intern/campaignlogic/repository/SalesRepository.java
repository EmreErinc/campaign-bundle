package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.SalesEntity;

import java.util.List;
import java.util.Optional;

public interface SalesRepository extends BaseRepository<SalesEntity, String> {
  Optional<List<SalesEntity>> findByOwnerIdAndItemId(int accountId, int itemId);
}

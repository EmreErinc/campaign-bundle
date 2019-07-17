package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.VariantEntity;

import java.util.List;
import java.util.Optional;

public interface VariantRepository extends BaseRepository<VariantEntity, String>{
  Optional<List<VariantEntity>> findByItemId(int itemId);

  boolean existsByItemId(int itemId);
}

package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.VariantEntity;

import java.util.List;
import java.util.Optional;

public interface VariantRepository extends BaseRepository<VariantEntity, String>{
  Optional<List<VariantEntity>> findByProductId(int itemId);

  boolean existsByProductId(int itemId);
}

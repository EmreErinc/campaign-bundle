package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.VariantSpecEntity;

import java.util.List;
import java.util.Optional;

public interface VariantSpecRepository extends BaseRepository<VariantSpecEntity, String> {
  Optional<List<VariantSpecEntity>> findByItemIdAndVariantId(int itemId, int variantId);
}

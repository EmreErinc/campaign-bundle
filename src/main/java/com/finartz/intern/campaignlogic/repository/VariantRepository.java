package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.VariantEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface VariantRepository extends BaseRepository<VariantEntity, String>{
  Optional<List<VariantEntity>> findByProductId(int itemId);

  boolean existsByProductId(int itemId);

  @Transactional
  @Modifying
  @Query(value = "update variants v set v.stock=v.stock + ?1 where v.id=?2", nativeQuery = true)
  void addStock(int stock, int variantId);
}

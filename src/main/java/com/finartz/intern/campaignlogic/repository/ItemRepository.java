package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends BaseRepository<ItemEntity, String> {
  Optional<List<ItemEntity>> findBySellerId(int sellerId);

  @Transactional
  @Modifying
  @Query(value = "update items i set i.stock=i.stock + ?1 where i.id=?2", nativeQuery = true)
  void addStock(int stock, int itemId);
}

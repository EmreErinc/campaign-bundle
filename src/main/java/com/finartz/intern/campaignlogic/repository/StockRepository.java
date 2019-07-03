package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.StockEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StockRepository extends BaseRepository<StockEntity, String> {

  @Transactional
  @Modifying
  @Query(value = "update stock c set c.stock=c.stock+?1 where c.item_id=?2", nativeQuery = true)
  void addStock(String stock, String itemId);

  StockEntity findByItemId(String itemId);
}

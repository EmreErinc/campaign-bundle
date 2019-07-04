package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends BaseRepository<ItemEntity, String> {

  //@Query(value = "select * from item i where i.name like '%:name%''")
  //List<ItemEntity> findText(String text);

  Optional<List<ItemEntity>> findBySellerId(int sellerId);

  Optional<List<ItemEntity>> findAllById(int itemId);

  //Optional<ItemEntity> findById(int itemId);

  @Transactional
  @Modifying
  @Query(value = "update items i set i.stock=?1 where i.id=?2", nativeQuery = true)
  void addStock(int stock, int itemId);
}

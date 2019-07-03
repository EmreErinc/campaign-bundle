package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends BaseRepository<ItemEntity, String> {

  //@Query(value = "select * from item i where i.name like '%:name%''")
  //List<ItemEntity> findText(String text);

  //List<ItemEntity> findBySellerId(String sellerId);
}

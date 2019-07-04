package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends BaseRepository<CampaignEntity, String> {

  //@Transactional
  //@Modifying
  //@Query(value = "update campaign c set status=?1 where c.campaign_id=?2", nativeQuery = true)
  //void updateCampaign(CampaignStatus status, String campaignId);

  Optional<List<CampaignEntity>> findBySellerId(int sellerId);

  Optional<CampaignEntity> findByItemId(int itemId);
}

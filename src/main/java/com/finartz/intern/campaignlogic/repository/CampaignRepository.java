package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CampaignRepository extends BaseRepository<CampaignEntity, String> {

  @Transactional
  @Modifying
  @Query(value = "update campaign c set status=?1 where c.campaign_id=?2", nativeQuery = true)
  void updateCampaign(CampaignStatus status, String campaignId);
}

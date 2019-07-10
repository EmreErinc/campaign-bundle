package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends BaseRepository<CampaignEntity, String> {
  Optional<List<CampaignEntity>> findBySellerId(int sellerId);

  Optional<CampaignEntity> findByItemId(int itemId);

  Optional<Boolean> existsByItemId(int itemId);
}

package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends BaseRepository<CampaignEntity, String> {
  Optional<List<CampaignEntity>> findBySellerId(int sellerId);

  Optional<List<CampaignEntity>> findBySellerIdAndStatusEquals(int sellerId, CampaignStatus status);

  Optional<CampaignEntity> findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(int campaignId, Long currentForStart, Long currentForEnd);

  Optional<CampaignEntity> findByProductIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(int campaignId, Long currentForStart, Long currentForEnd);

  Optional<CampaignEntity> findByProductId(int itemId);

  Boolean existsByProductId(int itemId);
}

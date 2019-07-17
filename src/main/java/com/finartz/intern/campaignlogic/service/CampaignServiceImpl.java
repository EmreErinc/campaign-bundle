package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.response.CampaignResponse;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.finartz.intern.campaignlogic.security.Errors.*;

@Service
public class CampaignServiceImpl extends BaseServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;

  @Autowired
  public CampaignServiceImpl(CampaignRepository campaignRepository,
                             AccountRepository accountRepository,
                             SellerRepository sellerRepository,
                             ItemRepository itemRepository,
                             SalesRepository salesRepository,
                             CartRepository cartRepository,
                             VariantRepository variantRepository,
                             VariantSpecRepository variantSpecRepository,
                             SpecDataRepository specDataRepository,
                             SpecDetailRepository specDetailRepository) {
    super(accountRepository,
        sellerRepository,
        campaignRepository,
        itemRepository,
        salesRepository,
        cartRepository,
        variantRepository,
        variantSpecRepository,
        specDataRepository,
        specDetailRepository);
    this.campaignRepository = campaignRepository;
  }

  @Override
  public CampaignResponse addCampaign(int accountId, AddCampaignRequest request) {
    if (!getRoleByAccountId(accountId).equals(Role.SELLER)) {
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    Integer sellerId = getSellerIdByAccountId(accountId);
    CampaignEntity campaignEntity = campaignRepository
        .save(Converters
            .addCampaignRequestToCampaignEntity(request, sellerId));

    return Converters
        .campaignEntityToCampaignResponse(campaignEntity, getBadgeByCampaignId(campaignEntity.getId()));
  }

  @Override
  public CampaignResponse getCampaign(int accountId, String campaignId) {
    CampaignResponse campaignResponse = Converters
        .campaignEntityToCampaignResponse(
            getCampaignEntity(Integer.valueOf(campaignId)),
            getBadgeByCampaignId(Integer.valueOf(campaignId)));

    if (!userAvailableForCampaign(accountId, Integer.valueOf(campaignId)) && !isCampaignAvailableGetById(Integer.valueOf(campaignId))) {
      campaignResponse.setBadge(Badge.builder().build());
    }
    return campaignResponse;
  }

  @Override
  public boolean updateCampaignStatus(int accountId, String campaignId, CampaignStatus status) {
    if (!getRoleByAccountId(accountId).equals(Role.SELLER)) {
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findById(Integer.valueOf(campaignId));
    if (!optionalCampaignEntity.isPresent()){
      throw new ApplicationContextException(CAMPAIGN_NOT_FOUND);
    }
    optionalCampaignEntity.get().setStatus(CampaignStatus.valueOf(status.name()));
    campaignRepository.save(optionalCampaignEntity.get());
    return true;
  }

  @Override
  public List<CampaignSummary> getCampaignList(int accountId, String sellerId) {
    Optional<List<CampaignEntity>> optionalCampaignEntities = campaignRepository
        .findBySellerIdAndStatusEquals(Integer.valueOf(sellerId), CampaignStatus.ACTIVE);

    List<CampaignSummary> campaignSummaries = new ArrayList<>();
    optionalCampaignEntities.ifPresent(campaignEntities ->
        campaignEntities
            .forEach(campaignEntity ->
                campaignSummaries
                    .add(prepareCampaignEntityToList(accountId, campaignEntity))));

    return campaignSummaries;
  }
}
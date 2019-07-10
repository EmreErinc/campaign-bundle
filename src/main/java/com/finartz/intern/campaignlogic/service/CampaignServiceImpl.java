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

@Service
public class CampaignServiceImpl extends BaseServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;

  @Autowired
  public CampaignServiceImpl(CampaignRepository campaignRepository,
                             AccountRepository accountRepository,
                             SellerRepository sellerRepository,
                             ItemRepository itemRepository,
                             SalesRepository salesRepository,
                             CartRepository cartRepository) {
    super(accountRepository, sellerRepository, campaignRepository, itemRepository, salesRepository, cartRepository);
    this.campaignRepository = campaignRepository;
  }

  @Override
  public CampaignResponse addCampaign(int accountId, AddCampaignRequest request) {
    //TODO ürüne ait başka kampanya var mı yok mu
    if (!getRoleByAccountId(accountId).equals(Role.SELLER)) {
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    Integer sellerId = getSellerIdByAccountId(accountId).get();
    CampaignEntity campaignEntity = campaignRepository
        .save(Converters
            .addCampaignRequestToCampaignEntity(request, sellerId));

    return Converters
        .campaignEntityToCampaignResponse(campaignEntity, getBadgeByCampaignId(campaignEntity.getId()).get());
  }

  @Override
  public CampaignResponse getCampaign(int accountId, String campaignId) {
    CampaignResponse campaignResponse = Converters
        .campaignEntityToCampaignResponse(
            campaignRepository
                .findById(Integer.valueOf(campaignId)).get(), getBadgeByCampaignId(Integer.valueOf(campaignId)).get());

    if (userAvailableForCampaign(accountId, Integer.valueOf(campaignId))) {
      campaignResponse.setBadge(Badge.builder().build());
    }
    return campaignResponse;
  }

  @Override
  public boolean updateCampaignStatus(int accountId, String campaignId, CampaignStatus status) {
    if (!getRoleByAccountId(accountId).equals(Role.SELLER)) {
      throw new ApplicationContextException("You do not have permission for this operation");
    }

    CampaignEntity campaignEntity = campaignRepository.findById(Integer.valueOf(campaignId)).get();
    campaignEntity.setStatus(CampaignStatus.valueOf(status.name()));
    campaignRepository.save(campaignEntity);
    return true;
  }

  @Override
  public List<CampaignSummary> getCampaignList(int accountId, String sellerId) {
    List<CampaignEntity> campaignEntities = campaignRepository
        .findBySellerIdAndStatusEquals(Integer.valueOf(sellerId), CampaignStatus.ACTIVE).get();

    List<CampaignSummary> campaignSummaries = new ArrayList<>();
    campaignEntities
        .forEach(campaignEntity ->
            campaignSummaries
                .add(prepareCampaignEntityToList(accountId, campaignEntity)));

    return campaignSummaries;
  }
}
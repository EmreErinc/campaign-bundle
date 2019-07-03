package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.response.CampaignResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class CampaignServiceImpl implements CampaignService, BaseService {
  private CampaignRepository campaignRepository;

  @Autowired
  public CampaignServiceImpl(CampaignRepository campaignRepository) {
    this.campaignRepository = campaignRepository;
  }

  @Override
  public CampaignResponse addCampaign(String accountId, AddCampaignRequest request) {
    //TODO yetki kontrolü eklencek
    //TODO ürüne ait başka kampanya var mı yok mu

    String sellerId = getSellerIdByAccountId(accountId).get();

    return Converters
        .campaignEntityToCampaignResponse(
            campaignRepository
                .save(Converters
                    .addCampaignRequestToCampaignEntity(request, sellerId)));
  }

  @Override
  public CampaignResponse getCampaign(String accountId, String campaignId) {
    //TODO kullanıcı campaign status sorgulanacak

    return Converters
        .campaignEntityToCampaignResponse(
            campaignRepository
                .findById(campaignId).get());
  }

  @Override
  public boolean updateCampaignStatus(String accountId, String campaignId, CampaignStatus status) {
    //TODO kullanıcı yetki durumu sorgulanacak

    campaignRepository.updateCampaign(status, campaignId);
    return true;
  }

  @Override
  public List<CampaignSummary> getCampaignList(String accountId, String sellerId) {
    //TODO kullanıcı campaign status sorgulanacak

    return Converters
        .campaignEntitiesToCampaignSummaries(
            campaignRepository
                .findBySellerId(sellerId).get());
  }

  public boolean userAvailableForCampaign(String accountId, String campaignId){
    //TODO DO THIS IMMEDIATELY
    return false;
  }

}

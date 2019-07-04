package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.response.CampaignResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.repository.AccountRepository;
import com.finartz.intern.campaignlogic.repository.CampaignRepository;
import com.finartz.intern.campaignlogic.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampaignServiceImpl extends BaseServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;

  @Autowired
  public CampaignServiceImpl(CampaignRepository campaignRepository,
                             AccountRepository accountRepository,
                             SellerRepository sellerRepository) {
    super(accountRepository, sellerRepository);
    this.campaignRepository = campaignRepository;
  }

  @Override
  public CampaignResponse addCampaign(int accountId, AddCampaignRequest request) {
    //TODO yetki kontrolü eklencek
    //TODO ürüne ait başka kampanya var mı yok mu

    Integer sellerId = getSellerIdByAccountId(accountId).get();

    return Converters
        .campaignEntityToCampaignResponse(
            campaignRepository
                .save(Converters
                    .addCampaignRequestToCampaignEntity(request, sellerId)));
  }

  @Override
  public CampaignResponse getCampaign(int accountId, String campaignId) {
    //TODO kullanıcı campaign status sorgulanacak

    return Converters
        .campaignEntityToCampaignResponse(
            campaignRepository
                .findById(Integer.valueOf(campaignId)).get());
  }

  @Override
  public boolean updateCampaignStatus(int accountId, String campaignId, CampaignStatus status) {
    //TODO kullanıcı yetki durumu sorgulanacak

    //campaignRepository.updateCampaign(status, campaignId);
    return true;
  }

  @Override
  public List<CampaignSummary> getCampaignList(int accountId, String sellerId) {
    //TODO kullanıcı campaign status sorgulanacak

    return Converters
        .campaignEntitiesToCampaignSummaries(
            campaignRepository
                .findBySellerId(Integer.valueOf(sellerId)).get());
  }

  public boolean userAvailableForCampaign(int accountId, String campaignId) {
    //TODO DO THIS IMMEDIATELY
    return false;
  }

}

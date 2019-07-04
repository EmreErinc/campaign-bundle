package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.response.CampaignResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;

import java.util.List;

public interface CampaignService {
  CampaignResponse addCampaign(int accountId, AddCampaignRequest request);

  CampaignResponse getCampaign(int accountId, String campaignId);

  boolean updateCampaignStatus(int accountId, String campaignId, CampaignStatus status);

  List<CampaignSummary> getCampaignList(int accountId, String sellerId);
}

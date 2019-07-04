package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.response.CampaignResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.model.value.CampaignSummary;
import com.finartz.intern.campaignlogic.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/campaign")
public class CampaignController extends BaseController {
  private final CampaignService campaignService;

  @Autowired
  public CampaignController(CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @PostMapping
  public CampaignResponse addCampaign(@RequestHeader HttpHeaders headers, @RequestBody @Valid AddCampaignRequest request) {
    return campaignService.addCampaign(getAccountIdFromHeader(headers), request);
  }

  @GetMapping("/{campaignId}/cancel")
  public boolean cancelCampaign(@RequestHeader HttpHeaders headers, @PathVariable String campaignId) {
    return campaignService.updateCampaignStatus(getAccountIdFromHeader(headers), campaignId, CampaignStatus.CANCELED);
  }

  @GetMapping("/{campaignId}/active")
  public boolean activeCampaign(@RequestHeader HttpHeaders headers, @PathVariable String campaignId) {
    return campaignService.updateCampaignStatus(getAccountIdFromHeader(headers), campaignId, CampaignStatus.ACTIVE);
  }

  @GetMapping("/{campaignId}")
  public CampaignResponse getCampaign(@RequestHeader HttpHeaders headers, @PathVariable String campaignId) {
    return campaignService.getCampaign(getAccountIdFromHeader(headers), campaignId);
  }

  @GetMapping("/seller/{sellerId}")
  public List<CampaignSummary> getSellerCampaigns(@RequestHeader HttpHeaders headers, @PathVariable String sellerId) {
    return campaignService.getCampaignList(getAccountIdFromHeader(headers), sellerId);
  }
}

package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campaign")
public class CampaignController extends BaseController {
  private final CampaignService campaignService;

  @Autowired
  public CampaignController(CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @PostMapping
  public boolean addCampaign(@RequestHeader HttpHeaders headers, @RequestBody AddCampaignRequest request){
    return false;
  }
}

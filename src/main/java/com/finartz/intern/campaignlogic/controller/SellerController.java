package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller")
public class SellerController extends BaseController{
  private SellerService sellerService;

  @Autowired
  public SellerController(SellerService sellerService) {
    this.sellerService = sellerService;
  }

  @PostMapping
  public SellerResponse addSeller(@RequestHeader HttpHeaders headers, AddSellerRequest request){
    return sellerService.addSeller(getAccountIdFromHeader(headers), request);
  }

  @GetMapping("/{sellerId}")
  public SellerResponse getSeller(@PathVariable String sellerId){
    return sellerService.getSeller(sellerId);
  }
}

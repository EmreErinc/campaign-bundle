package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.response.SaleResponse;
import com.finartz.intern.campaignlogic.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sale")
public class SalesController extends BaseController {
  private SalesService salesService;

  @Autowired
  public SalesController(SalesService salesService) {
    this.salesService = salesService;
  }

  @PostMapping
  public SaleResponse addSale(@RequestHeader HttpHeaders headers) {
    return salesService.addSale(getAccountIdFromHeader(headers), getCartIdFromHeader(headers));
  }
}

package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.AddStockRequest;
import com.finartz.intern.campaignlogic.model.response.StockResponse;
import com.finartz.intern.campaignlogic.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock")
public class StockController extends BaseController{
  private StockService stockService;

  @Autowired
  public StockController(StockService stockService) {
    this.stockService = stockService;
  }

  @PostMapping
  public StockResponse addStock(@RequestBody AddStockRequest request){
    return stockService.addStock(request);
  }

  @GetMapping("/{itemId}")
  public StockResponse getStock(@PathVariable String itemId){
    return stockService.getStockCount(itemId);
  }
}

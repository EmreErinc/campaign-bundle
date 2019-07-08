package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddStockRequest;
import com.finartz.intern.campaignlogic.model.response.StockResponse;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl extends BaseServiceImpl implements StockService {
  private ItemRepository itemRepository;

  @Autowired
  public StockServiceImpl(ItemRepository itemRepository,
                          CartRepository cartRepository,
                          SellerRepository sellerRepository,
                          AccountRepository accountRepository,
                          CampaignRepository campaignRepository,
                          SalesRepository salesRepository) {
    super(accountRepository, sellerRepository, campaignRepository, itemRepository, salesRepository, cartRepository);
    this.itemRepository = itemRepository;
  }

  @Override
  public StockResponse addStock(AddStockRequest request) {
    int currentStock = itemRepository.findById(request.getItemId()).get().getStock();
    int newStock = currentStock + request.getStock();
    itemRepository.addStock(newStock, request.getItemId());
    return StockResponse.builder()
        .stock(itemRepository.findById(request.getItemId()).get().getStock().toString())
        .build();
  }

  @Override
  public StockResponse getStockCount(String itemId) {
    return StockResponse.builder()
        .stock(getItemStock(Integer.valueOf(itemId)).toString())
        .build();
  }
}

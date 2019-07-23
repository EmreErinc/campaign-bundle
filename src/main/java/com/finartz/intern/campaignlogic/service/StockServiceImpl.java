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
                          SalesRepository salesRepository,
                          VariantRepository variantRepository,
                          VariantSpecRepository variantSpecRepository,
                          SpecDataRepository specDataRepository,
                          SpecDetailRepository specDetailRepository) {
    super(accountRepository,
        sellerRepository,
        campaignRepository,
        itemRepository,
        salesRepository,
        cartRepository,
        variantRepository,
        variantSpecRepository,
        specDataRepository,
        specDetailRepository);
    this.itemRepository = itemRepository;
  }

  @Override
  public StockResponse addStock(AddStockRequest request) {
    int currentStock = getItemEntity(request.getProductId()).getStock();
    itemRepository.addStock(request.getStock(), request.getProductId());
    return StockResponse.builder()
        .stock(currentStock + request.getStock())
        .build();
  }

  @Override
  public StockResponse getStockCount(String itemId) {
    return StockResponse.builder()
        .stock(getProductStock(Integer.valueOf(itemId)))
        .build();
  }
}

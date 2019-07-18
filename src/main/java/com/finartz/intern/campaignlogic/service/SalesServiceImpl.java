package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.SalesEntity;
import com.finartz.intern.campaignlogic.model.response.SaleResponse;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesServiceImpl extends BaseServiceImpl implements SalesService {
  private SalesRepository salesRepository;

  @Autowired
  public SalesServiceImpl(CartRepository cartRepository,
                          SellerRepository sellerRepository,
                          AccountRepository accountRepository,
                          CampaignRepository campaignRepository,
                          ItemRepository itemRepository,
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
    this.salesRepository = salesRepository;
  }

  @Override
  public SaleResponse addSale(int accountId, String cartId) {
    List<Integer> saleIds = new ArrayList<>();
    List<SalesEntity> salesEntities = new ArrayList<>();
    CartEntity cartEntity = getCartEntityById(cartId);

    cartEntity
        .getCartItems()
        .forEach(cartItem -> {
          SalesEntity sale = SalesEntity.builder()
              .itemId(cartItem.getItemId())
              .ownerId(accountId)
              .saleCount(cartItem.getSaleCount())
              .price(cartItem.getPrice())
              .soldAt(Instant.now().toEpochMilli())
              .build();
          if (cartItem.getHasCampaign()) {
            sale.setGiftCount(cartItem.getCampaignParams().getActualGiftCount());
          }
          if (cartItem.getHasVariant()) {
            sale.setVariantId(cartItem.getVariant().getId());
          }
          salesEntities.add(salesRepository.save(sale));
        });

    salesEntities
        .forEach(salesEntity -> saleIds.add(salesEntity.getId()));

    saveAsSoldCart(cartEntity);

    return SaleResponse.builder().saleIds(saleIds).build();
  }
}

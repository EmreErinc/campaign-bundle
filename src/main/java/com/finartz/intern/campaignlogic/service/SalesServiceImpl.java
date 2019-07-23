package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.SalesEntity;
import com.finartz.intern.campaignlogic.model.response.ControlResponse;
import com.finartz.intern.campaignlogic.model.response.SaleResponse;
import com.finartz.intern.campaignlogic.model.value.Messages;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SalesServiceImpl extends BaseServiceImpl implements SalesService {
  private SalesRepository salesRepository;
  private CartServiceImpl cartServiceImpl;

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
                          SpecDetailRepository specDetailRepository,
                          CartServiceImpl cartServiceImpl) {
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
    this.cartServiceImpl = cartServiceImpl;
    this.salesRepository = salesRepository;
  }

  @Override
  public SaleResponse addSale(int accountId, String cartId) {
    List<Integer> saleIds = new ArrayList<>();
    List<SalesEntity> salesEntities = new ArrayList<>();
    CartEntity cartEntity = getCartEntityById(cartId);

    ControlResponse unfitCartItems = getUnfitCartItems(cartEntity);
    if (!unfitCartItems.getCartControlResponses().isEmpty()) {
      unfitCartItems
          .getCartControlResponses()
          .forEach(cartControlResponse -> cartServiceImpl.recalculateCartItems(accountId, cartId, cartControlResponse));

      throw new ApplicationContextException(Messages.ONE_OR_MORE_PRODUCT_ITEM_UNFIT.getValue());
    }

    cartEntity
        .getCartItems()
        .forEach(cartItem -> {
          SalesEntity sale = SalesEntity.builder()
              .productId(cartItem.getProductId())
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
          sale.setVariantId(cartItem.getHasVariant() ? cartItem.getVariant().getId() : 0);
          boolean addition = salesEntities.add(salesRepository.save(sale));
          if (addition) {
            int soldCount = sale.getSaleCount() + (sale.getGiftCount() == null ? 0 : sale.getGiftCount());
            decreaseItemStock(cartItem.getProductId(), cartItem.getVariant() == null ? Optional.empty() : Optional.of(cartItem.getVariant().getId()), soldCount);
          }
        });

    salesEntities
        .forEach(salesEntity -> saleIds.add(salesEntity.getId()));

    saveAsSoldCart(cartEntity);

    return SaleResponse.builder().saleIds(saleIds).build();
  }
}
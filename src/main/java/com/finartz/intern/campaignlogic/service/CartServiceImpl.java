package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CartItemResult;
import com.finartz.intern.campaignlogic.model.value.CartItemResultWithCampaign;
import com.finartz.intern.campaignlogic.repository.AccountRepository;
import com.finartz.intern.campaignlogic.repository.CampaignRepository;
import com.finartz.intern.campaignlogic.repository.CartRepository;
import com.finartz.intern.campaignlogic.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class CartServiceImpl extends BaseServiceImpl implements CartService {
  private final CartRepository cartRepository;

  @Autowired
  public CartServiceImpl(CartRepository cartRepository,
                         SellerRepository sellerRepository,
                         AccountRepository accountRepository,
                         CampaignRepository campaignRepository) {
    super(accountRepository, sellerRepository, campaignRepository);
    this.cartRepository = cartRepository;
  }

  @Override
  public boolean addToCart(int accountId, String cartId, String itemId, String count) {
    CartEntity cartEntity = cartRepository.findCart(cartId).get();

    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(Integer.valueOf(itemId));

    optionalCampaignEntity.filter(campaignEntity -> )

    optionalCampaignEntity.ifPresent(campaignEntity ->
        cartEntity.getItemList().add(mapCampaignExists(campaignEntity, itemId, count));
    );

    optionalCampaignEntity
        .map(campaignEntity -> cartEntity.getItemList().add(mapCampaignExists(campaignEntity, itemId, count)))
        .orElse(mapCampaignNotExists(optionalCampaignEntity.get(), itemId, count));
    /*optionalCampaignEntity.flatMap(campaignEntity -> {
      return CartItemResult.builder()
          .itemId(Integer.valueOf(itemId))
          .sellerId(campaignEntity.getSellerId())
          .saleCount(Integer.valueOf(count))
          .addedAt(Instant.now().toEpochMilli())
          .build();
    });*/


    //optionalCampaignEntity.map(Optional::of).orElse(() -> mapCampaignNotExists(optionalCampaignEntity.get(), itemId, count))


    return false;
  }

  private CartItemResult mapCampaignNotExists(CampaignEntity campaignEntity, String itemId, String count) {
    return CartItemResult.builder()
        .itemId(Integer.valueOf(itemId))
        .sellerId(campaignEntity.getSellerId())
        .saleCount(Integer.valueOf(count))
        .addedAt(Instant.now().toEpochMilli())
        .build();
  }

  private CartItemResultWithCampaign mapCampaignExists(CampaignEntity campaignEntity, String itemId, String count) {

    //TODO will calculate
    int expectedGiftCount = 0;

    return CartItemResultWithCampaign.builder()
        .expectedGiftCount(expectedGiftCount)
        .badge(Badge.builder()
            .requirement(campaignEntity.getRequirementCount())
            .gift(campaignEntity.getExpectedGiftCount())
            .build())
        .cartItemResult(mapCampaignNotExists(campaignEntity, itemId, count))
        .build();
  }

  @Override
  public boolean removeFromCart(int accountId, String cartId, String itemId) {
    return false;
  }

  @Override
  public boolean incrementItem(int accountId, String cartId, String itemId) {
    return false;
  }

  @Override
  public boolean decrementItem(int accountId, String cartId, String itemId) {
    return false;
  }

  @Override
  public String createCart(int accountId) {
    return cartRepository
        .createCart(accountId).getId();
  }

  @Override
  public CartResponse getCart(String cartId) {
    return Converters
        .cartEntityToCartResponse(cartRepository.findCart(cartId).get());
  }
}

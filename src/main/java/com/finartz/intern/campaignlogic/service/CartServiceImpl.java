package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignParams;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Optionals;
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
                         CampaignRepository campaignRepository,
                         ItemRepository itemRepository,
                         SalesRepository salesRepository) {
    super(accountRepository, sellerRepository, campaignRepository, itemRepository, salesRepository);
    this.cartRepository = cartRepository;
  }

  @Override
  public boolean addToCart(int accountId, String cartId, String itemId, String count) {
    if (itemAvailability(accountId, cartId, Integer.valueOf(itemId))) {
      CartEntity cartEntity = addItemToCart(cartId, Integer.valueOf(itemId), Integer.valueOf(count));

      int itemOnCart = cartEntity
          .getCartItems()
          .stream()
          .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
          .findFirst()
          .get()
          .getSaleCount();


      Optional<CampaignEntity> campaignEntity = getCampaignByItemId(Integer.valueOf(itemId));
      if (!campaignEntity.isPresent()){
        CartEntity updatedCartEntity = cartRepository
            .updateCart(CartEntity.builder()
                .id(cartEntity.getId())
                .accountId(accountId)
                .cartItems(cartEntity.getCartItems()).build());
        return true;
      }

      int giftCount = calculateGiftCount(campaignEntity.get(), itemOnCart);

      if (stockIsAvailable(Integer.valueOf(itemId), giftCount + itemOnCart)) {
        CampaignParams campaignParams = CampaignParams.builder()
            .expectedGiftCount(giftCount)
            .badge(Badge.builder()
                .requirement(campaignEntity.get().getRequirementCount())
                .gift(campaignEntity.get().getExpectedGiftCount())
                .build())
            .totalItemCount(giftCount + itemOnCart)
            .build();

        /*cartEntity
            .getCartItems()
            .stream()
            .filter(cartItem -> cartItem.getItemId().equals(itemId))
            .findFirst()
            .get()
            .setAdditionalParams(campaignParams);*/

        cartEntity
            .getCartItems()
            .stream()
            .filter(cartItem -> cartItem.getItemId().equals(itemId))
            .findFirst()
            .get().setAdditionalParams(campaignParams);

        CartEntity updatedCartEntity = cartRepository
            .updateCart(CartEntity.builder()
                .id(cartEntity.getId())
                .accountId(accountId)
                .cartItems(cartEntity.getCartItems()).build());
      }
      return true;
    }
    return false;
  }

  private boolean itemAvailability(int accountId, String cartId, int itemId) {
    //TODO hata mesajları dönmeli

    if (!campaignLimitIsAvailable(accountId, itemId).isPresent()) {
      //kampanyadan yararlanma limiti yetersiz
      return false;
    }

    return cartLimitAvailability(cartId, itemId);
  }

  private boolean cartLimitAvailability(String cartId, int itemId) {
    CartEntity cartEntity = cartRepository.findCart(cartId).get();
    int cartLimit = getCampaignCartLimit(itemId);

    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
        .findFirst();

    return optionalCartItem
        .filter(cartItem -> cartItem.getHasCampaign().equals(false) || cartItem.getSaleCount() <= cartLimit)
        .isPresent();
  }

  private int calculateGiftCount(CampaignEntity campaignEntity, int saleCount) {
    int requirementCount = campaignEntity.getRequirementCount();
    int quotient = saleCount / requirementCount;

    return quotient * campaignEntity.getExpectedGiftCount();
  }

  private CartEntity addItemToCart(String cartId, int itemId, int count) {
    CartEntity cartEntity = cartRepository.findCart(cartId).get();
    int sellerId = getSellerIdByItemId(itemId).get();
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(itemId);

    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
        .findFirst();

    Optionals.ifPresentOrElse(
        optionalCartItem,
        cartItem ->
            Optionals.ifPresentOrElse(
                optionalCampaignEntity,
                campaignEntity ->
                    cartEntity
                        .getCartItems()
                        .stream()
                        .filter(item -> item.getHasCampaign().equals(true) && item.getItemId().equals(itemId))
                        .findFirst()
                        .get()
                        .setSaleCount(count + optionalCartItem.get().getSaleCount()),
                () ->
                    cartEntity
                        .getCartItems()
                        .stream()
                        .filter(item -> item.getHasCampaign().equals(false) && item.getItemId().equals(itemId))
                        .findFirst()
                        .get()
                        .setSaleCount(count + optionalCartItem.get().getSaleCount())
            ),
        () ->
            Optionals.ifPresentOrElse(
                optionalCampaignEntity,
                campaignEntity -> cartEntity
                    .getCartItems()
                    .add(mapNewCampaignCartItem(sellerId, itemId, count)),
                () -> cartEntity
                    .getCartItems()
                    .add(mapNewCartItem(sellerId, itemId, count)))

    );
    return cartEntity;
  }


  private CartItem mapNewCartItem(int sellerId, int itemId, int count) {
    return CartItem.builder()
        .itemId(itemId)
        .sellerId(sellerId)
        .saleCount(count)
        .addedAt(Instant.now().toEpochMilli())
        .hasCampaign(false)
        .build();
  }

  private CartItem mapNewCampaignCartItem(int sellerId, int itemId, int count) {
    return CartItem.builder()
        .itemId(itemId)
        .sellerId(sellerId)
        .saleCount(count)
        .addedAt(Instant.now().toEpochMilli())
        .hasCampaign(true)
        .build();
  }

  @Override
  public boolean removeFromCart(int accountId, String cartId, String itemId) {
    return false;
  }

  @Override
  public boolean incrementItem(int accountId, String cartId, String itemId) {
    //TODO add update at

    if (itemAvailability(accountId, cartId, Integer.valueOf(itemId))) {
      CartEntity cartEntity = addItemToCart(cartId, Integer.valueOf(itemId), 1);
      CartEntity updatedCartEntity = cartRepository
          .updateCart(CartEntity.builder()
              .id(cartEntity.getId())
              .accountId(accountId)
              .cartItems(cartEntity.getCartItems()).build());
      return true;
    }
    return false;
  }

  @Override
  public boolean decrementItem(int accountId, String cartId, String itemId) {
    //TODO add update at

    if (itemAvailability(accountId, cartId, Integer.valueOf(itemId))) {
      CartEntity cartEntity = addItemToCart(cartId, Integer.valueOf(itemId), -1);
      CartEntity updatedCartEntity = cartRepository
          .updateCart(CartEntity.builder()
              .id(cartEntity.getId())
              .accountId(accountId)
              .cartItems(cartEntity.getCartItems()).build());
      return true;
    }
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

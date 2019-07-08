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
import org.springframework.context.ApplicationContextException;
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
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(Integer.valueOf(itemId));

    //checks campaign is available
    if (optionalCampaignEntity.isPresent() && campaignIsAvailable(Integer.valueOf(itemId))) {
      if (itemAvailability(accountId, cartId, Integer.valueOf(itemId), Integer.valueOf(count))) {
        CartEntity cartEntity = addItemToCartWithCampaign(optionalCampaignEntity.get(), cartId, Integer.valueOf(itemId), Integer.valueOf(count));
        CartEntity updatedCartEntity = updateCartEntity(cartEntity, accountId, Integer.valueOf(itemId));
        return true;
      }
    } else {
      CartEntity cartEntity = addItemToCart(cartId, Integer.valueOf(itemId), Integer.valueOf(count));
      CartEntity updatedCartEntity = updateCartEntity(cartEntity, accountId, Integer.valueOf(itemId));
      return true;
    }
    return false;
  }

  private boolean itemAvailability(int accountId, String cartId, int itemId, int itemCount) {
    //checks campaign limit availability
    if (!campaignLimitIsAvailable(accountId, itemId).isPresent()) {
      throw new ApplicationContextException("Kampanya Limitinizi Doldurdunuz");
    }
    if (!cartLimitAvailability(cartId, itemId, itemCount)) {
      throw new ApplicationContextException("Sepet Limitinizi Doldurdunuz");
    }
    return true;
  }

  private CampaignParams prepareCampaignParams(CampaignEntity campaignEntity, int giftCount, int itemCount) {
    return CampaignParams.builder()
        .expectedGiftCount(giftCount)
        .badge(Badge.builder()
            .requirement(campaignEntity.getRequirementCount())
            .gift(campaignEntity.getExpectedGiftCount())
            .build())
        .totalItemCount(itemCount)
        .build();
  }

  private CartEntity updateCartEntity(CartEntity cartEntity, int accountId, int itemId) {

    CartItem updatedItem = cartEntity.getCartItems().stream().filter(cartItem -> cartItem.getItemId().equals(itemId)).findFirst().get();
    int updatedItemIndex = cartEntity.getCartItems().indexOf(updatedItem);
    cartEntity.getCartItems().remove(updatedItemIndex);
    updatedItem.builder().updatedAt(Instant.now().toEpochMilli()).build();
    cartEntity.getCartItems().add(updatedItemIndex, updatedItem);

    return cartRepository
        .updateCart(CartEntity.builder()
            .id(cartEntity.getId())
            .accountId(accountId)
            .cartItems(cartEntity.getCartItems()).build());
  }

  private boolean cartLimitAvailability(String cartId, int itemId, int itemCount) {
    CartEntity cartEntity = findCart(cartId);
    int cartLimit = getCampaignCartLimit(itemId);

    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
        .findFirst();

    int saleCount = 0;
    if (optionalCartItem.isPresent()) {
      saleCount = optionalCartItem.get().getSaleCount();
    }
    return saleCount + itemCount <= cartLimit;
  }

  private int calculateGiftCount(CampaignEntity campaignEntity, int saleCount) {
    int requirementCount = campaignEntity.getRequirementCount();
    int quotient = saleCount / requirementCount;
    return quotient * campaignEntity.getExpectedGiftCount();
  }

  private CartEntity addItemToCartWithCampaign(CampaignEntity campaignEntity, String cartId, int itemId, int count) {
    CartEntity cartEntity = findCart(cartId);
    int sellerId = getSellerIdByItemId(itemId).get();

    //item already on cart or not
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
        .findFirst();

    Optionals.ifPresentOrElse(
        optionalCartItem,
        //item found on cart
        cartItem -> {
          int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
          int itemOnCart = cartItem.getSaleCount();
          int giftCount = calculateGiftCount(campaignEntity, itemOnCart + count);

          if (campaignEntity.getCartLimit() > itemOnCart + count) {
            //TODO add one-by-one addition
          } else {
            if (stockIsAvailable(itemId, giftCount + itemOnCart + count)) {
              optionalCartItem
                  .map(item -> {
                    item.setSaleCount(count + optionalCartItem.get().getSaleCount());
                    item.setUpdatedAt(Instant.now().toEpochMilli());
                    item.setCampaignParams(prepareCampaignParams(campaignEntity, giftCount, itemOnCart + count));
                    return true;
                  });
              cartEntity.getCartItems().remove(itemIndex);
              cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
            }
          }
        },
        //item not found on cart
        () -> {
          if (campaignEntity.getCartLimit() <= count) {
            //TODO add one-by-one addition
          } else {
            int giftCount = calculateGiftCount(campaignEntity, count);

            //stock control for desired items
            if (stockIsAvailable(itemId, giftCount + count)) {
              cartEntity
                  .getCartItems()
                  .add(CartItem.builder()
                      .itemId(itemId)
                      .sellerId(sellerId)
                      .saleCount(count)
                      .addedAt(Instant.now().toEpochMilli())
                      .hasCampaign(true)
                      .campaignParams(prepareCampaignParams(campaignEntity, giftCount, count))
                      .build());
            }
          }
        }
    );
    return cartEntity;
  }

  private CartEntity addItemToCart(String cartId, int itemId, int count) {
    CartEntity cartEntity = findCart(cartId);
    int sellerId = getSellerIdByItemId(itemId).get();

    //item already on cart or not
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
        .findFirst();

    Optionals.ifPresentOrElse(
        optionalCartItem,
        //item found on cart
        cartItem -> {
          int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
          int itemOnCart = cartItem.getSaleCount();

          if (stockIsAvailable(itemId, itemOnCart + count)) {
            optionalCartItem
                .map(item -> {
                  item.setSaleCount(count + optionalCartItem.get().getSaleCount());
                  item.setUpdatedAt(Instant.now().toEpochMilli());
                  return true;
                });
            cartEntity.getCartItems().remove(itemIndex);
            cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
          }
        },
        //item not found on cart
        () -> {
          if (stockIsAvailable(itemId, count)) {
            cartEntity
                .getCartItems()
                .add(CartItem.builder()
                    .itemId(itemId)
                    .sellerId(sellerId)
                    .saleCount(count)
                    .addedAt(Instant.now().toEpochMilli())
                    .hasCampaign(false)
                    .build());
          }
        }
    );
    return cartEntity;
  }

  @Override
  public boolean removeFromCart(int accountId, String cartId, String itemId) {
    CartEntity cartEntity = findCart(cartId);
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
        .findFirst();
    if (!optionalCartItem.isPresent()) {
      throw new ApplicationContextException("Ürün sepette bulunamadı.");
    }
    return cartEntity.getCartItems().remove(optionalCartItem.get());
  }

  @Override
  public boolean incrementItem(int accountId, String cartId, String itemId) {
    if (itemAvailability(accountId, cartId, Integer.valueOf(itemId), 1)) {
      CartEntity cartEntity = addItemToCart(cartId, Integer.valueOf(itemId), 1);
      CartEntity updatedCartEntity = updateCartEntity(cartEntity, accountId, Integer.valueOf(itemId));
      return true;
    }
    return false;
  }

  @Override
  public boolean decrementItem(int accountId, String cartId, String itemId) {
    if (itemAvailability(accountId, cartId, Integer.valueOf(itemId), -1)) {
      CartEntity cartEntity = addItemToCart(cartId, Integer.valueOf(itemId), -1);
      CartEntity updatedCartEntity = updateCartEntity(cartEntity, accountId, Integer.valueOf(itemId));
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
        .cartEntityToCartResponse(findCart(cartId));
  }

  private CartEntity findCart(String cartId) {
    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (!optionalCartEntity.isPresent()) {
      throw new ApplicationContextException("Sepet Bulunamadı.");
    }
    return optionalCartEntity.get();
  }
}

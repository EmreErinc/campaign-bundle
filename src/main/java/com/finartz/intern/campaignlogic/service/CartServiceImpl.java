package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.value.Badge;
import com.finartz.intern.campaignlogic.model.value.CampaignParams;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.finartz.intern.campaignlogic.model.value.SuitableSaleAndGiftCount;
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
    super(accountRepository, sellerRepository, campaignRepository, itemRepository, salesRepository, cartRepository);
    this.cartRepository = cartRepository;
  }

  @Override
  public CartResponse addToCart(int accountId, String cartId, String itemId, String count) {
    return updateCart(accountId, cartId, itemId, count);
  }

  @Override
  public CartResponse getCart(String cartId) {
    return Converters
        .cartEntityToCartResponse(findCart(cartId));
  }

  @Override
  public CartResponse removeFromCart(int accountId, String cartId, String itemId) {
    CartEntity cartEntity = findCart(cartId);
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
        .findFirst();
    if (!optionalCartItem.isPresent()) {
      throw new ApplicationContextException("Ürün sepette bulunamadı.");
    }
    cartEntity.getCartItems().remove(optionalCartItem.get());
    cartRepository.updateCart(cartEntity);
    return getCart(cartId);
  }

  @Override
  public CartResponse incrementItem(int accountId, String cartId, String itemId) {
    if (itemAvailability(accountId, cartId, Integer.valueOf(itemId), 1)) {
      updateCart(accountId, cartId, itemId, String.valueOf(1));
    }
    return getCart(cartId);
  }

  @Override
  public CartResponse decrementItem(int accountId, String cartId, String itemId) {
    if (itemOnCart(cartId, Integer.valueOf(itemId)) && itemAvailability(accountId, cartId, Integer.valueOf(itemId), -1)) {
      updateCart(accountId, cartId, itemId, String.valueOf(-1));
      if (getItemCountOnCart(cartId, Integer.valueOf(itemId)).equals(0)) {
        removeFromCart(accountId, cartId, itemId);
      }
    }
    return getCart(cartId);
  }

  @Override
  public String createCart(int accountId) {
    return cartRepository
        .createCart(accountId).getId();
  }

  private CartResponse updateCart(int accountId, String cartId, String itemId, String count) {
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(Integer.valueOf(itemId));

    //checks campaign is available
    if (optionalCampaignEntity.isPresent()) {
      if (itemAvailability(accountId, cartId, Integer.valueOf(itemId), Integer.valueOf(count))) {
        CartEntity cartEntity = addItemToCartWithCampaign(optionalCampaignEntity.get(), cartId, Integer.valueOf(itemId), Integer.valueOf(count));
        updateCartEntity(cartEntity, accountId, Integer.valueOf(itemId));
      }
    } else {
      CartEntity cartEntity = addItemToCart(cartId, Integer.valueOf(itemId), Integer.valueOf(count));
      updateCartEntity(cartEntity, accountId, Integer.valueOf(itemId));
    }
    return getCart(cartId);
  }

  private boolean itemAvailability(int accountId, String cartId, int itemId, int itemCount) {
    //checks campaign is available on current date
    if (!campaignIsAvailable(itemId)) {
      //throw new ApplicationContextException("Kampanya Süresi Doldu.");
      return false;
    }

    if (!itemOnCampaign(itemId) && itemOnCart(cartId, itemId)){
      //  throw new ApplicationContextException("Ürün Sepette Bulunamadı.");
      return false;
    }

    if (!campaignLimitIsAvailableForAccount(accountId, itemId)) {
      //throw new ApplicationContextException("Kampanya Limitinizi Doldurdunuz");
      return false;
    }

    if (!cartLimitAvailability(cartId, itemId, itemCount)) {
      //  throw new ApplicationContextException("Sepet Limitinizi Doldurdunuz");
      return false;
    }

    return true;
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

  private CartEntity addItemToCartWithCampaign(CampaignEntity campaignEntity, String cartId, int itemId, int desiredSaleCount) {
    CartEntity cartEntity = findCart(cartId);
    int sellerId = getSellerIdByItemId(itemId).get();
    double itemPrice = getItemPrice(itemId);

    //item already on cart or not
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
        .findFirst();

    Optionals.ifPresentOrElse(
        optionalCartItem,
        cartItem -> { //item found on cart
          int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
          int itemOnCart = cartItem.getSaleCount();
          int updatedSaleCount = desiredSaleCount + itemOnCart;

          int giftCount = calculateGiftCount(campaignEntity, updatedSaleCount);
          boolean stockIsAvailable = stockIsAvailable(itemId, giftCount + updatedSaleCount);
          //check stock availability
          if (stockIsAvailable && updatedSaleCount >= 0) {
            optionalCartItem
                .ifPresent(item -> {
                  item.setPrice(updatedSaleCount * itemPrice);
                  item.setSaleCount(updatedSaleCount);
                  item.setUpdatedAt(Instant.now().toEpochMilli());
                  item.setCampaignParams(prepareCampaignParams(campaignEntity, giftCount, updatedSaleCount));
                });
            cartEntity.getCartItems().remove(itemIndex);
            cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
          } else { //stock is not available for direct addition
            optionalCartItem
                .ifPresent(item -> {
                  //describes which sale and gift count available for this item operation
                  SuitableSaleAndGiftCount suitableCount = addOneByOneToCart(campaignEntity, itemOnCart);

                  item.setPrice(suitableCount.getSaleCount() * itemPrice);
                  item.setSaleCount(suitableCount.getSaleCount());
                  item.setUpdatedAt(Instant.now().toEpochMilli());
                  item.setCampaignParams(prepareCampaignParams(campaignEntity, suitableCount.getGiftCount(), suitableCount.getSaleCount()));
                });
            cartEntity.getCartItems().remove(itemIndex);
            cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
          }
        },
        () -> { //item not found on cart
          int giftCount = calculateGiftCount(campaignEntity, desiredSaleCount);
          boolean stockIsAvailable = stockIsAvailable(itemId, giftCount + desiredSaleCount);

          //check stock availability
          if (stockIsAvailable && desiredSaleCount > 0) {
            cartEntity
                .getCartItems()
                .add(CartItem.builder()
                    .itemId(itemId)
                    .sellerId(sellerId)
                    .saleCount(desiredSaleCount)
                    .addedAt(Instant.now().toEpochMilli())
                    .hasCampaign(true)
                    .campaignParams(prepareCampaignParams(campaignEntity, giftCount, desiredSaleCount))
                    .price(itemPrice * desiredSaleCount)
                    .build());
          } else { //if desired item count exceeds campaign cart limit
            SuitableSaleAndGiftCount suitableCount = addOneByOneToCart(campaignEntity, 0);
            cartEntity
                .getCartItems()
                .add(CartItem.builder()
                    .itemId(itemId)
                    .sellerId(sellerId)
                    .saleCount(suitableCount.getSaleCount())
                    .addedAt(Instant.now().toEpochMilli())
                    .hasCampaign(true)
                    .campaignParams(prepareCampaignParams(campaignEntity, suitableCount.getGiftCount(), suitableCount.getSaleCount()))
                    .price(itemPrice * suitableCount.getSaleCount())
                    .build());
          }
        }
    );
    return cartEntity;
  }

  private int calculateGiftCount(CampaignEntity campaignEntity, int saleCount) {
    int requirementCount = campaignEntity.getRequirementCount();
    int quotient = saleCount / requirementCount;
    int possibleGiftCount = quotient * campaignEntity.getExpectedGiftCount();

    if (possibleGiftCount > campaignEntity.getExpectedGiftCount()) {
      return campaignEntity.getExpectedGiftCount();
    }
    return possibleGiftCount;
  }

  private SuitableSaleAndGiftCount addOneByOneToCart(CampaignEntity campaignEntity, int itemOnCart) {
    int suitableSaleCount = 0;
    int suitableGiftCount;
    int itemStock = getItemStock(campaignEntity.getItemId());

    while (true) {
      suitableGiftCount = calculateGiftCount(campaignEntity, itemOnCart + suitableSaleCount);
      if (itemStock - (itemOnCart + suitableGiftCount + suitableSaleCount) == 0) {
        //if stock is not suits for this operation, calculate gift count for itemCart and (suitableSaleCount - 1)
        suitableGiftCount = calculateGiftCount(campaignEntity, itemOnCart + suitableSaleCount - 1);
        break;
      }
      suitableSaleCount += 1;
    }

    return SuitableSaleAndGiftCount.builder()
        .saleCount(suitableSaleCount + itemOnCart)
        .giftCount(suitableGiftCount)
        .build();
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

  private CartEntity addItemToCart(String cartId, int itemId, int desiredSaleCount) {
    CartEntity cartEntity = findCart(cartId);
    int sellerId = getSellerIdByItemId(itemId).get();
    double itemPrice = getItemPrice(itemId);

    //item already on cart or not
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(Integer.valueOf(itemId)))
        .findFirst();

    Optionals.ifPresentOrElse(
        optionalCartItem,
        cartItem -> { //item found on cart
          int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
          int itemOnCart = cartItem.getSaleCount();
          boolean stockIsAvailable = stockIsAvailable(itemId, itemOnCart + desiredSaleCount);

          if (stockIsAvailable) {
            cartEntity.getCartItems().remove(itemIndex);
            cartEntity.getCartItems().add(itemIndex, setCartItem(optionalCartItem, desiredSaleCount, itemPrice));
          } else {
            cartEntity.getCartItems().remove(itemIndex);
            int suitableSaleCount = addOneByOneToCart(itemId, itemOnCart);
            cartEntity.getCartItems().add(itemIndex, setCartItem(optionalCartItem, suitableSaleCount, itemPrice));
          }
        },
        () -> { //item not found on cart
          boolean stockIsAvailable = stockIsAvailable(itemId, desiredSaleCount);

          if (stockIsAvailable && desiredSaleCount > 0) {
            cartEntity
                .getCartItems()
                .add(CartItem.builder()
                    .itemId(itemId)
                    .sellerId(sellerId)
                    .saleCount(desiredSaleCount)
                    .addedAt(Instant.now().toEpochMilli())
                    .hasCampaign(false)
                    .price(itemPrice * desiredSaleCount)
                    .build());
          } else {
            int suitableSaleCount = addOneByOneToCart(itemId, 0);
            cartEntity
                .getCartItems()
                .add(CartItem.builder()
                    .itemId(itemId)
                    .sellerId(sellerId)
                    .saleCount(suitableSaleCount)
                    .addedAt(Instant.now().toEpochMilli())
                    .hasCampaign(false)
                    .price(itemPrice * (suitableSaleCount))
                    .build());
          }
        }
    );
    return cartEntity;
  }

  private CartItem setCartItem(Optional<CartItem> optionalCartItem, int desiredSaleCount, double itemPrice) {
    return optionalCartItem
        .map(item -> {
          item.setPrice((desiredSaleCount + optionalCartItem.get().getSaleCount()) * itemPrice);
          item.setSaleCount(desiredSaleCount + optionalCartItem.get().getSaleCount());
          item.setUpdatedAt(Instant.now().toEpochMilli());
          return item;
        }).get();
  }

  private Integer addOneByOneToCart(int itemId, int itemOnCart) {
    int suitableAddition = 0;
    int stock = getItemStock(itemId);

    while (true) {
      suitableAddition++;
      if (stock - (itemOnCart + suitableAddition) < 0) {
        suitableAddition--;
        break;
      }
    }
    return suitableAddition;
  }

  private void updateCartEntity(CartEntity cartEntity, int accountId, int itemId) {
    CartItem updatedItem = cartEntity.getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
        .findFirst()
        .get();
    int updatedItemIndex = cartEntity.getCartItems().indexOf(updatedItem);
    cartEntity.getCartItems().remove(updatedItemIndex);
    updatedItem.setUpdatedAt(Instant.now().toEpochMilli());
    cartEntity.getCartItems().add(updatedItemIndex, updatedItem);

    cartRepository
        .updateCart(CartEntity.builder()
            .id(cartEntity.getId())
            .accountId(accountId)
            .cartItems(cartEntity.getCartItems())
            .build());
  }

  private CartEntity findCart(String cartId) {
    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (!optionalCartEntity.isPresent()) {
      throw new ApplicationContextException("Sepet Bulunamadı.");
    }
    return optionalCartEntity.get();
  }
}
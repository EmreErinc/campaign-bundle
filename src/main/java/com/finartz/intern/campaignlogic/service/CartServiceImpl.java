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

  private CartResponse updateCart(int accountId, String cartId, String itemId, String count) {
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(Integer.valueOf(itemId));

    //checks campaign is available
    if (optionalCampaignEntity.isPresent() && campaignIsAvailable(Integer.valueOf(itemId))) {
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
    //checks campaign limit availability
    if (!campaignLimitIsAvailableForAccount(accountId, itemId).isPresent()) {
      throw new ApplicationContextException("Kampanya Limitinizi Doldurdunuz");
    }
    //if (itemOnCampaign(itemId) && !cartLimitAvailability(cartId, itemId, itemCount)) {
    //  throw new ApplicationContextException("Sepet Limitinizi Doldurdunuz");
    //}
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
        //item found on cart
        cartItem -> {
          int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
          int itemOnCart = cartItem.getSaleCount();

          //checks item on cart and desired sale count suitable for campaign criteria
          if (campaignEntity.getCartLimit() <= itemOnCart + desiredSaleCount && itemOnCart + desiredSaleCount >= 0) {
            //calculate suitable sale and gift count referenced with item on cart
            SuitableSaleAndGiftCount suitableCount = calculateSuitableItemCount(campaignEntity, itemOnCart);
            //prepares cart item
            optionalCartItem
                .map(item -> {
                  item.setPrice((suitableCount.getSaleCount() + optionalCartItem.get().getSaleCount()) * itemPrice);
                  item.setSaleCount(suitableCount.getSaleCount() + optionalCartItem.get().getSaleCount());
                  item.setUpdatedAt(Instant.now().toEpochMilli());
                  item.setCampaignParams(prepareCampaignParams(campaignEntity, suitableCount.getGiftCount(), itemOnCart + suitableCount.getSaleCount()));
                  return item;
                });
            cartEntity.getCartItems().remove(itemIndex);
            cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
          } else {
            //calculate gift count
            int giftCount = calculateGiftCount(campaignEntity, itemOnCart + desiredSaleCount);

            //checks max gift count exceeds or not
            //if ((cartItem.getCampaignParams().getExpectedGiftCount() + giftCount) <= campaignEntity.getExpectedGiftCount()){
              //verilebilecek max ürün verilir, desired sale kullanıcıya sunulur
            //}

            //checks stock is available
            if (stockIsAvailable(itemId, giftCount + itemOnCart + desiredSaleCount) && itemOnCart + desiredSaleCount >= 0) {
              //prepares cart item
              optionalCartItem
                  .map(item -> {
                    item.setPrice((desiredSaleCount + cartItem.getSaleCount()) * itemPrice);
                    item.setSaleCount(desiredSaleCount + optionalCartItem.get().getSaleCount());
                    item.setUpdatedAt(Instant.now().toEpochMilli());
                    item.setCampaignParams(prepareCampaignParams(campaignEntity, giftCount, itemOnCart + desiredSaleCount));
                    return item;
                  });
              cartEntity.getCartItems().remove(itemIndex);
              cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
            }
          }
        },
        //item not found on cart
        () -> {
          //if desired item count exceeds campaign cart limit
          if (campaignEntity.getCartLimit() <= desiredSaleCount && desiredSaleCount > 0) {
            SuitableSaleAndGiftCount suitableCount = calculateSuitableItemCount(campaignEntity, 0);
            //prepares new item
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
          } else {
            int giftCount = calculateGiftCount(campaignEntity, desiredSaleCount);

            //stock control for desired items
            if (stockIsAvailable(itemId, giftCount + desiredSaleCount) && desiredSaleCount > 0) {
              //prepares new item
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
            }
          }
        }
    );
    return cartEntity;
  }

  private SuitableSaleAndGiftCount calculateSuitableItemCount(CampaignEntity campaignEntity, int itemOnCart) {
    int suitableSaleCount = 0;
    int suitableGiftCount = 0;
    int itemStock = getItemStock(campaignEntity.getItemId());

    while (true) {
      //checks item count reach to limit or not
      if (campaignEntity.getCartLimit() >= itemOnCart + suitableSaleCount) {
        //calculate gift count for desired sale count
        suitableGiftCount = calculateGiftCount(campaignEntity, itemOnCart + suitableSaleCount);
        //checks stock is suitable for this operation
        if (!(itemStock - (itemOnCart + suitableGiftCount + suitableSaleCount) >= 0)) {
          //if stock is not suits for this operation, calculate gift count for itemCart and (suitableAddition - 1)
          suitableGiftCount = calculateGiftCount(campaignEntity, itemOnCart + suitableSaleCount - 1);
          break;
        }
      } else {
        break;
      }
      suitableSaleCount += 1;
    }

    return SuitableSaleAndGiftCount.builder()
        .saleCount(suitableSaleCount)
        .giftCount(suitableGiftCount)
        .build();
  }

  private int calculateGiftCount(CampaignEntity campaignEntity, int saleCount) {
    int requirementCount = campaignEntity.getRequirementCount();
    int quotient = saleCount / requirementCount;
    return quotient * campaignEntity.getExpectedGiftCount();
  }

  private CartEntity addItemToCart(String cartId, int itemId, int count) {
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
        //item found on cart
        cartItem -> {
          int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
          int itemOnCart = cartItem.getSaleCount();
          boolean stockIsAvailable = stockIsAvailable(itemId, itemOnCart + count);

          if (!stockIsAvailable) {
            CartItem updatedCartItem = updateCartItemUtils(optionalCartItem, calculateSuitableItemCount(itemId, itemOnCart), itemPrice);
            cartEntity.getCartItems().remove(itemIndex);
            cartEntity.getCartItems().add(itemIndex, updatedCartItem);
          } else if (itemOnCart + count >= 0) {
            CartItem updatedCartItem = updateCartItemUtils(optionalCartItem, count, itemPrice);
            cartEntity.getCartItems().remove(itemIndex);
            cartEntity.getCartItems().add(itemIndex, updatedCartItem);
          }
        },
        //item not found on cart
        () -> {
          boolean stockIsAvailable = stockIsAvailable(itemId, count);

          if (!stockIsAvailable) {
            int suitableItemCount = calculateSuitableItemCount(itemId, 0);
            cartEntity
                .getCartItems()
                .add(CartItem.builder()
                    .itemId(itemId)
                    .sellerId(sellerId)
                    .saleCount(suitableItemCount)
                    .addedAt(Instant.now().toEpochMilli())
                    .hasCampaign(false)
                    .price(itemPrice * (suitableItemCount))
                    .build());
          } else if (stockIsAvailable && count > 0) {
            cartEntity
                .getCartItems()
                .add(CartItem.builder()
                    .itemId(itemId)
                    .sellerId(sellerId)
                    .saleCount(count)
                    .addedAt(Instant.now().toEpochMilli())
                    .hasCampaign(false)
                    .price(itemPrice * (count))
                    .build());
          }
        }
    );
    return cartEntity;
  }

  private CartItem updateCartItemUtils(Optional<CartItem> optionalCartItem, int saleCount, double itemPrice) {
    return optionalCartItem
        .map(item -> {
          item.setPrice((saleCount + optionalCartItem.get().getSaleCount()) * itemPrice);
          item.setSaleCount(saleCount + optionalCartItem.get().getSaleCount());
          item.setUpdatedAt(Instant.now().toEpochMilli());
          return item;
        }).get();
  }

  private Integer calculateSuitableItemCount(int itemId, int itemOnCart) {
    int suitableSaleCount = 0;
    int stock = getItemStock(itemId);

    while (true) {
      suitableSaleCount++;
      if (stock - (itemOnCart + suitableSaleCount) < 0) {
        suitableSaleCount--;
        break;
      }
    }
    return suitableSaleCount;
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
    if (itemAvailability(accountId, cartId, Integer.valueOf(itemId), -1)) {
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
package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.request.AddItemToCartRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemDecrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemIncrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemRemoveRequest;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl extends BaseServiceImpl implements CartService {
  private final CartRepository cartRepository;

  @Autowired
  public CartServiceImpl(CartRepository cartRepository,
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
    this.cartRepository = cartRepository;
  }

  @Override
  public CartResponse addToCart(int accountId, String cartId, AddItemToCartRequest request) {
    return updateCart(accountId, cartId, request.getItemId(), request.getCount(), Optional.ofNullable(request.getVariantId()));
  }

  @Override
  public CartResponse getCart(String cartId) {
    return Converters
        .cartEntityToCartResponse(getCartEntityById(cartId));
  }

  @Override
  public CartResponse removeFromCart(int accountId, String cartId, CartItemRemoveRequest request) {
    CartEntity cartEntity = getCartEntityById(cartId);
    List<CartItem> collect = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(request.getItemId()))
        .collect(Collectors.toList());

    Optional<CartItem> optionalCartItem;
    if (collect.stream().anyMatch(cartItem -> cartItem.getHasVariant().equals(true))) {
      optionalCartItem = collect
          .stream()
          .filter(cartItem -> cartItem.getVariant().getId().equals(request.getVariantId()))
          .findFirst();
    } else {
      optionalCartItem = collect.stream().findFirst();
    }
    if (!optionalCartItem.isPresent()) {
      throw new ApplicationContextException("Ürün sepette bulunamadı.");
    }
    cartEntity.getCartItems().remove(optionalCartItem.get());
    cartRepository.updateCart(cartEntity);
    return getCart(cartId);
  }

  @Override
  public CartResponse incrementItem(int accountId, String cartId, CartItemIncrementRequest request) {
    if (isItemAvailable(accountId, cartId, request.getItemId(), 1)) {
      updateCart(accountId, cartId, request.getItemId(), 1, Optional.ofNullable(request.getVariantId()));
    }
    return getCart(cartId);
  }

  @Override
  public CartResponse decrementItem(int accountId, String cartId, CartItemDecrementRequest request) {
    if (isItemOnCart(cartId, request.getItemId())
        && isItemAvailable(accountId, cartId, request.getItemId(), -1)) {
      updateCart(accountId, cartId, request.getItemId(), -1, Optional.ofNullable(request.getVariantId()));
      if (getItemCountOnCart(cartId, request.getItemId()).equals(0)) {
        removeFromCart(accountId, cartId, CartItemRemoveRequest.builder()
            .itemId(request.getItemId())
            .count(-1)
            .variantId(request.getVariantId())
            .build());
      }
    }
    return getCart(cartId);
  }

  @Override
  public String createCart(int accountId) {
    return cartRepository
        .createCart(accountId).getId();
  }

  private CartResponse updateCart(int accountId, String cartId, int itemId, int count, Optional<Integer> variantId) {
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(itemId);

    //checks campaign is available
    if (optionalCampaignEntity.isPresent()
        && isItemAvailable(accountId, cartId, itemId, count)
        && isCampaignAvailableGetById(optionalCampaignEntity.get().getId())
        && atLeastOneAvailability(optionalCampaignEntity.get(), count, cartId)) {
      CartEntity cartEntity = addItemToCartWithCampaign(optionalCampaignEntity.get(), cartId, count, variantId.orElse(0));
      updateCartEntity(cartEntity, accountId, itemId);
    } else {
      CartEntity cartEntity = addItemToCartOrdinary(cartId, itemId, count, variantId.orElse(0));
      updateCartEntity(cartEntity, accountId, itemId);
    }
    return getCart(cartId);
  }

  private boolean isItemAvailable(int accountId, String cartId, int itemId, int itemCount) {
    if (!isCampaignLimitAvailableForAccount(accountId, itemId)) {
      return false;
    }

    if (!cartLimitAvailability(cartId, itemId, itemCount)) {
      return false;
    }
    return true;
  }

  private boolean cartLimitAvailability(String cartId, int itemId, int itemCount) {
    CartEntity cartEntity = getCartEntityById(cartId);
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByItemId(itemId);
    if (!optionalCampaignEntity.isPresent()) {
      return true;
    }

    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(itemId))
        .findFirst();

    int saleCount = 0;
    if (optionalCartItem.isPresent()) {
      saleCount = optionalCartItem.get().getSaleCount();
    }
    return calculateGiftCount(optionalCampaignEntity.get(), saleCount + itemCount)
        <= (optionalCampaignEntity.get().getCartLimit() * optionalCampaignEntity.get().getGiftCount());
  }

  private CartEntity addItemToCartWithCampaign(CampaignEntity campaignEntity, String cartId, int desiredSaleCount, int variantId) {
    CartEntity cartEntity = getCartEntityById(cartId);
    double itemPrice = getItemPrice(campaignEntity.getProductId());
    int itemStock = getItemStock(campaignEntity.getProductId());

    Optional<Variant> optionalVariant = getItemVariant(campaignEntity.getProductId(), variantId);
    Optional<List<CartItem>> optionalCartItems = getCartItems(cartEntity, campaignEntity.getProductId());

    //checks item on cart
    if (optionalCartItems.isPresent()) {
      Optional<CartItem> optionalCartItem = extractItemVariant(optionalCartItems, optionalVariant);

      int actualTotalGiftCount = optionalCartItems
          .get()
          .stream()
          .mapToInt(cartItem -> cartItem.getCampaignParams().getActualGiftCount())
          .sum();

      //checks item variant on cart
      if (optionalCartItem.isPresent()) {
        int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
        int itemOnCart = optionalCartItem.get().getSaleCount();
        int updatedSaleCount = desiredSaleCount + itemOnCart;
        int expectedGiftCount = calculateGiftCount(campaignEntity, updatedSaleCount);
        int actualGiftCount = optionalCartItem.get().getCampaignParams().getActualGiftCount();
        boolean isStockAvailable = isStockAvailable(campaignEntity.getProductId(), expectedGiftCount + updatedSaleCount);

        //check item variant status and stock availability
        if (optionalVariant.isPresent()) {
          itemStock = optionalCartItem.get().getVariant().getStock();
          isStockAvailable = isStockAvailable && (itemStock >= updatedSaleCount + expectedGiftCount);
        }

        SuitableSaleAndGiftCount suitableSaleAndGiftCount = SuitableSaleAndGiftCount.builder()
            .saleCount(updatedSaleCount)
            .giftCount(expectedGiftCount)
            .build();

        //check stock availability
        if (!isStockAvailable || updatedSaleCount < 0) {
          //stock is not available for direct addition, try one by one
          suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, itemOnCart, desiredSaleCount, itemStock);
          optionalCartItem.get().setMessageKey(Messages.CART_UPDATED.getKey());
        }

        //checks exceed cart limit
        if ((actualTotalGiftCount + suitableSaleAndGiftCount.getGiftCount() - actualGiftCount) / campaignEntity.getGiftCount() > campaignEntity.getCartLimit()) {
          //decrease updatedSaleCount for fit to itemStock
          while (updatedSaleCount + actualGiftCount > itemStock) {
            updatedSaleCount = updatedSaleCount - actualGiftCount;
          }
          int remainingGiftCount = (campaignEntity.getCartLimit() - (actualTotalGiftCount / campaignEntity.getGiftCount())) * campaignEntity.getGiftCount();

          //if addOneByOne method gives last items on stock as gift and sale, merge them and set to sale count
          if (suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() >= itemStock) {
            suitableSaleAndGiftCount.setSaleCount(updatedSaleCount - remainingGiftCount);
            optionalCartItem.get().setMessageKey(Messages.CART_UPDATED.getKey());
          }

          //save actual gift count or give possible gift count
          if (remainingGiftCount == 0){
            suitableSaleAndGiftCount.setGiftCount(actualGiftCount);
            optionalCartItem.get().setMessageKey(Messages.CART_LIMIT_EXCEED.getKey());
          }else{
            suitableSaleAndGiftCount.setGiftCount(remainingGiftCount);
            optionalCartItem.get().setMessageKey(Messages.CART_LIMIT_EXCEED.getKey());
          }
        }

        optionalCartItem.get().setPrice(suitableSaleAndGiftCount.getSaleCount() * itemPrice);
        optionalCartItem.get().setSaleCount(suitableSaleAndGiftCount.getSaleCount());
        optionalCartItem.get().setUpdatedAt(Instant.now().toEpochMilli());
        optionalCartItem.get().setCampaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()));

        cartEntity.getCartItems().remove(itemIndex);
        cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
      } else { //item variant not found cart
        SuitableSaleAndGiftCount suitableSaleAndGiftCount = defineSuitableSaleAndGiftCount(campaignEntity, desiredSaleCount, itemStock, optionalVariant, actualTotalGiftCount);
        int messageKey = describeMessageKey(desiredSaleCount, suitableSaleAndGiftCount, itemStock);
        addItem(cartEntity, campaignEntity, campaignEntity.getProductId(), variantId, suitableSaleAndGiftCount, messageKey);
      }
    } else { //item not found cart
      SuitableSaleAndGiftCount suitableSaleAndGiftCount = defineSuitableSaleAndGiftCount(campaignEntity, desiredSaleCount, itemStock, optionalVariant, 0);
      int messageKey = describeMessageKey(desiredSaleCount, suitableSaleAndGiftCount, itemStock);
      addItem(cartEntity, campaignEntity, campaignEntity.getProductId(), variantId, suitableSaleAndGiftCount, messageKey);
    }
    return cartEntity;
  }

  private Integer describeMessageKey(int desiredSaleCount, SuitableSaleAndGiftCount suitableSaleAndGiftCount, int stock){
    if (desiredSaleCount != suitableSaleAndGiftCount.getSaleCount()){
      return Messages.CART_UPDATED.getKey();
    }
    //TODO add complete logic
    return Messages.CART_UPDATED.getKey();
  }

  private CartEntity addItem(CartEntity cartEntity, CampaignEntity campaignEntity, int itemId, int variantId, SuitableSaleAndGiftCount suitableSaleAndGiftCount, int messageKey) {
    int sellerId = getSellerIdByItemId(itemId);
    double itemPrice = getItemPrice(itemId);
    Optional<Variant> optionalVariant = getItemVariant(itemId, variantId);

    cartEntity
        .getCartItems()
        .add(CartItem.builder()
            .productId(campaignEntity.getProductId())
            .sellerId(sellerId)
            .saleCount(suitableSaleAndGiftCount.getSaleCount())
            .addedAt(Instant.now().toEpochMilli())
            .hasCampaign(true)
            .campaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()))
            .price(itemPrice * suitableSaleAndGiftCount.getSaleCount())
            .hasVariant(optionalVariant.isPresent())
            .variant(optionalVariant.orElse(null))
            .messageKey(messageKey)
            .build());

    return cartEntity;
  }

  private SuitableSaleAndGiftCount defineSuitableSaleAndGiftCount(CampaignEntity campaignEntity, int desiredSaleCount, int itemStock, Optional<Variant> optionalVariant, int actualTotalGiftCount) {
    int giftCount = calculateGiftCount(campaignEntity, desiredSaleCount);
    boolean isStockAvailable = isStockAvailable(campaignEntity.getProductId(), giftCount + desiredSaleCount);

    //check item variant status and stock availability
    if (optionalVariant.isPresent()) {
      itemStock = optionalVariant.get().getStock();
      isStockAvailable = isStockAvailable && (itemStock >= desiredSaleCount + giftCount);
    }

    SuitableSaleAndGiftCount suitableSaleAndGiftCount = SuitableSaleAndGiftCount.builder()
        .saleCount(desiredSaleCount)
        .giftCount(giftCount)
        .build();

    //check stock availability
    if (!isStockAvailable || desiredSaleCount <= 0) {
      //stock not available, try one by one
      suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, 0, desiredSaleCount, itemStock);
    }

    if ((actualTotalGiftCount + suitableSaleAndGiftCount.getGiftCount()) / campaignEntity.getGiftCount() > campaignEntity.getCartLimit()) {
      int remainingGiftCount = (campaignEntity.getCartLimit() - (actualTotalGiftCount / campaignEntity.getGiftCount())) * campaignEntity.getGiftCount();

      //if addOneByOne method gives last items on stock as gift and sale, merge them and set to sale count
      if (suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() >= itemStock) {
        suitableSaleAndGiftCount.setSaleCount(suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() - remainingGiftCount);
      }

      suitableSaleAndGiftCount.setGiftCount(remainingGiftCount);
    }

    return suitableSaleAndGiftCount;
  }

  private SuitableSaleAndGiftCount addOneByOneToCart(CampaignEntity campaignEntity, int itemOnCart, int desiredSaleCount, int itemStock) {
    int suitableSaleCount = 0;
    int suitableGiftCount = 0;

    for (suitableSaleCount = 0; suitableSaleCount < desiredSaleCount; suitableSaleCount++) {
      suitableGiftCount = calculateGiftCount(campaignEntity, itemOnCart + suitableSaleCount);
      //stock exactly fit to give suitable sale count and gift count
      if (itemStock - (itemOnCart + suitableGiftCount + suitableSaleCount) == 0) {
        break;
      } else if (itemStock - (itemOnCart + suitableGiftCount + suitableSaleCount) < 0) { //suitable values exceeds stock
        //decrease for find max available value
        suitableSaleCount -= 1;

        //calculate available gift count
        suitableGiftCount = calculateGiftCount(campaignEntity, itemOnCart + suitableSaleCount);

        int remainingStock = itemStock - (itemOnCart + suitableSaleCount + suitableGiftCount);
        if (remainingStock > 0 && remainingStock <= campaignEntity.getGiftCount() && suitableSaleCount + remainingStock <= desiredSaleCount) {
          suitableSaleCount += remainingStock;
        }
        break;
      }
    }

    return SuitableSaleAndGiftCount.builder()
        .saleCount(suitableSaleCount + itemOnCart)
        .giftCount(suitableGiftCount)
        .build();
  }

  private int calculateGiftCount(CampaignEntity campaignEntity, int saleCount) {
    int requirementCount = campaignEntity.getRequirementCount();
    int quotient = saleCount / requirementCount;
    int possibleGiftCount = quotient * campaignEntity.getGiftCount();

    if (possibleGiftCount > (campaignEntity.getGiftCount() * campaignEntity.getCartLimit())) {
      return (campaignEntity.getGiftCount() * campaignEntity.getCartLimit());
    }
    return possibleGiftCount;
  }

  private boolean atLeastOneAvailability(CampaignEntity campaignEntity, int itemCount, String cartId) {
    int itemOnCart = 0;
    int giftOnCart = 0;

    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (optionalCartEntity.isPresent()) {
      Optional<CartItem> optionalCartItem = optionalCartEntity
          .get()
          .getCartItems()
          .stream()
          .filter(cartItem -> cartItem.getProductId().equals(campaignEntity.getProductId()))
          .findFirst();
      if (optionalCartItem.isPresent()) {
        itemOnCart = optionalCartItem.get().getSaleCount();
        giftOnCart = optionalCartItem.get().getCampaignParams().getActualGiftCount();
      }
    }

    int itemStock = getItemStock(campaignEntity.getProductId());

    if (itemCount + itemOnCart + giftOnCart > itemStock) {
      SuitableSaleAndGiftCount suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, itemOnCart, itemCount, getItemStock(campaignEntity.getProductId()));
      return suitableSaleAndGiftCount.getGiftCount() >= campaignEntity.getGiftCount();
    }
    return true;
  }

  private CampaignParams prepareCampaignParams(CampaignEntity campaignEntity, int giftCount, int itemCount) {
    return CampaignParams.builder()
        .actualGiftCount(giftCount)
        .badge(Badge.builder()
            .requirement(campaignEntity.getRequirementCount())
            .gift(campaignEntity.getGiftCount())
            .build())
        .totalItemCount(itemCount)
        .build();
  }

  private Optional<CartItem> extractItemVariant(Optional<List<CartItem>> optionalCartItems, Optional<Variant> optionalVariant) {
    return optionalVariant
        .map(variant -> optionalCartItems
            .get()
            .stream()
            .filter(cartItem -> cartItem.getVariant().getId().equals(variant.getId()))
            .findFirst())
        .orElseGet(() -> optionalCartItems.get().stream().findFirst());
  }

  private Optional<List<CartItem>> getCartItems(CartEntity cartEntity, int itemId) {
    List<CartItem> cartItems = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(itemId))
        .collect(Collectors.toList());

    return Optional.ofNullable(cartItems);
  }

  private CartEntity addItemToCartOrdinary(String cartId, int itemId, int desiredSaleCount, int variantId) {
    CartEntity cartEntity = getCartEntityById(cartId);
    double itemPrice = getItemPrice(itemId);
    int itemStock = getItemStock(itemId);
    Optional<Variant> optionalVariant = getItemVariant(itemId, variantId);

    Optional<List<CartItem>> optionalCartItems = getCartItems(cartEntity, itemId);
    //checks item on cart
    if (optionalCartItems.isPresent()) {
      Optional<CartItem> optionalCartItem = extractItemVariant(optionalCartItems, optionalVariant);

      //checks item variant on cart
      if (optionalCartItem.isPresent()) {
        int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
        int itemOnCart = optionalCartItem.get().getSaleCount();
        boolean isStockAvailable = isStockAvailable(itemId, itemOnCart + desiredSaleCount);

        //check item variant status and stock availability
        if (optionalVariant.isPresent()) {
          itemStock = optionalCartItem.get().getVariant().getStock();
          isStockAvailable = isStockAvailable && itemStock >= optionalCartItem.get().getSaleCount() + desiredSaleCount;
        }

        //check stock availability
        if (!isStockAvailable) {
          //stock is not available for direct addition, try one by one
          desiredSaleCount = addOneByOneToCart(itemOnCart, itemStock);
          optionalCartItem.get().setMessageKey(Messages.CART_UPDATED.getKey());
        }
        cartEntity.getCartItems().remove(itemIndex);
        cartEntity.getCartItems().add(itemIndex, setCountAndPriceToCartItem(optionalCartItem.get(), desiredSaleCount, itemPrice));
      } else {
        addItem(cartEntity, itemId, variantId, desiredSaleCount);
      }
    } else {
      addItem(cartEntity, itemId, variantId, desiredSaleCount);
    }
    return cartEntity;
  }

  private CartEntity addItem(CartEntity cartEntity, int itemId, int variantId, int desiredSaleCount) {
    int sellerId = getSellerIdByItemId(itemId);
    double itemPrice = getItemPrice(itemId);
    int itemStock = getItemStock(itemId);
    Optional<Variant> optionalVariant = getItemVariant(itemId, variantId);
    boolean isStockAvailable = isStockAvailable(itemId, desiredSaleCount);

    //check item variant status and stock availability
    if (optionalVariant.isPresent()) {
      itemStock = optionalVariant.get().getStock();
      isStockAvailable = isStockAvailable && getItemVariantStock(variantId) >= desiredSaleCount;
    }

    //checks stock availability
    if (!isStockAvailable || desiredSaleCount <= 0) {
      //stock is not available, try one by one
      desiredSaleCount = addOneByOneToCart(0, itemStock);
    }

    cartEntity
        .getCartItems()
        .add(CartItem.builder()
            .productId(itemId)
            .sellerId(sellerId)
            .saleCount(desiredSaleCount)
            .addedAt(Instant.now().toEpochMilli())
            .hasCampaign(false)
            .price(itemPrice * (desiredSaleCount))
            .hasVariant(optionalVariant.isPresent())
            .variant(optionalVariant.orElse(null))
            .build());

    return cartEntity;
  }

  private CartItem setCountAndPriceToCartItem(CartItem cartItem, int desiredSaleCount, double itemPrice) {
    cartItem.setPrice((desiredSaleCount + cartItem.getSaleCount()) * itemPrice);
    cartItem.setSaleCount(desiredSaleCount + cartItem.getSaleCount());
    cartItem.setUpdatedAt(Instant.now().toEpochMilli());
    return cartItem;
  }

  private Integer addOneByOneToCart(int itemOnCart, int itemStock) {
    int suitableAddition = 0;

    while (true) {
      suitableAddition++;
      if (itemStock - (itemOnCart + suitableAddition) < 0) {
        suitableAddition--;
        break;
      }
    }
    return suitableAddition;
  }

  private void updateCartEntity(CartEntity cartEntity, int accountId, int itemId) {
    Optional<CartItem> optionalCartItem = cartEntity.getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(itemId))
        .findFirst();

    if (optionalCartItem.isPresent()) {
      int updatedItemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
      cartEntity.getCartItems().remove(updatedItemIndex);
      optionalCartItem.get().setUpdatedAt(Instant.now().toEpochMilli());
      cartEntity.getCartItems().add(updatedItemIndex, optionalCartItem.get());

      cartRepository
          .updateCart(CartEntity.builder()
              .id(cartEntity.getId())
              .accountId(accountId)
              .cartItems(cartEntity.getCartItems())
              .build());
    }
  }
}
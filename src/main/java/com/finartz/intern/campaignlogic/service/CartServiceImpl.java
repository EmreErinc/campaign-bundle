package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.request.AddItemToCartRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemDecrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemIncrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemRemoveRequest;
import com.finartz.intern.campaignlogic.model.response.CartControlResponse;
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
    return updateCart(Converters.convertToCartDto(accountId, cartId, request), false);
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
        .filter(cartItem -> cartItem.getProductId().equals(request.getProductId()))
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
      throw new ApplicationContextException(Messages.ITEM_NOT_FOUND_ON_CART.getValue());
    }
    cartEntity.getCartItems().remove(optionalCartItem.get());
    cartRepository.updateCart(cartEntity);
    return getCart(cartId);
  }

  @Override
  public CartResponse incrementItem(int accountId, String cartId, CartItemIncrementRequest request) {
    if (isItemAvailable(Converters.convertToCartDto(accountId, cartId, request))) {
      updateCart(Converters.convertToCartDto(accountId, cartId, request), false);
    }
    return getCart(cartId);
  }

  @Override
  public CartResponse decrementItem(int accountId, String cartId, CartItemDecrementRequest request) {
    if (isProductOnCart(cartId, request.getProductId())
        && isItemAvailable(Converters.convertToCartDto(accountId, cartId, request))) {
      updateCart(Converters.convertToCartDto(accountId, cartId, request), false);
      if (getProductCountOnCart(cartId, request.getProductId(), Optional.ofNullable(request.getVariantId())).equals(0)) {
        removeFromCart(accountId, cartId, CartItemRemoveRequest.builder().productId(request.getProductId()).variantId(request.getVariantId()).build());
      }
    }
    return getCart(cartId);
  }

  @Override
  public String createCart(int accountId) {
    return cartRepository
        .createCart(accountId).getId();
  }

  public CartResponse updateCart(CartDto cartDto, boolean recalculate) {
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByProductId(cartDto.getProductId());

    //checks campaign is available
    if (optionalCampaignEntity.isPresent()
        && isItemAvailable(cartDto)
        && isCampaignAvailableGetById(optionalCampaignEntity.get().getId())
        && atLeastOneAvailability(optionalCampaignEntity.get(), cartDto.getDesiredCount(), cartDto.getCartId(), recalculate)) {
      CartEntity cartEntity = addItemToCartWithCampaign(optionalCampaignEntity.get(), cartDto, recalculate);
      updateCartEntity(cartEntity, cartDto.getAccountId(), cartDto.getProductId());
    } else {
      CartEntity cartEntity = addItemToCartOrdinary(cartDto, recalculate);
      updateCartEntity(cartEntity, cartDto.getAccountId(), cartDto.getProductId());
    }
    return getCart(cartDto.getCartId());
  }

  private CartEntity addItemToCartWithCampaign(CampaignEntity campaignEntity, CartDto cartDto, boolean recalculate) {
    CartEntity cartEntity = getCartEntityById(cartDto.getCartId());
    double itemPrice = getProductPrice(campaignEntity.getProductId());
    int itemStock = getProductStock(campaignEntity.getProductId());

    Optional<Variant> optionalVariant = getProductVariant(campaignEntity.getProductId(), cartDto.getVariantId().orElse(0));
    Optional<List<CartItem>> optionalCartItems = getCartItems(cartEntity, campaignEntity.getProductId());

    //checks item on cart
    if (optionalCartItems.isPresent()) {
      updateOptionalCartItem(campaignEntity, cartDto, cartEntity, itemPrice, itemStock, optionalVariant, optionalCartItems, recalculate);
    } else { //item not found cart
      SuitableSaleAndGiftCount suitableSaleAndGiftCount = calculateSuitableSaleAndGiftCount(campaignEntity, cartDto.getDesiredCount(), itemStock, optionalVariant, 0, recalculate);
      int messageKey = describeMessageKey(cartEntity.getAccountId(), campaignEntity, cartDto.getDesiredCount(), suitableSaleAndGiftCount, itemStock, cartDto.getCartId(), optionalVariant);
      addItemToCartEntity(cartEntity, campaignEntity, campaignEntity.getProductId(), cartDto.getVariantId().orElse(0), cartDto.getDesiredCount(), suitableSaleAndGiftCount, messageKey);
    }
    return cartEntity;
  }

  private void updateOptionalCartItem(CampaignEntity campaignEntity, CartDto cartDto, CartEntity cartEntity, double itemPrice, int itemStock, Optional<Variant> optionalVariant, Optional<List<CartItem>> optionalCartItems, boolean recalculate) {
    Optional<CartItem> optionalCartItem = extractItemVariant(optionalCartItems, optionalVariant);

    int actualTotalGiftCount = optionalCartItems
        .get()
        .stream()
        .mapToInt(cartItem -> cartItem.getCampaignParams().getActualGiftCount())
        .sum();

    //checks item variant on cart
    if (optionalCartItem.isPresent()) {
      checkItemVariantOnCartAndAddToCartEntity(campaignEntity, cartDto.getDesiredCount(), cartEntity, itemPrice, itemStock, optionalVariant, optionalCartItem, actualTotalGiftCount, recalculate);
    } else { //item variant not found cart
      SuitableSaleAndGiftCount suitableSaleAndGiftCount = calculateSuitableSaleAndGiftCount(campaignEntity, cartDto.getDesiredCount(), itemStock, optionalVariant, actualTotalGiftCount, recalculate);
      int messageKey = describeMessageKey(cartEntity.getAccountId(), campaignEntity, cartDto.getDesiredCount(), suitableSaleAndGiftCount, itemStock, cartDto.getCartId(), optionalVariant);
      addItemToCartEntity(cartEntity, campaignEntity, campaignEntity.getProductId(), cartDto.getVariantId().orElse(0), cartDto.getDesiredCount(), suitableSaleAndGiftCount, messageKey);
    }
  }

  private void checkItemVariantOnCartAndAddToCartEntity(CampaignEntity campaignEntity, int desiredSaleCount, CartEntity cartEntity, double itemPrice, int itemStock, Optional<Variant> optionalVariant, Optional<CartItem> optionalCartItem, int actualTotalGiftCount, boolean recalculate) {
    int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
    int itemOnCart = optionalCartItem.get().getSaleCount();
    int updatedSaleCount = desiredSaleCount + itemOnCart;
    int expectedGiftCount = calculateGiftCount(campaignEntity, updatedSaleCount);
    int actualGiftCount = optionalCartItem.get().getCampaignParams().getActualGiftCount();
    boolean isStockAvailable = isStockAvailable(campaignEntity.getProductId(), expectedGiftCount + updatedSaleCount);

    //check item variant status and stock availability
    if (optionalVariant.isPresent()) {
      itemStock = getProductVariantStock(optionalVariant.get().getId());
      optionalCartItem.get().getVariant().setStock(itemStock);
      isStockAvailable = isStockAvailable && (itemStock >= updatedSaleCount + expectedGiftCount);
    }

    SuitableSaleAndGiftCount suitableSaleAndGiftCount = SuitableSaleAndGiftCount.builder()
        .saleCount(updatedSaleCount)
        .giftCount(expectedGiftCount)
        .build();

    //check stock availability
    if (!isStockAvailable || updatedSaleCount < 0) {
      //stock is not available for direct addition, try one by one
      suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, itemOnCart, desiredSaleCount, itemStock, recalculate);
      optionalCartItem.get().setMessageKey(Messages.CART_UPDATED.getKey());
    }

    //checks exceed cart limit
    if ((actualTotalGiftCount + suitableSaleAndGiftCount.getGiftCount() - actualGiftCount) / campaignEntity.getGiftCount() > campaignEntity.getCartLimit()) {
      calculateGiftsAccordingToLimit(campaignEntity, cartEntity.getId(), itemStock, optionalVariant, optionalCartItem, actualTotalGiftCount, updatedSaleCount, suitableSaleAndGiftCount);
    }

    //if (campaignEntity.getRequirementCount() * campaignEntity.getGiftCount() < suitableSaleAndGiftCount.getSaleCount()){
    //  optionalCartItem.get().setMessageKey(Messages.CART_LIMIT_EXCEED.getKey());
    //}

    if (suitableSaleAndGiftCount.getSaleCount() != 0) {
      updateIndexOfItem(campaignEntity, desiredSaleCount, cartEntity, itemPrice, optionalCartItem, recalculate, itemIndex, suitableSaleAndGiftCount);
    }
  }

  private void updateIndexOfItem(CampaignEntity campaignEntity, int desiredSaleCount, CartEntity cartEntity, double itemPrice, Optional<CartItem> optionalCartItem, boolean recalculate, int itemIndex, SuitableSaleAndGiftCount suitableSaleAndGiftCount) {
    if (!recalculate) {
      optionalCartItem.get().setDesiredSaleCount(optionalCartItem.get().getDesiredSaleCount() + desiredSaleCount);
    }
    if (recalculate) {
      optionalCartItem.get().setMessageKey(0);
    }
    optionalCartItem.get().setPrice(suitableSaleAndGiftCount.getSaleCount() * itemPrice);
    optionalCartItem.get().setSaleCount(suitableSaleAndGiftCount.getSaleCount());
    optionalCartItem.get().setUpdatedAt(Instant.now().toEpochMilli());
    optionalCartItem.get().setCampaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()));

    cartEntity.getCartItems().remove(itemIndex);
    cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
  }

  private void calculateGiftsAccordingToLimit(CampaignEntity campaignEntity, String cartId, int itemStock, Optional<Variant> optionalVariant, Optional<CartItem> optionalCartItem, int actualTotalGiftCount, int updatedSaleCount, SuitableSaleAndGiftCount suitableSaleAndGiftCount) {
    int actualGiftCount = optionalCartItem.get().getCampaignParams().getActualGiftCount();

    //decrease updatedSaleCount for fit to itemStock
    while (updatedSaleCount + actualGiftCount > itemStock) {
      updatedSaleCount = updatedSaleCount - actualGiftCount;
    }
    int remainingGiftCount = (campaignEntity.getCartLimit() - (actualTotalGiftCount / campaignEntity.getGiftCount())) * campaignEntity.getGiftCount();

    //if addOneByOne method gives last items on stock as gift and sale, merge them and set to sale desiredCount
    if (suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() >= itemStock) {
      suitableSaleAndGiftCount.setSaleCount(updatedSaleCount - remainingGiftCount);
      optionalCartItem.get().setMessageKey(Messages.CART_UPDATED.getKey());
    }

    //save actual gift desiredCount or give possible gift desiredCount
    if (remainingGiftCount == 0) {
      suitableSaleAndGiftCount.setGiftCount(actualGiftCount);
      optionalCartItem.get().setMessageKey(Messages.CART_LIMIT_EXCEED.getKey());
    } else {
      suitableSaleAndGiftCount.setGiftCount(remainingGiftCount);
      optionalCartItem.get().setMessageKey(Messages.CART_LIMIT_EXCEED.getKey());
    }

    if (optionalVariant.isPresent() && getTotalProductCountOnCart(cartId, campaignEntity.getProductId()) > (campaignEntity.getGiftCount() * campaignEntity.getRequirementCount())) {
      optionalCartItem.get().setMessageKey(Messages.CART_LIMIT_EXCEED.getKey());
    }
  }

  private Integer describeMessageKey(int accountId, CampaignEntity campaignEntity, int desiredSaleCount, SuitableSaleAndGiftCount suitableSaleAndGiftCount, int stock, String cartId, Optional<Variant> optionalVariant) {
    if (userAvailableForCampaign(accountId, campaignEntity.getId())) {
      return Messages.CAMPAIGN_LIMIT_EXCEED.getKey();
    }
    if (optionalVariant.isPresent() && (getTotalProductCountOnCart(cartId, campaignEntity.getProductId()) + suitableSaleAndGiftCount.getSaleCount()) > (campaignEntity.getCartLimit() * campaignEntity.getRequirementCount())) {
      return Messages.CART_LIMIT_EXCEED.getKey();
    }
    if (desiredSaleCount != suitableSaleAndGiftCount.getSaleCount()) {
      return Messages.CART_UPDATED.getKey();
    }
    if (suitableSaleAndGiftCount.getSaleCount() > stock) {
      return Messages.CART_UPDATED.getKey();
    }
    return Messages.EMPTY.getKey();
  }

  private CartEntity addItemToCartEntity(CartEntity cartEntity, CampaignEntity campaignEntity, int itemId, int variantId, int desiredSaleCount, SuitableSaleAndGiftCount suitableSaleAndGiftCount, int messageKey) {
    int sellerId = getSellerIdByProductId(itemId);
    double itemPrice = getProductPrice(itemId);
    Optional<Variant> optionalVariant = getProductVariant(itemId, variantId);

    if (suitableSaleAndGiftCount.getSaleCount() != 0) {
      cartEntity
          .getCartItems()
          .add(CartItem.builder()
              .productId(campaignEntity.getProductId())
              .sellerId(sellerId)
              .desiredSaleCount(desiredSaleCount)
              .saleCount(suitableSaleAndGiftCount.getSaleCount())
              .addedAt(Instant.now().toEpochMilli())
              .hasCampaign(true)
              .campaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()))
              .price(itemPrice * suitableSaleAndGiftCount.getSaleCount())
              .hasVariant(optionalVariant.isPresent())
              .variant(optionalVariant.orElse(null))
              .messageKey(messageKey)
              .build());
    }
    return cartEntity;
  }

  private SuitableSaleAndGiftCount calculateSuitableSaleAndGiftCount(CampaignEntity campaignEntity, int desiredSaleCount, int itemStock, Optional<Variant> optionalVariant, int actualTotalGiftCount, boolean recalculate) {
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
      suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, 0, desiredSaleCount, itemStock, recalculate);
    }

    if ((actualTotalGiftCount + suitableSaleAndGiftCount.getGiftCount()) / campaignEntity.getGiftCount() > campaignEntity.getCartLimit()) {
      calculateGiftsAccordingToLimit(campaignEntity, itemStock, actualTotalGiftCount, suitableSaleAndGiftCount);
    }

    return suitableSaleAndGiftCount;
  }

  private void calculateGiftsAccordingToLimit(CampaignEntity campaignEntity, int itemStock, int actualTotalGiftCount, SuitableSaleAndGiftCount suitableSaleAndGiftCount) {
    int remainingGiftCount = (campaignEntity.getCartLimit() - (actualTotalGiftCount / campaignEntity.getGiftCount())) * campaignEntity.getGiftCount();

    //if addOneByOne method gives last items on stock as gift and sale, merge them and set to sale desiredCount
    if (suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() >= itemStock) {
      suitableSaleAndGiftCount.setSaleCount(suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() - remainingGiftCount);
    }

    suitableSaleAndGiftCount.setGiftCount(remainingGiftCount);
  }

  private SuitableSaleAndGiftCount addOneByOneToCart(CampaignEntity campaignEntity, int itemOnCart, int desiredSaleCount, int itemStock, boolean recalculate) {
    int suitableSaleCount = 0;
    int suitableGiftCount = 0;

    if (recalculate) {
      itemOnCart = 0;
    }

    for (suitableSaleCount = 0; suitableSaleCount < desiredSaleCount; suitableSaleCount++) {
      suitableGiftCount = calculateGiftCount(campaignEntity, itemOnCart + suitableSaleCount);
      //stock exactly fit to give suitable sale desiredCount and gift desiredCount
      if (itemStock - (itemOnCart + suitableGiftCount + suitableSaleCount) == 0) {
        break;
      } else if (itemStock - (itemOnCart + suitableGiftCount + suitableSaleCount) < 0) { //suitable values exceeds stock
        //decrease for find max available value
        suitableGiftCount = decreaseAndCalculateSuitableGiftCount(suitableSaleCount, suitableGiftCount, itemStock, itemOnCart);
        break;
      }
    }
    return SuitableSaleAndGiftCount.builder()
        .saleCount(suitableSaleCount + itemOnCart)
        .giftCount(suitableGiftCount)
        .build();
  }

  private Integer decreaseAndCalculateSuitableGiftCount(int suitableSaleCount, int suitableGiftCount, int itemStock, int itemOnCart) {
    while (itemOnCart + suitableSaleCount + suitableGiftCount > itemStock) {
      suitableGiftCount -= 1;
    }
    return suitableGiftCount;
  }

  private Boolean atLeastOneAvailability(CampaignEntity campaignEntity, int itemCount, String cartId, boolean recalculate) {
    int itemOnCart = 0;
    int giftOnCart = 0;

    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (optionalCartEntity.isPresent()) {
      getItemSaleAndGiftCountOnCart(optionalCartEntity, campaignEntity, itemOnCart, giftOnCart);
    }

    int itemStock = getProductStock(campaignEntity.getProductId());
    if (itemCount + itemOnCart + giftOnCart > itemStock) {
      SuitableSaleAndGiftCount suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, itemOnCart, itemCount, getProductStock(campaignEntity.getProductId()), recalculate);
      return suitableSaleAndGiftCount.getGiftCount() >= campaignEntity.getGiftCount();
    }
    return true;
  }

  private void getItemSaleAndGiftCountOnCart(Optional<CartEntity> optionalCartEntity, CampaignEntity campaignEntity, int itemOnCart, int giftOnCart) {
    Optional<CartItem> optionalCartItem = optionalCartEntity
        .get()
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(campaignEntity.getProductId()))
        .findFirst();
    if (optionalCartItem.isPresent()) {
      optionalCartItem.get().getSaleCount();
      optionalCartItem.get().getCampaignParams().getActualGiftCount();
    }
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

  private CartEntity addItemToCartOrdinary(CartDto cartDto, boolean recalculate) {
    CartEntity cartEntity = getCartEntityById(cartDto.getCartId());
    int itemStock = getProductStock(cartDto.getProductId());
    Optional<Variant> optionalVariant = getProductVariant(cartDto.getProductId(), cartDto.getVariantId().orElse(0));

    Optional<List<CartItem>> optionalCartItems = getCartItems(cartEntity, cartDto.getProductId());
    //checks item on cart
    if (optionalCartItems.isPresent()) {
      updateItemQuantity(cartDto, cartEntity, itemStock, optionalVariant, optionalCartItems, recalculate);
    } else {
      addItemToCartEntity(cartEntity, cartDto);
    }
    return cartEntity;
  }

  private void updateItemQuantity(CartDto cartDto, CartEntity cartEntity, int itemStock, Optional<Variant> optionalVariant, Optional<List<CartItem>> optionalCartItems, boolean recalculate) {
    double itemPrice = getProductPrice(cartDto.getProductId());
    Optional<CartItem> optionalCartItem = extractItemVariant(optionalCartItems, optionalVariant);

    //checks item variant on cart
    if (optionalCartItem.isPresent()) {
      updateIndexOfItemVariant(cartDto.getProductId(), cartDto.getDesiredCount(), cartEntity, itemPrice, itemStock, optionalVariant, optionalCartItem, recalculate);
    } else {
      addItemToCartEntity(cartEntity, cartDto);
    }
  }

  private void updateIndexOfItemVariant(int itemId, int desiredSaleCount, CartEntity cartEntity, double itemPrice, int itemStock, Optional<Variant> optionalVariant, Optional<CartItem> optionalCartItem, boolean recalculate) {
    int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
    int itemOnCart = optionalCartItem.get().getSaleCount();
    boolean isStockAvailable = isStockAvailable(itemId, itemOnCart + desiredSaleCount);

    //check item variant status and stock availability
    if (optionalVariant.isPresent()) {
      itemStock = getProductVariantStock(optionalVariant.get().getId());
      optionalCartItem.get().getVariant().setStock(itemStock);
      isStockAvailable = isStockAvailable && itemStock >= optionalCartItem.get().getSaleCount() + desiredSaleCount;
    }

    //check stock availability
    if (!isStockAvailable) {
      //stock is not available for direct addition, try one by one
      desiredSaleCount = addOneByOneToCart(itemOnCart, itemStock);
      optionalCartItem.get().setMessageKey(Messages.CART_UPDATED.getKey());
    }

    if (desiredSaleCount != 0) {
      updateIndexOfItem(desiredSaleCount, cartEntity, itemPrice, optionalCartItem, recalculate, itemIndex);
    }
  }

  private void updateIndexOfItem(int saleCount, CartEntity cartEntity, double itemPrice, Optional<CartItem> optionalCartItem, boolean recalculate, int itemIndex) {
    if (!recalculate) {
      saleCount += optionalCartItem.get().getSaleCount();
    }
    if (recalculate) {
      optionalCartItem.get().setMessageKey(0);
    }
    optionalCartItem.get().setDesiredSaleCount(saleCount);
    optionalCartItem.get().setPrice(saleCount * itemPrice);
    optionalCartItem.get().setSaleCount(saleCount);
    optionalCartItem.get().setUpdatedAt(Instant.now().toEpochMilli());

    cartEntity.getCartItems().remove(itemIndex);
    cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
  }

  private CartEntity addItemToCartEntity(CartEntity cartEntity, CartDto cartDto) {
    int sellerId = getSellerIdByProductId(cartDto.getProductId());
    double itemPrice = getProductPrice(cartDto.getProductId());
    int itemStock = getProductStock(cartDto.getProductId());
    Optional<Variant> optionalVariant = getProductVariant(cartDto.getProductId(), cartDto.getVariantId().orElse(0));
    boolean isStockAvailable = isStockAvailable(cartDto.getProductId(), cartDto.getDesiredCount());
    int desiredSaleCount = cartDto.getDesiredCount();

    //check item variant status and stock availability
    if (optionalVariant.isPresent()) {
      itemStock = getProductVariantStock(optionalVariant.get().getId());
      isStockAvailable = isStockAvailable && getProductVariantStock(cartDto.getVariantId().get()) >= desiredSaleCount;
    }

    //checks stock availability
    if (!isStockAvailable || cartDto.getDesiredCount() <= 0) {
      //stock is not available, try one by one
      desiredSaleCount = addOneByOneToCart(0, itemStock);
    }

    if (desiredSaleCount != 0) {
      cartEntity
          .getCartItems()
          .add(CartItem.builder()
              .productId(cartDto.getProductId())
              .sellerId(sellerId)
              .desiredSaleCount(cartDto.getDesiredCount())
              .saleCount(desiredSaleCount)
              .addedAt(Instant.now().toEpochMilli())
              .hasCampaign(false)
              .price(itemPrice * (desiredSaleCount))
              .hasVariant(optionalVariant.isPresent())
              .variant(optionalVariant.orElse(null))
              .build());
    }
    return cartEntity;
  }

  private Integer addOneByOneToCart(int itemOnCart, int itemStock) {
    int suitableAddition = 0;

    //use for update operation
    if (itemStock < itemOnCart) {
      return -itemOnCart + itemStock;
    }

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

  public CartResponse recalculateCartItems(int accountId, String cartId, CartControlResponse cartControlResponse) {
    CartDto cartDto = CartDto.builder()
        .accountId(accountId)
        .cartId(cartId)
        .productId(cartControlResponse.getProductId())
        .variantId(Optional.of(cartControlResponse.getVariantId()))
        .desiredCount(cartControlResponse.getDesiredSaleCount())
        .build();

    return updateCart(cartDto, true);
  }
}
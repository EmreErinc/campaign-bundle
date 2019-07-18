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
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(request.getItemId()))//TODO variant kontrolü eklenecek
        .findFirst();
    if (!optionalCartItem.isPresent()) {
      throw new ApplicationContextException("Ürün sepette bulunamadı.");
    }
    cartEntity.getCartItems().remove(optionalCartItem.get());
    cartRepository.updateCart(cartEntity);
    return getCart(cartId);
  }

  @Override
  public CartResponse incrementItem(int accountId, String cartId, CartItemIncrementRequest request) {
    if (itemAvailability(accountId, cartId, request.getItemId(), 1)) {
      updateCart(accountId, cartId, request.getItemId(), 1, Optional.ofNullable(request.getVariantId()));
    }
    return getCart(cartId);
  }

  @Override
  public CartResponse decrementItem(int accountId, String cartId, CartItemDecrementRequest request) {
    if (isItemOnCart(cartId, request.getItemId())
        && itemAvailability(accountId, cartId, request.getItemId(), -1)) {
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
        && itemAvailability(accountId, cartId, itemId, count)
        && isCampaignAvailableGetById(optionalCampaignEntity.get().getId())
        && atLeastOneAvailability(optionalCampaignEntity.get(), count, cartId)) {
      CartEntity cartEntity = addItemToCartWithCampaign(optionalCampaignEntity.get(), cartId, count, variantId.orElse(0));
      updateCartEntity(cartEntity, accountId, itemId);
    } else {
      CartEntity cartEntity = addItemToCart(cartId, itemId, count, variantId.orElse(0));
      updateCartEntity(cartEntity, accountId, itemId);
    }
    return getCart(cartId);
  }

  private boolean itemAvailability(int accountId, String cartId, int itemId, int itemCount) {
    if (!isCampaignAvailableGetByItemId(itemId)) {
      log.info("Kampanya Süresi Doldu.");
      //return false;
    }

    if (!isItemHasCampaign(itemId)) {
      log.info("Ürüne ait kampanya bulunamadı. Normal süreç işlenecek.");
      //return true;
    }

    if (!isCampaignLimitAvailableForAccount(accountId, itemId)) {
      log.info("Kampanya Limitinizi Doldurdunuz.");
      return false;
    }

    if (!cartLimitAvailability(cartId, itemId, itemCount)) {
      log.info("Sepet Limitinizi Doldurdunuz.");
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
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
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
    int sellerId = getSellerIdByItemId(campaignEntity.getItemId());
    double itemPrice = getItemPrice(campaignEntity.getItemId());
    int itemStock = getItemStock(campaignEntity.getItemId());
    Optional<Variant> optionalVariant = getItemVariant(campaignEntity.getItemId(), variantId);

    Optional<List<CartItem>> optionalCartItems = getCartItems(cartEntity, campaignEntity.getItemId());
    //checks item on cart
    if (optionalCartItems.isPresent()) {
      Optional<CartItem> optionalCartItem = Optional.empty();
      if (optionalVariant.isPresent()) {
        optionalCartItem = optionalCartItems
            .get()
            .stream()
            .filter(cartItem -> cartItem.getVariant().getId().equals(variantId))
            .findFirst();
      } else {
        optionalCartItem = optionalCartItems.get().stream().findFirst();
      }

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
        boolean isStockAvailable = isStockAvailable(campaignEntity.getItemId(), expectedGiftCount + updatedSaleCount);

        //check item variant status and stock availability
        if (optionalVariant.isPresent()) {
          itemStock = optionalCartItem.get().getVariant().getStock();
          isStockAvailable = isStockAvailable && (itemStock >= optionalCartItem.get().getSaleCount() + desiredSaleCount + expectedGiftCount);
        }

        SuitableSaleAndGiftCount suitableSaleAndGiftCount = SuitableSaleAndGiftCount.builder()
            .saleCount(updatedSaleCount)
            .giftCount(expectedGiftCount)
            .build();

        //check stock availability
        if (!isStockAvailable || updatedSaleCount < 0) {
          //stock is not available for direct addition, try one by one
          suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, itemOnCart, desiredSaleCount, itemStock);
        }

        //checks exceed cart limit
        if ((actualTotalGiftCount + suitableSaleAndGiftCount.getGiftCount() - actualGiftCount) / campaignEntity.getGiftCount() > campaignEntity.getCartLimit()) {
          //if addOneByOne method gives last items on stock as gift and sale, merge them and set to sale count
          if (suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() >= itemStock) {
            suitableSaleAndGiftCount.setSaleCount(suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() - actualGiftCount);
          }

          int remainingGiftCount = (campaignEntity.getCartLimit() - (actualTotalGiftCount / campaignEntity.getGiftCount())) * campaignEntity.getGiftCount();
          if (remainingGiftCount == 0) {
            suitableSaleAndGiftCount.setGiftCount(actualGiftCount);
          } else {
            suitableSaleAndGiftCount.setGiftCount(remainingGiftCount);
          }
        }

        optionalCartItem.get().setPrice(suitableSaleAndGiftCount.getSaleCount() * itemPrice);
        optionalCartItem.get().setSaleCount(suitableSaleAndGiftCount.getSaleCount());
        optionalCartItem.get().setUpdatedAt(Instant.now().toEpochMilli());
        optionalCartItem.get().setCampaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()));

        cartEntity.getCartItems().remove(itemIndex);
        cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
      } else { //item variant not found cart
        SuitableSaleAndGiftCount suitableSaleAndGiftCount = defineSuitableSaleAndGiftCount(campaignEntity, desiredSaleCount, itemStock, optionalVariant);

        if ((actualTotalGiftCount + suitableSaleAndGiftCount.getGiftCount()) / campaignEntity.getGiftCount() > campaignEntity.getCartLimit()) {
          //if addOneByOne method gives last items on stock as gift and sale, merge them and set to sale count
          if (suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount() >= itemStock) {
            suitableSaleAndGiftCount.setSaleCount(suitableSaleAndGiftCount.getSaleCount() + suitableSaleAndGiftCount.getGiftCount());
          }

          int remainingGiftCount = (campaignEntity.getCartLimit() - (actualTotalGiftCount / campaignEntity.getGiftCount())) * campaignEntity.getGiftCount();
          if (remainingGiftCount == 0) {
            suitableSaleAndGiftCount.setGiftCount(remainingGiftCount);
          }
        }

        cartEntity = addItemToCartEntity(cartEntity, campaignEntity,campaignEntity.getItemId(), variantId, suitableSaleAndGiftCount);
      }
    } else {
      SuitableSaleAndGiftCount suitableSaleAndGiftCount = defineSuitableSaleAndGiftCount(campaignEntity, desiredSaleCount, itemStock, optionalVariant);
      cartEntity = addItemToCartEntity(cartEntity, campaignEntity,campaignEntity.getItemId(), variantId, suitableSaleAndGiftCount);
    }

    //is item on cart
    /*Optional<CartItem> optionalCartItem = getCartItem(cartEntity, campaignEntity.getItemId(), variantId);
    if (optionalCartItem.isPresent()) {
      int itemIndex = cartEntity.getCartItems().indexOf(optionalCartItem.get());
      int itemOnCart = optionalCartItem.get().getSaleCount();
      int updatedSaleCount = desiredSaleCount + itemOnCart;
      int giftCount = calculateGiftCount(campaignEntity, updatedSaleCount);
      boolean isStockAvailable = isStockAvailable(campaignEntity.getItemId(), giftCount + updatedSaleCount);

      //check item variant status and stock availability
      if (optionalVariant.isPresent()) {
        itemStock = optionalCartItem.get().getVariant().getStock();
        isStockAvailable = isStockAvailable && (itemStock >= optionalCartItem.get().getSaleCount() + desiredSaleCount + giftCount);
      }

      SuitableSaleAndGiftCount suitableSaleAndGiftCount = SuitableSaleAndGiftCount.builder()
          .saleCount(updatedSaleCount)
          .giftCount(giftCount)
          .build();

      //check stock availability
      if (!isStockAvailable || updatedSaleCount < 0) {
        //stock is not available for direct addition, try one by one
        suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, itemOnCart, desiredSaleCount, itemStock);
      }

      optionalCartItem.get().setPrice(suitableSaleAndGiftCount.getSaleCount() * itemPrice);
      optionalCartItem.get().setSaleCount(suitableSaleAndGiftCount.getSaleCount());
      optionalCartItem.get().setUpdatedAt(Instant.now().toEpochMilli());
      optionalCartItem.get().setCampaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()));

      cartEntity.getCartItems().remove(itemIndex);
      cartEntity.getCartItems().add(itemIndex, optionalCartItem.get());
    } else { //item not found on cart
      int giftCount = calculateGiftCount(campaignEntity, desiredSaleCount);
      boolean isStockAvailable = isStockAvailable(campaignEntity.getItemId(), giftCount + desiredSaleCount);

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

      cartEntity
          .getCartItems()
          .add(CartItem.builder()
              .itemId(campaignEntity.getItemId())
              .sellerId(sellerId)
              .saleCount(suitableSaleAndGiftCount.getSaleCount())
              .addedAt(Instant.now().toEpochMilli())
              .hasCampaign(true)
              .campaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()))
              .price(itemPrice * suitableSaleAndGiftCount.getSaleCount())
              .hasVariant(optionalVariant.isPresent())
              .variant(optionalVariant.orElse(null))
              .build());
    }*/
    return cartEntity;
  }

  private CartEntity addItemToCartEntity(CartEntity cartEntity, CampaignEntity campaignEntity, int itemId, int variantId, SuitableSaleAndGiftCount suitableSaleAndGiftCount){
    int sellerId = getSellerIdByItemId(itemId);
    double itemPrice = getItemPrice(itemId);
    Optional<Variant> optionalVariant = getItemVariant(itemId, variantId);

    cartEntity
        .getCartItems()
        .add(CartItem.builder()
            .itemId(campaignEntity.getItemId())
            .sellerId(sellerId)
            .saleCount(suitableSaleAndGiftCount.getSaleCount())
            .addedAt(Instant.now().toEpochMilli())
            .hasCampaign(true)
            .campaignParams(prepareCampaignParams(campaignEntity, suitableSaleAndGiftCount.getGiftCount(), suitableSaleAndGiftCount.getSaleCount()))
            .price(itemPrice * suitableSaleAndGiftCount.getSaleCount())
            .hasVariant(optionalVariant.isPresent())
            .variant(optionalVariant.orElse(null))
            .build());

    return cartEntity;
  }

  private SuitableSaleAndGiftCount defineSuitableSaleAndGiftCount(CampaignEntity campaignEntity, int desiredSaleCount, int itemStock, Optional<Variant> optionalVariant){
    int giftCount = calculateGiftCount(campaignEntity, desiredSaleCount);
    boolean isStockAvailable = isStockAvailable(campaignEntity.getItemId(), giftCount + desiredSaleCount);

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

    return suitableSaleAndGiftCount;
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
          .filter(cartItem -> cartItem.getItemId().equals(campaignEntity.getItemId()))
          .findFirst();
      if (optionalCartItem.isPresent()) {
        itemOnCart = optionalCartItem.get().getSaleCount();
        giftOnCart = optionalCartItem.get().getCampaignParams().getActualGiftCount();
      }
    }

    int itemStock = getItemStock(campaignEntity.getItemId());

    if (itemCount + itemOnCart + giftOnCart > itemStock) {
      SuitableSaleAndGiftCount suitableSaleAndGiftCount = addOneByOneToCart(campaignEntity, itemOnCart, itemCount, getItemStock(campaignEntity.getItemId()));
      return suitableSaleAndGiftCount.getGiftCount() >= campaignEntity.getGiftCount();
    }
    return true;
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

  private Optional<CartItem> getCartItem(CartEntity cartEntity, int itemId, int variantId) {
    Optional<CartItem> optionalCartItem = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
        .findFirst();

    if (variantId != 0 && isItemHasVariant(itemId, variantId)) {
      optionalCartItem
          .filter(cartItem -> cartItem.getVariant().getId().equals(variantId));
    }
    return optionalCartItem;
  }

  private Optional<List<CartItem>> getCartItems(CartEntity cartEntity, int itemId) {
    List<CartItem> cartItems = cartEntity
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
        .collect(Collectors.toList());

    return Optional.ofNullable(cartItems);
  }

  private CartEntity addItemToCart(String cartId, int itemId, int desiredSaleCount, int variantId) {
    CartEntity cartEntity = getCartEntityById(cartId);
    int sellerId = getSellerIdByItemId(itemId);
    double itemPrice = getItemPrice(itemId);
    int itemStock = getItemStock(itemId);
    Optional<Variant> optionalVariant = getItemVariant(itemId, variantId);

    Optional<List<CartItem>> optionalCartItems = getCartItems(cartEntity, itemId);
    //checks item on cart
    if (optionalCartItems.isPresent()) {
      Optional<CartItem> optionalCartItem = Optional.empty();
      if (optionalVariant.isPresent()) {
        optionalCartItem = optionalCartItems
            .get()
            .stream()
            .filter(cartItem -> cartItem.getVariant().getId().equals(variantId))
            .findFirst();
      } else {
        optionalCartItem = optionalCartItems.get().stream().findFirst();
      }

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
          desiredSaleCount = addOneByOneToCart(itemOnCart, itemStock); //TODO stock - onCart
        }
        cartEntity.getCartItems().remove(itemIndex);
        cartEntity.getCartItems().add(itemIndex, setCountAndPriceToCartItem(optionalCartItem.get(), desiredSaleCount, itemPrice));
      } else {
        cartEntity = addItemToCartEntity(cartEntity, itemId, variantId, desiredSaleCount);
      }
    } else {
      cartEntity = addItemToCartEntity(cartEntity, itemId, variantId, desiredSaleCount);
    }
    return cartEntity;
  }

  private CartEntity addItemToCartEntity(CartEntity cartEntity, int itemId, int variantId, int desiredSaleCount) {
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
            .itemId(itemId)
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
        .filter(cartItem -> cartItem.getItemId().equals(itemId))
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
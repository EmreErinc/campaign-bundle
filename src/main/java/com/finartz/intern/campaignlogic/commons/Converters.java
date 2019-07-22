package com.finartz.intern.campaignlogic.commons;

import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.request.*;
import com.finartz.intern.campaignlogic.model.response.*;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.security.Utils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Converters {
  private Converters() {
  }

  public static AccountEntity registerRequestToUserEntity(RegisterRequest request) {
    return AccountEntity.builder()
        .name(request.getName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(Utils.encrypt(request.getPassword()))
        .createdAt(Instant.now().toEpochMilli())
        .role(Role.USER)
        .build();
  }

  public static AccountEntity registerSellerRequestToUserEntity(RegisterRequest request) {
    return AccountEntity.builder()
        .name(request.getName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(Utils.encrypt(request.getPassword()))
        .createdAt(Instant.now().toEpochMilli())
        .role(Role.SELLER)
        .build();
  }

  public static LoginResponse accountToLoginResponse(AccountEntity accountEntity) {
    return LoginResponse.builder()
        .name(accountEntity.getName())
        .lastName(accountEntity.getLastName())
        .id(accountEntity.getId())
        .build();
  }

  public static SellerEntity addSellerRequestToSellerEntity(int accountId, AddSellerRequest request) {
    return SellerEntity.builder()
        .name(request.getName())
        .address(request.getAddress())
        .createdAt(Instant.now().toEpochMilli())
        .status(SellerStatus.ACTIVE)
        .accountId(accountId)
        .build();
  }

  public static SellerResponse sellerEntityToSellerResponse(SellerEntity sellerEntity) {
    return SellerResponse.builder()
        .id(sellerEntity.getId())
        .name(sellerEntity.getName())
        .address(sellerEntity.getAddress())
        .build();
  }

  public static ItemEntity addItemRequestToItemEntity(AddItemRequest request, int sellerId) {
    return ItemEntity.builder()
        .name(request.getName())
        .price(request.getPrice())
        .description(request.getDescription())
        .createdAt(Instant.now().toEpochMilli())
        .sellerId(sellerId)
        .cargoType(request.getCargoType())
        .cargoCompany(request.getCargoCompany())
        .stock(request.getStock())
        .build();
  }

  public static ItemResponse itemEntityToItemResponse(ItemEntity itemEntity, List<Variant> variants) {
    return ItemResponse.builder()
        .productId(itemEntity.getId())
        .name(itemEntity.getName())
        .price(itemEntity.getPrice())
        .cargoType(itemEntity.getCargoType())
        .description(itemEntity.getDescription())
        .variants(variants)
        .build();
  }

  public static ItemDetail itemEntityToItemDetail(ItemEntity itemEntity, Badge badge, List<Variant> variants) {
    return ItemDetail.builder()
        .id(itemEntity.getId().toString())
        .name(itemEntity.getName())
        .description(itemEntity.getDescription())
        .cargoType(itemEntity.getCargoType())
        .price(itemEntity.getPrice())
        .badge(badge)
        .variants(variants)
        .build();
  }

  public static ItemSummary itemEntityToItemSummary(ItemEntity itemEntity, Badge badge) {
    return ItemSummary.builder()
        .id(itemEntity.getId())
        .name(itemEntity.getName())
        .cargoType(itemEntity.getCargoType())
        .price(itemEntity.getPrice())
        .badge(badge)
        .build();
  }

  public static CampaignEntity addCampaignRequestToCampaignEntity(AddCampaignRequest request, int sellerId) {
    return CampaignEntity.builder()
        .title(request.getTitle())
        .startAt(request.getStartAt())
        .endAt(request.getEndAt())
        .campaignLimit(request.getCampaignLimit())
        .cartLimit(request.getCartLimit())
        .createdAt(Instant.now().toEpochMilli())
        .requirementCount(request.getRequirement())
        .giftCount(request.getGift())
        .status(CampaignStatus.ACTIVE)
        .productId(request.getProductId())
        .sellerId(sellerId)
        .build();
  }

  public static CampaignResponse campaignEntityToCampaignResponse(CampaignEntity campaignEntity, Badge badge) {
    return CampaignResponse.builder()
        .id(campaignEntity.getId())
        .productId(campaignEntity.getProductId())
        .sellerId(campaignEntity.getSellerId())
        .title(campaignEntity.getTitle())
        .campaignLimit(campaignEntity.getCampaignLimit())
        .cartLimit(campaignEntity.getCartLimit())
        .startAt(campaignEntity.getStartAt())
        .endAt(campaignEntity.getEndAt())
        .badge(badge)
        .status(campaignEntity.getStatus())
        .build();
  }

  public static CampaignSummary campaignEntityToCampaignSummary(CampaignEntity campaignEntity, Badge badge) {
    return CampaignSummary.builder()
        .campaignId(campaignEntity.getId())
        .title(campaignEntity.getTitle())
        .startAt(campaignEntity.getStartAt())
        .endAt(campaignEntity.getEndAt())
        .campaignLimit(campaignEntity.getCampaignLimit())
        .cartLimit(campaignEntity.getCartLimit())
        .status(campaignEntity.getStatus())
        .badge(badge)
        .build();
  }

  public static CartResponse cartEntityToCartResponse(CartEntity cartEntity) {
    return CartResponse.builder()
        .itemList(cartItemToCartItemDto(cartEntity.getCartItems()))
        .build();
  }

  public static List<CartItemDto> cartItemToCartItemDto(List<CartItem> cartItems) {
    return cartItems
        .stream()
        .map(cartItem -> CartItemDto.builder()
            .productId(cartItem.getProductId())
            .saleCount(cartItem.getSaleCount())
            .sellerId(cartItem.getSellerId())
            .hasCampaign(cartItem.getHasCampaign())
            .campaignParams(cartItem.getCampaignParams())
            .hasVariant(cartItem.getHasVariant())
            .variant(cartItem.getVariant())
            .price(cartItem.getPrice())
            .desiredSaleCount(cartItem.getDesiredSaleCount())
            .message(cartItem.getMessageKey() == null ? Messages.EMPTY.getValue() : Messages.values()[cartItem.getMessageKey()].getValue())
            .build())
        .collect(Collectors.toList());

  }

  public static VariantEntity prepareItemVariant(int itemId, AddVariantRequest request) {
    return VariantEntity.builder()
        .productId(itemId)
        .price(request.getPrice() == null ? 0.0D : request.getPrice())
        .stock(request.getStock())
        .build();
  }

  public static Variant variantEntityToVariant(VariantEntity variantEntity, List<VariantSpec> variantSpecs) {
    return Variant.builder()
        .id(variantEntity.getId())
        .stock(variantEntity.getStock())
        .price(variantEntity.getPrice())
        .variantSpecs(variantSpecs)
        .build();
  }

  public static CartDto convertToCartDto(int accountId, String cartId, AddItemToCartRequest request){
    return CartDto.builder()
        .accountId(accountId)
        .cartId(cartId)
        .productId(request.getProductId())
        .desiredCount(request.getCount())
        .variantId(Optional.ofNullable(request.getVariantId()))
        .build();
  }

  public static CartDto convertToCartDto(int accountId, String cartId, CartItemIncrementRequest request){
    return CartDto.builder()
        .accountId(accountId)
        .cartId(cartId)
        .productId(request.getProductId())
        .desiredCount(1)
        .variantId(Optional.ofNullable(request.getVariantId()))
        .build();
  }

  public static CartDto convertToCartDto(int accountId, String cartId, CartItemDecrementRequest request){
    return CartDto.builder()
        .accountId(accountId)
        .cartId(cartId)
        .productId(request.getProductId())
        .desiredCount(-1)
        .variantId(Optional.ofNullable(request.getVariantId()))
        .build();
  }
}
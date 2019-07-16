package com.finartz.intern.campaignlogic.commons;

import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.*;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.security.Utils;

import java.time.Instant;
import java.util.List;
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
        .itemId(itemEntity.getId())
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
        .expectedGiftCount(request.getGift())
        .status(CampaignStatus.ACTIVE)
        .itemId(request.getItemId())
        .sellerId(sellerId)
        .build();
  }

  public static CampaignResponse campaignEntityToCampaignResponse(CampaignEntity campaignEntity, Badge badge) {
    return CampaignResponse.builder()
        .id(campaignEntity.getId())
        .itemId(campaignEntity.getItemId())
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

  public static CartResponse cartEntityToCartResponse(CartEntity cartEntity){
    return CartResponse.builder()
        .itemList(cartEntity.getCartItems())
        .build();
  }

  public static VariantEntity prepareItemVariant(int itemId,Variant variant){
    return VariantEntity.builder()
        .itemId(itemId)
        .specification(variant.getSpecification())
        .specificationDetail(variant.getDetail())
        .price(variant.getPrice() == null ? 0.0D : variant.getPrice())
        .build();
  }

  public static Variant variantEntityToVariant(VariantEntity variantEntity){
    return Variant.builder()
        .specification(variantEntity.getSpecification())
        .detail(variantEntity.getSpecificationDetail())
        .price(variantEntity.getPrice())
        .build();
  }

  public static List<Variant> variantEntitiesToVariants(List<VariantEntity> variantEntities){
    return variantEntities
        .stream()
        .map(Converters::variantEntityToVariant)
        .collect(Collectors.toList());
  }
}
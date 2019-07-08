package com.finartz.intern.campaignlogic.commons;

import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.request.*;
import com.finartz.intern.campaignlogic.model.response.*;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.security.Utils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {
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
        .id(accountEntity.getId().toString())
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
        .id(sellerEntity.getId().toString())
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

  public static ItemResponse itemEntityToItemResponse(ItemEntity itemEntity) {
    return ItemResponse.builder()
        .id(itemEntity.getId().toString())
        .name(itemEntity.getName())
        .price(itemEntity.getPrice())
        .cargoType(itemEntity.getCargoType())
        .description(itemEntity.getDescription())
        .build();
  }

  public static ItemDetail itemEntityToItemDetail(ItemEntity itemEntity, Badge badge) {
    return ItemDetail.builder()
        .id(itemEntity.getId().toString())
        .name(itemEntity.getName())
        .description(itemEntity.getDescription())
        .cargoType(itemEntity.getCargoType())
        .price(itemEntity.getPrice())
        .badge(badge)
        .build();
  }

  public static List<ItemSummary> itemEntitiesToItemSummaries(List<ItemEntity> itemEntities) {
    return itemEntities
        .stream()
        .map(Converters::itemEntityToItemSummary)
        .collect(Collectors.toList());
  }

  public static ItemSummary itemEntityToItemSummary(ItemEntity itemEntity) {
    return ItemSummary.builder()
        .id(itemEntity.getId().toString())
        .name(itemEntity.getName())
        .cargoType(itemEntity.getCargoType())
        .price(itemEntity.getPrice())
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

  public static CampaignResponse campaignEntityToCampaignResponse(CampaignEntity campaignEntity) {
    return CampaignResponse.builder()
        .itemId(campaignEntity.getItemId().toString())
        .sellerId(campaignEntity.getSellerId().toString())
        .title(campaignEntity.getTitle())
        .campaignLimit(campaignEntity.getCampaignLimit())
        .cartLimit(campaignEntity.getCartLimit())
        .startAt(campaignEntity.getStartAt())
        .endAt(campaignEntity.getEndAt())
        .requirementCount(campaignEntity.getRequirementCount())
        .giftCount(campaignEntity.getExpectedGiftCount())
        .status(campaignEntity.getStatus())
        .build();
  }

  public static List<CampaignSummary> campaignEntitiesToCampaignSummaries(List<CampaignEntity> campaignEntities) {
    return campaignEntities
        .stream()
        .map(Converters::campaignEntityToCampaignSummary)
        .collect(Collectors.toList());
  }

  public static CampaignSummary campaignEntityToCampaignSummary(CampaignEntity campaignEntity) {
    return CampaignSummary.builder()
        .title(campaignEntity.getTitle())
        .startAt(campaignEntity.getStartAt())
        .endAt(campaignEntity.getEndAt())
        .campaignLimit(campaignEntity.getCampaignLimit())
        .cartLimit(campaignEntity.getCartLimit())
        .status(campaignEntity.getStatus())
        .build();
  }

  /*public static SalesEntity saleItemRequestToSaleEntity(CartItem saleItem, int accountId){
    return SalesEntity.builder()
        .itemId(saleItem.getItemId())
        .count(saleItem.getCount())
        .ownerId(accountId)
        .price(saleItem.getPrice())
        .soldAt(Instant.now().toEpochMilli())
        .build();
  }*/

  public static CartResponse cartEntityToCartResponse(CartEntity cartEntity){
        //TODO fix it list

    return CartResponse.builder()
        .itemList(cartEntity.getCartItems())
        .build();
  }
}

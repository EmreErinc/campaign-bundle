package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.response.CartControlResponse;
import com.finartz.intern.campaignlogic.model.response.ControlResponse;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.finartz.intern.campaignlogic.security.Errors.*;

@Slf4j
@Service
public class BaseServiceImpl implements BaseService {
  private AccountRepository accountRepository;
  private SellerRepository sellerRepository;
  private CampaignRepository campaignRepository;
  private ItemRepository itemRepository;
  private SalesRepository salesRepository;
  private CartRepository cartRepository;
  private VariantRepository variantRepository;
  private VariantSpecRepository variantSpecRepository;
  private SpecDataRepository specDataRepository;
  private SpecDetailRepository specDetailRepository;

  @Autowired
  public BaseServiceImpl(AccountRepository accountRepository,
                         SellerRepository sellerRepository,
                         CampaignRepository campaignRepository,
                         ItemRepository itemRepository,
                         SalesRepository salesRepository,
                         CartRepository cartRepository,
                         VariantRepository variantRepository,
                         VariantSpecRepository variantSpecRepository,
                         SpecDataRepository specDataRepository,
                         SpecDetailRepository specDetailRepository) {
    this.accountRepository = accountRepository;
    this.sellerRepository = sellerRepository;
    this.campaignRepository = campaignRepository;
    this.itemRepository = itemRepository;
    this.salesRepository = salesRepository;
    this.cartRepository = cartRepository;
    this.variantRepository = variantRepository;
    this.variantSpecRepository = variantSpecRepository;
    this.specDataRepository = specDataRepository;
    this.specDetailRepository = specDetailRepository;
  }

  @Override
  public Role getRoleByAccountId(int accountId) {
    Optional<AccountEntity> optionalAccountEntity = accountRepository.findById(accountId);
    if (!optionalAccountEntity.isPresent()) {
      throw new ApplicationContextException(ACCOUNT_NOT_FOUND);
    }
    return optionalAccountEntity.get().getRole();
  }

  @Override
  public Integer getSellerIdByAccountId(int accountId) {
    Optional<SellerEntity> optionalSellerEntity = sellerRepository.findByAccountId(accountId);
    if (!optionalSellerEntity.isPresent()) {
      throw new ApplicationContextException(SELLER_NOT_FOUND);
    }
    return optionalSellerEntity.get().getId();
  }

  @Override
  public Optional<CampaignEntity> getCampaignByProductId(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()){
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return campaignRepository.findByProductId(itemId);
  }

  @Override
  public Integer getSellerIdByProductId(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()) {
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return optionalItemEntity.get().getSellerId();
  }

  @Override
  public Boolean isCampaignAvailableGetByItemId(int itemId) {
    return campaignRepository
        .findByProductIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(itemId,
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli())
        .isPresent();
  }

  @Override
  public Boolean isCampaignAvailableGetById(int campaignId) {
    return campaignRepository
        .findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(campaignId,
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli())
        .isPresent();
  }

  @Override
  public CampaignEntity getCampaignEntity(int campaignId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findById(campaignId);
    if (!optionalCampaignEntity.isPresent()) {
      throw new ApplicationContextException(CAMPAIGN_NOT_FOUND);
    }
    return optionalCampaignEntity.get();
  }

  @Override
  public Boolean isStockAvailable(int itemId, int expectedSaleAndGiftCount) {
    return (getProductStock(itemId) - expectedSaleAndGiftCount >= 0);
  }

  public Boolean isItemAvailable(CartDto cartDto) {
    if (!isCampaignLimitAvailableForAccount(cartDto.getAccountId(), cartDto.getProductId())) {
      return false;
    }

    if (!cartLimitAvailability(cartDto.getCartId(), cartDto.getProductId(), cartDto.getDesiredCount())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean cartLimitAvailability(String cartId, int itemId, int itemCount) {
    CartEntity cartEntity = getCartEntityById(cartId);
    Optional<CampaignEntity> optionalCampaignEntity = getCampaignByProductId(itemId);
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

  @Override
  public Integer calculateGiftCount(CampaignEntity campaignEntity, int saleCount) {
    int requirementCount = campaignEntity.getRequirementCount();
    int quotient = saleCount / requirementCount;
    int possibleGiftCount = quotient * campaignEntity.getGiftCount();

    if (possibleGiftCount > (campaignEntity.getGiftCount() * campaignEntity.getCartLimit())) {
      return (campaignEntity.getGiftCount() * campaignEntity.getCartLimit());
    }
    return possibleGiftCount;
  }

  @Override
  public Boolean isItemHasCampaign(int itemId) {
    return campaignRepository
        .findByProductId(itemId)
        .isPresent();
  }

  @Override
  public ItemEntity getItemEntity(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()) {
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return optionalItemEntity.get();
  }

  @Override
  public Integer getProductStock(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()) {
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return optionalItemEntity.get().getStock();
  }

  @Override
  public Boolean isCampaignLimitAvailableForAccount(int accountId, int itemId) {
    //this means there is no campaign on item
    if (!campaignRepository.existsByProductId(itemId)) {
      return true;
    }

    Optional<Integer> campaignItemUsageCount = getCampaignProductUsageCount(accountId, itemId);
    //this means user did not use from campaign
    if (!campaignItemUsageCount.isPresent()) {
      return true;
    }

    return campaignItemUsageCount.get() < getCampaignLimit(itemId);
  }

  @Override
  public Optional<Integer> getCampaignProductUsageCount(int accountId, int itemId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerIdAndProductId(accountId, itemId);
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByProductId(itemId);
    if (!optionalCampaignEntity.isPresent()) {
      throw new ApplicationContextException(CAMPAIGN_NOT_FOUND);
    }

    AtomicInteger cartLimit = new AtomicInteger();
    optionalCampaignEntity.ifPresent(campaignEntity ->
        cartLimit.set(campaignEntity.getCartLimit()));

    AtomicInteger sumOfSales = new AtomicInteger();
    optionalSalesEntities.ifPresent(salesEntities ->
        sumOfSales.set(salesEntities
            .stream()
            .filter(salesEntity -> salesEntity.getSaleCount() != null)
            .collect(Collectors.toList())
            .stream()
            .mapToInt(SalesEntity::getSaleCount).sum())
    );

    if (cartLimit.intValue() == 0 || sumOfSales.intValue() == 0) {
      return Optional.empty();
    }
    return Optional.of(sumOfSales.intValue() / cartLimit.intValue());
  }

  @Override
  public Boolean userAvailableForCampaign(int accountId, int campaignId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerId(accountId);
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findById(campaignId);
    return optionalCampaignEntity
        //map campaign entity with the compatible sale items
        .map(campaignEntity ->
            optionalSalesEntities.map(salesEntities ->
                salesEntities
                    .stream()
                    .filter(salesEntity -> salesEntity.getProductId().equals(campaignEntity.getProductId()))
                    .collect(Collectors.toList())
                    .size()))
        .orElse(Optional.of(0))
        .map(campaignUsageCount -> optionalCampaignEntity.get().getCampaignLimit() <= campaignUsageCount)
        .orElse(false);
  }

  @Override
  public Integer getCampaignLimit(int itemId) {
    Optional<CampaignEntity> optionalCampaignEntity = campaignRepository.findByProductId(itemId);
    if (!optionalCampaignEntity.isPresent()) {
      return 0;
    }
    return optionalCampaignEntity.get().getCampaignLimit();
  }

  @Override
  public CartEntity getCartEntityById(String cartId) {
    Optional<CartEntity> optionalCartEntity = cartRepository.findCart(cartId);
    if (!optionalCartEntity.isPresent()) {
      throw new ApplicationContextException(CART_NOT_FOUND);
    }
    return optionalCartEntity.get();
  }

  @Override
  public void saveAsSoldCart(CartEntity cartEntity) {
    cartRepository
        .saveAsSold(SoldCartEntity.builder()
            .relatedCartId(cartEntity.getId())
            .accountId(cartEntity.getAccountId())
            .cartItems(cartEntity.getCartItems())
            .build());

    cartRepository
        .updateCart(CartEntity.builder()
            .id(cartEntity.getId())
            .accountId(cartEntity.getAccountId())
            .cartItems(new ArrayList<>())
            .build());
  }

  @Override
  public Double getProductPrice(int itemId) {
    Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
    if (!optionalItemEntity.isPresent()) {
      throw new ApplicationContextException(ITEM_NOT_FOUND);
    }
    return optionalItemEntity.get().getPrice();
  }

  @Override
  public Badge getBadgeByProductId(int itemId) {
    return extractCampaignEntityBadge(campaignRepository.findByProductId(itemId));
  }

  @Override
  public Badge getBadgeByCampaignId(int campaignId) {
    return extractCampaignEntityBadge(campaignRepository.findById(campaignId));
  }

  private Badge extractCampaignEntityBadge(Optional<CampaignEntity> optionalCampaignEntity) {
    return optionalCampaignEntity
        .map(campaignEntity ->
            Badge.builder()
                .requirement(campaignEntity.getRequirementCount())
                .gift(campaignEntity.getGiftCount())
                .build())
        .orElseGet(() ->
            Badge.builder()
                .build());
  }

  @Override
  public List<CampaignEntity> getUsedCampaignsByUserId(int userId) {
    Optional<List<SalesEntity>> optionalSalesEntities = salesRepository.findByOwnerId(userId);
    List<CampaignEntity> campaignEntities = new ArrayList<>();

    optionalSalesEntities.ifPresent(salesEntities ->
        salesEntities
            .forEach(salesEntity ->
                getCampaignByProductId(salesEntity.getProductId())
                    .ifPresent(campaignEntities::add)));

    return campaignEntities;
  }

  @Override
  public CampaignSummary prepareCampaignEntityToList(int accountId, CampaignEntity campaignEntity) {
    Badge badge = Badge.builder().build();
    if (!userAvailableForCampaign(accountId, campaignEntity.getId())) {
      badge = getBadgeByCampaignId(campaignEntity.getId());
    }
    return Converters.campaignEntityToCampaignSummary(campaignEntity, badge);
  }

  @Override
  public Boolean isProductOnCart(String cartId, int itemId) {
    return getCartEntityById(cartId)
        .getCartItems()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemId));
  }

  @Override
  public Integer getProductCountOnCart(String cartId, int itemId, Optional<Integer> optionalVariantId) {
    Optional<Variant> optionalVariant = Optional.empty();

    if (optionalVariantId.isPresent()) {
      optionalVariant = getProductVariant(itemId, optionalVariantId.get());
    }

    List<CartItem> collect = getCartEntityById(cartId)
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(itemId))
        .collect(Collectors.toList());

    if (optionalVariant.isPresent()) {
      return collect.stream().filter(cartItem -> cartItem.getVariant().getId().equals(optionalVariantId.get())).mapToInt(CartItem::getSaleCount).sum();
    }

    return collect.get(0).getSaleCount();
  }

  @Override
  public Integer getTotalProductCountOnCart(String cartId, int itemId) {
    return getCartEntityById(cartId)
        .getCartItems()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(itemId))
        .mapToInt(CartItem::getSaleCount)
        .sum();
  }

  @Override
  public Variant addVariant(VariantEntity variantEntity) {
    VariantEntity savedVariantEntity = variantRepository.save(variantEntity);
    return Converters
        .variantEntityToVariant(savedVariantEntity,
            getProductVariantSpecs(savedVariantEntity.getProductId(), savedVariantEntity.getId()));
  }

  @Override
  public Optional<List<Variant>> getProductVariants(int itemId) {
    Optional<List<VariantEntity>> optionalVariantEntities = variantRepository.findByProductId(itemId);
    if (!optionalVariantEntities.isPresent()) {
      return Optional.empty();
    }

    return Optional.of(optionalVariantEntities
        .get()
        .stream()
        .map(variantEntity -> Variant.builder()
            .id(variantEntity.getId())
            .price(variantEntity.getPrice())
            .stock(variantEntity.getStock())
            .variantSpecs(getProductVariantSpecs(variantEntity.getProductId(), variantEntity.getId()))
            .build())
        .collect(Collectors.toList()));
  }

  @Override
  public Optional<Variant> getProductVariant(int itemId, int variantId) {
    Optional<VariantEntity> optionalVariantEntity = variantRepository.findById(variantId);
    if (!optionalVariantEntity.isPresent() || !optionalVariantEntity.get().getProductId().equals(itemId)) {
      return Optional.empty();
    }

    return Optional.of(Variant.builder()
        .id(optionalVariantEntity.get().getId())
        .price(optionalVariantEntity.get().getPrice())
        .stock(optionalVariantEntity.get().getStock())
        .variantSpecs(getProductVariantSpecs(itemId, variantId))
        .build());
  }

  @Override
  public List<VariantSpec> getProductVariantSpecs(int itemId, int variantId) {
    Optional<List<VariantSpecEntity>> optionalVariantEntities = variantSpecRepository.findByProductIdAndVariantId(itemId, variantId);
    if (!optionalVariantEntities.isPresent()) {
      return new ArrayList<>();
    }
    return optionalVariantEntities
        .get()
        .stream()
        .map(variantSpecEntity -> {
          VariantSpec variantSpec = VariantSpec.builder().build();

          variantSpec.setId(variantSpecEntity.getId());

          //find and set spec data
          Optional<SpecDataEntity> optionalSpecDataEntity = specDataRepository.findById(variantSpecEntity.getSpecDataId());
          variantSpec.setSpecData(optionalSpecDataEntity.get().getData());

          //find and set spec detail
          Optional<SpecDetailEntity> optionalSpecDetailEntity = specDetailRepository.findById(optionalSpecDataEntity.get().getSpecDetailId());
          variantSpec.setSpecDetail(optionalSpecDetailEntity.get().getDetail());

          return variantSpec;
        })
        .collect(Collectors.toList());
  }

  @Override
  public Integer getProductVariantStock(int variantId) {
    Optional<VariantEntity> optionalVariantEntity = variantRepository.findById(variantId);
    if (!optionalVariantEntity.isPresent()) {
      throw new ApplicationContextException("Variant Not Found");
    }
    return optionalVariantEntity.get().getStock();
  }

  @Override
  public ControlResponse getUnfitCartItems(CartEntity cartEntity) {
    return ControlResponse.builder()
        .cartControlResponses(controlCartItems(cartEntity)
            .stream()
            .filter(response -> !response.getIsAvailableForContinue())
            .sorted(Comparator.comparing(response -> response.getCauseMessage().hashCode()))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public List<CartControlResponse> controlCartItems(CartEntity cartEntity) {
    return cartEntity
        .getCartItems()
        .stream()
        .map(cartItem -> {
          CartControlResponse cartControlResponse = CartControlResponse.builder().build();

          cartControlResponse.setIsAvailableForContinue(true);
          cartControlResponse.setCauseMessage(Messages.CART_AVAILABLE.getValue());
          cartControlResponse.setDesiredSaleCount(cartItem.getDesiredSaleCount());
          cartControlResponse.setProductId(cartItem.getProductId());
          cartControlResponse.setVariantId(cartItem.getHasVariant() ? cartItem.getVariant().getId() : 0);

          //check general item stock
          if (!isStockAvailable(cartItem.getProductId(), cartItem.getSaleCount())) {
            setMessageAsNotAvailable(cartControlResponse, Messages.PRODUCT_STOCK_NOT_AVAILABLE);
          }

          //check item variant stock
          if (cartItem.getHasVariant()) {
            //check campaign status
            checkItemVariantStock(cartItem, cartControlResponse);
          }

          //check item campaign params
          if (cartItem.getHasCampaign()) {
            checkCampaignItemStock(cartItem, cartControlResponse);
          }
          return cartControlResponse;
        }).collect(Collectors.toList());
  }

  private void checkCampaignItemStock(CartItem cartItem, CartControlResponse cartControlResponse) {
    if ((cartItem.getCampaignParams().getTotalItemCount() + cartItem.getCampaignParams().getActualGiftCount()) > getProductStock(cartItem.getProductId())) {
      setMessageAsNotAvailable(cartControlResponse, Messages.PRODUCT_STOCK_NOT_AVAILABLE);
    } else if (cartItem.getDesiredSaleCount() > cartItem.getCampaignParams().getTotalItemCount() && (cartItem.getDesiredSaleCount() <= getProductStock(cartItem.getProductId()))) {
      setMessageAsNotAvailable(cartControlResponse, Messages.PRODUCT_STOCK_INCREASE);
    }
  }

  private void checkItemVariantStock(CartItem cartItem, CartControlResponse cartControlResponse) {
    if (cartItem.getHasCampaign()) { //campaign exists for item
      checkCampaignItemVariantStock(cartItem, cartControlResponse);
    } else if (cartItem.getSaleCount() > getProductVariantStock(cartItem.getVariant().getId())) { //campaign not exists for item
      setMessageAsNotAvailable(cartControlResponse, Messages.PRODUCT_STOCK_VARIANT_NOT_AVAILABLE);
    } else if (cartItem.getDesiredSaleCount() > cartItem.getSaleCount() && cartItem.getDesiredSaleCount() <= getProductVariantStock(cartItem.getVariant().getId())) {
      setMessageAsNotAvailable(cartControlResponse, Messages.PRODUCT_STOCK_VARIANT_INCREASE);
    }
  }

  private void checkCampaignItemVariantStock(CartItem cartItem, CartControlResponse cartControlResponse) {
    if ((cartItem.getCampaignParams().getTotalItemCount() + cartItem.getCampaignParams().getActualGiftCount()) > getProductVariantStock(cartItem.getVariant().getId())) {
      setMessageAsNotAvailable(cartControlResponse, Messages.PRODUCT_STOCK_VARIANT_NOT_AVAILABLE);
    } else if (cartItem.getDesiredSaleCount() > cartItem.getCampaignParams().getTotalItemCount() && cartControlResponse.getDesiredSaleCount() < getProductVariantStock(cartItem.getVariant().getId())) {
      setMessageAsNotAvailable(cartControlResponse, Messages.PRODUCT_STOCK_VARIANT_INCREASE);
    }
  }

  private void setMessageAsNotAvailable(CartControlResponse cartControlResponse, Messages message) {
    cartControlResponse.setIsAvailableForContinue(false);
    cartControlResponse.setCauseMessage(message.getValue());
  }

  @Override
  public void decreaseItemStock(int itemId, Optional<Integer> variantId, int soldCount) {
    itemRepository.addStock(-soldCount, itemId);
    variantId.ifPresent(id -> variantRepository.addStock(-soldCount, id));
  }

  @Override
  public String getProductName(int itemId){
    return getItemEntity(itemId).getName();
  }
}
package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.CampaignResponse;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.repository.*;
import com.finartz.intern.campaignlogic.security.JwtTokenProvider;
import com.finartz.intern.campaignlogic.service.AccountService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
public abstract class BaseTestController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private CampaignRepository campaignRepository;

  @Autowired
  private SalesRepository salesRepository;

  @Autowired
  private VariantRepository variantRepository;

  @Autowired
  private VariantSpecRepository variantSpecRepository;

  @Autowired
  private SpecDetailRepository specDetailRepository;

  @Autowired
  private SpecDataRepository specDataRepository;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  public ItemGenerateResponse generateItemWithCampaign(int accountId, int stock, int cartLimit, int campaignLimit, int requirementCount, int giftCount) {
    String[] cargoList = {"MNG Kargo", "Sürat Kargo", "Yurtiçi Kargo", "PTT", "UPS"};

    //item creation
    ItemEntity itemEntity = ItemEntity.builder()
        .name("test-item-" + Instant.now().toEpochMilli())
        .description("item-description-1")
        .sellerId(accountId)
        .price(new Random().nextDouble() * 50D)
        .stock(stock)
        .cargoType(CargoType.values()[new Random().nextInt(2)])
        .cargoCompany(cargoList[new Random().nextInt(5)])
        .createdAt(1564128012966L)
        .build();

    itemEntity = itemRepository.save(itemEntity);

    //campaign creation
    CampaignEntity campaignEntity = CampaignEntity.builder()
        .title("custom campaign")
        .productId(itemEntity.getId())
        .cartLimit(cartLimit)
        .campaignLimit(campaignLimit)
        .createdAt(1564128135680L)
        .sellerId(accountId)
        .requirementCount(requirementCount)
        .giftCount(giftCount)
        .startAt(1562939866630L)
        .endAt(1577653200000L)
        .status(CampaignStatus.ACTIVE)
        .build();

    campaignEntity = campaignRepository.save(campaignEntity);

    return ItemGenerateResponse.builder()
        .productId(itemEntity.getId())
        .price(itemEntity.getPrice())
        .campaignId(campaignEntity.getId())
        .build();
  }

  public ItemGenerateResponse generateItemWithCampaignCanceledStatus(int accountId, int stock, int cartLimit, int campaignLimit, int requirementCount, int giftCount) {
    String[] cargoList = {"MNG Kargo", "Sürat Kargo", "Yurtiçi Kargo", "PTT", "UPS"};

    //item creation
    ItemEntity itemEntity = ItemEntity.builder()
        .name("test-item-" + Instant.now().toEpochMilli())
        .description("item-description-1")
        .sellerId(accountId)
        .price(new Random().nextDouble() * 50D)
        .stock(stock)
        .cargoType(CargoType.values()[new Random().nextInt(2)])
        .cargoCompany(cargoList[new Random().nextInt(5)])
        .createdAt(1564128012966L)
        .build();

    itemEntity = itemRepository.save(itemEntity);

    //campaign creation
    CampaignEntity campaignEntity = CampaignEntity.builder()
        .title("custom campaign")
        .productId(itemEntity.getId())
        .cartLimit(cartLimit)
        .campaignLimit(campaignLimit)
        .createdAt(1564128135680L)
        .sellerId(accountId)
        .requirementCount(requirementCount)
        .giftCount(giftCount)
        .startAt(1562939866630L)
        .endAt(1577653200000L)
        .status(CampaignStatus.CANCELED)
        .build();

    campaignEntity = campaignRepository.save(campaignEntity);

    return ItemGenerateResponse.builder()
        .productId(itemEntity.getId())
        .price(itemEntity.getPrice())
        .campaignId(campaignEntity.getId())
        .build();
  }

  public ItemResponse generateItem(int accountId) {
    String[] cargoList = {"MNG Kargo", "Sürat Kargo", "Yurtiçi Kargo", "PTT", "UPS"};

    //item creation
    ItemEntity itemEntity = ItemEntity.builder()
        .name("test-item-" + Instant.now().toEpochMilli())
        .description("item-description-1")
        .sellerId(accountId)
        .price(new Random().nextDouble() * 50D)
        .stock(20)
        .cargoType(CargoType.values()[new Random().nextInt(2)])
        .cargoCompany(cargoList[new Random().nextInt(5)])
        .createdAt(1564128012966L)
        .build();

    itemEntity = itemRepository.save(itemEntity);

    List<Variant> variants = new ArrayList<>();

    return Converters.itemEntityToItemResponse(itemEntity, variants);
  }

  public ItemResponse generateItem(int accountId, int stock) {
    String[] cargoList = {"MNG Kargo", "Sürat Kargo", "Yurtiçi Kargo", "PTT", "UPS"};

    //item creation
    ItemEntity itemEntity = ItemEntity.builder()
        .name("test-item-" + Instant.now().toEpochMilli())
        .description("item-description-1")
        .sellerId(accountId)
        .price(new Random().nextDouble() * 50D)
        .stock(stock)
        .cargoType(CargoType.values()[new Random().nextInt(2)])
        .cargoCompany(cargoList[new Random().nextInt(5)])
        .createdAt(1564128012966L)
        .build();

    itemEntity = itemRepository.save(itemEntity);

    List<Variant> variants = new ArrayList<>();

    return Converters.itemEntityToItemResponse(itemEntity, variants);
  }

  public CampaignResponse generateCampaign(int productId, int accountId, int cartLimit, int campaignLimit, int requirementCount, int giftCount) {
    CampaignEntity campaignEntity = CampaignEntity.builder()
        .title("custom campaign")
        .productId(productId)
        .cartLimit(cartLimit)
        .campaignLimit(campaignLimit)
        .createdAt(1564128135680L)
        .sellerId(accountId)
        .requirementCount(requirementCount)
        .giftCount(giftCount)
        .startAt(1562939866630L)
        .endAt(1577653200000L)
        .status(CampaignStatus.ACTIVE)
        .build();

    campaignEntity = campaignRepository.save(campaignEntity);

    Badge badge = Badge.builder()
        .requirement(requirementCount)
        .gift(giftCount)
        .build();

    return Converters.campaignEntityToCampaignResponse(campaignEntity, badge);
  }

  public Variant generateVariant(int productId, int stock, List<VariantSpec> variantSpecs) {
    VariantEntity variantEntity = variantRepository.save(VariantEntity.builder()
        .productId(productId)
        .stock(stock)
        .price(new Random().nextDouble() * 15D)
        .build());

    variantSpecs.forEach(variantSpec -> {
      VariantSpecEntity variantSpecEntity = VariantSpecEntity.builder()
          .variantId(variantEntity.getId())
          .specDataId(getIndexOfSpecData(variantSpec.getSpecData()))
          .productId(productId)
          .build();

      variantSpecRepository.save(variantSpecEntity);
    });

    return Variant.builder()
        .id(variantEntity.getId())
        .price(variantEntity.getPrice())
        .stock(variantEntity.getStock())
        .variantSpecs(variantSpecs)
        .build();
  }

  public Integer getIndexOfSpecData(String data){
    String[] specData = {"small", "medium", "large", "sarı", "kırmızı", "beyaz", "2017", "2018", "2019"};
    return Lists.newArrayList(specData).indexOf(data);
  }

  public void prepareVariantSpecs() {
    String[] specDetails = {"Ebat", "Renk", "Üretim Yılı"};
    for (int i = 0; i < 3; i++) {
      SpecDetailEntity specDetailEntity = SpecDetailEntity.builder()
          .detail(specDetails[i])
          .build();
      specDetailRepository.save(specDetailEntity);
    }

    String[] specData = {"small", "medium", "large", "sarı", "kırmızı", "beyaz", "2017", "2018", "2019"};
    for (int i = 0; i < 9; i++) {
      SpecDataEntity specDataEntity = SpecDataEntity.builder()
          .specDetailId(i / 3)
          .data(specData[i])
          .build();
      specDataRepository.save(specDataEntity);
    }
  }

  public VariantSpec generateVariantSpec() {
    String[] specDetails = {"Ebat", "Renk", "Üretim Yılı"};
    String[] specData = {"small", "medium", "large", "sarı", "kırmızı", "beyaz", "2017", "2018", "2019"};

    int detailSelector = new Random().nextInt(3);
    int dataSelector = (new Random().nextInt(3) * 3) + detailSelector;

    return VariantSpec.builder()
        .specDetail(specDetails[detailSelector])
        .specData(specData[dataSelector])
        .build();
  }

  public void updateCart(CartEntity cartEntity) {
    cartRepository.updateCart(cartEntity);
  }

  public CartItem generateCartItem(int accountId, int stock, int desiredSaleCount, boolean variant) {
    ItemResponse itemResponse = generateItem(accountId, stock);

    Variant variantResult = Variant.builder().build();

    if (variant) {
      VariantSpec variantSpec1 = generateVariantSpec();
      VariantSpec variantSpec2 = generateVariantSpec();

      while (variantSpec1.getSpecDetail().equals(variantSpec2.getSpecData())) {
        variantSpec2 = generateVariantSpec();
      }

      List<VariantSpec> variantSpecs = new ArrayList<>();
      variantSpecs.add(variantSpec1);
      variantSpecs.add(variantSpec2);

      variantResult = generateVariant(itemResponse.getProductId(), stock, variantSpecs);
    }

    return CartItem.builder()
        .productId(itemResponse.getProductId())
        .desiredSaleCount(desiredSaleCount)
        .saleCount(desiredSaleCount)
        .sellerId(accountId)
        .price(itemResponse.getPrice())
        .addedAt(Instant.now().toEpochMilli())
        .messageKey(0)
        .hasCampaign(false)
        .campaignParams(null)
        .hasVariant(variant)
        .variant(variantResult)
        .build();
  }

  public void generateSale(int accountId, int productId, int saleCount, int giftCount, double price) {
    SalesEntity salesEntity = SalesEntity.builder()
        .ownerId(accountId)
        .productId(productId)
        .saleCount(saleCount)
        .giftCount(giftCount)
        .price(price * saleCount)
        .soldAt(1564129933353L)
        .variantId(0)
        .build();

    salesRepository.save(salesEntity);
  }

  public RegisterResponse generateUserAccount() {
    return accountService.addUser(RegisterRequest.builder()
        .email("test-seller" + Instant.now().toEpochMilli() + "@mail.com")
        .password("qwerty")
        .name("test")
        .lastName("seller")
        .build());
  }

  public RegisterResponse generateSellerAccount() {
    return accountService.addSellerAccount(RegisterRequest.builder()
        .email("test-seller" + Instant.now().toEpochMilli() + "@mail.com")
        .password("qwerty")
        .name("test")
        .lastName("seller")
        .build());
  }

  public Optional<Integer> getAccountIdFromToken(String token) {
    return Optional.of(Integer.valueOf(jwtTokenProvider
        .getIdFromToken(token)));
  }

  public String getCartIdFromToken(String token) {
    return jwtTokenProvider
        .getCartIdFromToken(token);
  }
}
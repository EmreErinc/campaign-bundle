package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.request.AddVariantRequest;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.repository.*;
import com.finartz.intern.campaignlogic.security.Errors;
import com.finartz.intern.campaignlogic.service.ItemService;
import com.finartz.intern.campaignlogic.service.ItemServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class ItemServiceTest {

  @Spy
  private ItemService itemService;

  @Mock
  private CartRepository cartRepository;
  @Mock
  private SellerRepository sellerRepository;
  @Mock
  private AccountRepository accountRepository;
  @Mock
  private CampaignRepository campaignRepository;
  @Mock
  private ItemRepository itemRepository;
  @Mock
  private SalesRepository salesRepository;
  @Mock
  private VariantRepository variantRepository;
  @Mock
  private VariantSpecRepository variantSpecRepository;
  @Mock
  private SpecDataRepository specDataRepository;
  @Mock
  private SpecDetailRepository specDetailRepository;

  private int variantStock = 10;
  private int specDataId1 = 151;
  private int specDataId2 = 152;
  private int specDataId3 = 153;
  private int variantSpecEntityId1 = 200;
  private int variantSpecEntityId2 = 201;
  private int variantSpecEntityId3 = 202;
  private List<AddVariantRequest> addVariantRequests = new ArrayList<>();

  @Before
  public void initialize() {
    MockitoAnnotations.initMocks(this);
    itemService = new ItemServiceImpl(itemRepository,
        accountRepository,
        sellerRepository,
        campaignRepository,
        salesRepository,
        cartRepository,
        variantRepository,
        variantSpecRepository,
        specDataRepository,
        specDetailRepository);


    int specDetailId1 = 100;
    int specDetailId2 = 101;
    int specDetailId3 = 102;
    SpecDetailEntity specDetailEntity1 = SpecDetailEntity.builder().id(specDetailId1).detail("Ebat").build();
    SpecDetailEntity specDetailEntity2 = SpecDetailEntity.builder().id(specDetailId2).detail("Dış Renk").build();
    SpecDetailEntity specDetailEntity3 = SpecDetailEntity.builder().id(specDetailId3).detail("Üretim Yılı").build();
    SpecDataEntity specDataEntity1 = SpecDataEntity.builder().id(specDataId1).specDetailId(specDetailId1).data("Small").build();
    SpecDataEntity specDataEntity2 = SpecDataEntity.builder().id(specDataId2).specDetailId(specDetailId2).data("Mavi").build();
    SpecDataEntity specDataEntity3 = SpecDataEntity.builder().id(specDataId3).specDetailId(specDetailId3).data("2018").build();
    AddVariantRequest addVariantRequest1 = AddVariantRequest.builder().price(20.5).specDataId(specDataId1).specDetailId(specDetailId1).stock(variantStock).build();
    AddVariantRequest addVariantRequest2 = AddVariantRequest.builder().price(20.5).specDataId(specDataId2).specDetailId(specDetailId2).stock(variantStock).build();
    AddVariantRequest addVariantRequest3 = AddVariantRequest.builder().price(20.5).specDataId(specDataId3).specDetailId(specDetailId3).stock(variantStock).build();

    addVariantRequests = new ArrayList<>();
    addVariantRequests.add(addVariantRequest1);
    addVariantRequests.add(addVariantRequest2);
    addVariantRequests.add(addVariantRequest3);


    when(specDataRepository.findById(specDataId1)).thenReturn(Optional.of(specDataEntity1));
    when(specDataRepository.findById(specDataId2)).thenReturn(Optional.of(specDataEntity2));
    when(specDataRepository.findById(specDataId3)).thenReturn(Optional.of(specDataEntity3));
    when(specDetailRepository.findById(specDetailId1)).thenReturn(Optional.of(specDetailEntity1));
    when(specDetailRepository.findById(specDetailId2)).thenReturn(Optional.of(specDetailEntity2));
    when(specDetailRepository.findById(specDetailId3)).thenReturn(Optional.of(specDetailEntity3));
  }

  @Test
  public void addItem_WithoutVariant_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId = 20;
    int stock = 25;
    double price = 20.5;
    String itemName = "ayakkabı";
    String itemDescription = "açıklama";

    AddItemRequest request = AddItemRequest.builder()
        .name(itemName)
        .description(itemDescription)
        .price(price)
        .stock(stock)
        .cargoCompany("PTT")
        .cargoType(CargoType.FREE)
        .build();

    AccountEntity accountEntity = AccountEntity.builder()
        .id(accountId)
        .name("test")
        .lastName("seller")
        .email("test-seller@mail.com")
        .password("p4s5")
        .role(Role.SELLER)
        .createdAt(1564037169922L)
        .build();

    SellerEntity sellerEntity = SellerEntity.builder()
        .id(sellerId)
        .accountId(accountId)
        .name("test-seller")
        .address("test republic")
        .createdAt(1564037338720L)
        .status(SellerStatus.ACTIVE)
        .build();

    ItemEntity itemEntity = generateItemEntity(sellerId, productId, stock, price, itemName, itemDescription);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(accountEntity));
    when(sellerRepository.findByAccountId(accountId))
        .thenReturn(Optional.of(sellerEntity));
    when(itemRepository.save(any()))
        .thenReturn(itemEntity);

    //test
    ItemResponse itemResponse = itemService.addItem(accountId, request);

    assertNotNull(itemResponse);
    assertEquals(productId, itemResponse.getProductId().intValue());
    assertEquals(itemName, itemResponse.getName());
    assertEquals(itemDescription, itemResponse.getDescription());
    //campaign assertions
    assertNull(itemResponse.getBadge());
    //variant assertions
    assertEquals(Lists.newArrayList(), itemResponse.getVariants());
  }

  @Test
  public void addItem_WithVariant_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId = 20;
    int variantId = 100;
    int stock = 25;
    double price = 20.5;
    String itemName = "ayakkabı";
    String itemDescription = "açıklama";

    AccountEntity accountEntity = AccountEntity.builder()
        .id(accountId)
        .name("test")
        .lastName("seller")
        .email("test-seller@mail.com")
        .password("p4s5")
        .role(Role.SELLER)
        .createdAt(1564037169922L)
        .build();

    SellerEntity sellerEntity = SellerEntity.builder()
        .id(sellerId)
        .accountId(accountId)
        .name("test-seller")
        .address("test republic")
        .createdAt(1564037338720L)
        .status(SellerStatus.ACTIVE)
        .build();

    ItemEntity itemEntity = generateItemEntity(sellerId, productId, stock, price, itemName, itemDescription);

    VariantEntity variantEntity = VariantEntity.builder()
        .id(variantId)
        .stock(variantStock)
        .price(12.3)
        .productId(productId)
        .build();

    AddItemRequest request = AddItemRequest.builder()
        .name(itemName)
        .description(itemDescription)
        .price(price)
        .stock(stock)
        .cargoCompany("PTT")
        .cargoType(CargoType.FREE)
        .variants(addVariantRequests)
        .build();

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntity));
    when(sellerRepository.findByAccountId(accountId)).thenReturn(Optional.of(sellerEntity));
    when(itemRepository.save(any())).thenReturn(itemEntity);
    when(variantRepository.save(any())).thenReturn(variantEntity);
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));


    //test
    ItemResponse itemResponse = itemService.addItem(accountId, request);

    assertNotNull(itemResponse);
    assertEquals(productId, itemResponse.getProductId().intValue());
    assertEquals(itemName, itemResponse.getName());
    assertEquals(itemDescription, itemResponse.getDescription());
    //campaign assertions
    assertNull(itemResponse.getBadge());
    //variant assertions
    assertNotNull(itemResponse.getVariants());
    assertTrue(itemResponse.getVariants().stream().anyMatch(variant -> variant.getId().equals(variantId)));
    assertTrue(itemResponse.getVariants().stream().findFirst().get().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId1)));
    assertTrue(itemResponse.getVariants().stream().findFirst().get().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId2)));
    assertTrue(itemResponse.getVariants().stream().findFirst().get().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId3)));
  }

  @Test
  public void addItem_UnauthorizedAccount_ThrowException() {
    int accountId = 1;
    int stock = 25;
    double price = 20.5;
    String itemName = "ayakkabı";
    String itemDescription = "açıklama";

    AddItemRequest request = AddItemRequest.builder()
        .name(itemName)
        .description(itemDescription)
        .price(price)
        .stock(stock)
        .cargoCompany("PTT")
        .cargoType(CargoType.FREE)
        .build();

    AccountEntity accountEntity = AccountEntity.builder()
        .id(accountId)
        .name("unauthorized")
        .lastName("seller")
        .email("unauthorized-seller@mail.com")
        .password("p4s5")
        .role(Role.USER)
        .createdAt(1564037169922L)
        .build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntity));

    //test
    try {
      itemService.addItem(accountId, request);
    } catch (ApplicationContextException ex) {
      assertEquals(Errors.NOT_PERMISSION, ex.getMessage());
    }
  }

  @Test
  public void getItem_WithoutVariantAndWithoutCampaign_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId = 20;
    int stock = 25;
    double price = 20.5;
    String itemName = "ayakkabı";
    String itemDescription = "açıklama";

    ItemEntity itemEntity = generateItemEntity(sellerId, productId, stock, price, itemName, itemDescription);

    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    ItemDetail itemDetail = itemService.getItem(Optional.of(accountId), String.valueOf(productId));

    assertNotNull(itemDetail);
    //campaign assertions
    assertEquals(Badge.builder().build(), itemDetail.getBadge());
    //variant assertions
    assertEquals(Lists.newArrayList(), itemDetail.getVariants());
  }

  @Test
  public void getItem_WithVariantAndWithoutCampaign_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId = 20;
    int variantId1 = 30;
    int variantId2 = 31;
    int stock = 25;
    double price = 20.5;
    String itemName = "ayakkabı";
    String itemDescription = "açıklama";

    ItemEntity itemEntity = generateItemEntity(sellerId, productId, stock, price, itemName, itemDescription);

    VariantEntity variantEntity1 = generateVariantEntity(productId, variantId1, variantStock);
    VariantEntity variantEntity2 = generateVariantEntity(productId, variantId2, variantStock);

    List<VariantEntity> variantEntities = new ArrayList<>();
    variantEntities.add(variantEntity1);
    variantEntities.add(variantEntity2);

    List<VariantSpecEntity> variantSpecEntities1 = generateVariantSpecEntities(variantId1, productId);
    List<VariantSpecEntity> variantSpecEntities2 = generateVariantSpecEntities(variantId1, productId);

    when(itemRepository.findById(productId)).thenReturn(Optional.of(itemEntity));
    when(variantRepository.findByProductId(productId)).thenReturn(Optional.of(variantEntities));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId1)).thenReturn(Optional.of(variantSpecEntities1));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId2)).thenReturn(Optional.of(variantSpecEntities2));

    //test
    ItemDetail itemDetail = itemService.getItem(Optional.of(accountId), String.valueOf(productId));

    assertNotNull(itemDetail);
    //campaign assertions
    assertEquals(Badge.builder().build(), itemDetail.getBadge());
    //variant assertions
    assertNotNull(itemDetail.getVariants());
    assertTrue(itemDetail.getVariants().stream().anyMatch(variant -> variant.getId().equals(variantId1)));
    assertTrue(itemDetail.getVariants().stream().anyMatch(variant -> variant.getId().equals(variantId2)));
  }

  @Test
  public void getItem_WithoutVariantAndWithCampaign_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId = 20;
    int stock = 25;
    int campaignId = 30;
    double price = 20.5;
    String itemName = "ayakkabı";
    String itemDescription = "açıklama";

    ItemEntity itemEntity = generateItemEntity(sellerId, productId, stock, price, itemName, itemDescription);

    int requirementCount = 3;
    int giftCount = 1;
    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId, requirementCount, giftCount);

    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));
    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));

    //test
    ItemDetail itemDetail = itemService.getItem(Optional.of(accountId), String.valueOf(productId));

    assertNotNull(itemDetail);
    //campaign assertions
    assertEquals(Badge.builder().requirement(requirementCount).gift(giftCount).build(), itemDetail.getBadge());
    //variant assertions
    assertEquals(Lists.newArrayList(), itemDetail.getVariants());
  }

  @Test
  public void getItem_WithVariantAndWithCampaign_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId = 20;
    int stock = 25;
    int campaignId = 30;
    double price = 20.5;
    int variantId1 = 30;
    int variantId2 = 31;
    String itemName = "ayakkabı";
    String itemDescription = "açıklama";

    ItemEntity itemEntity = generateItemEntity(sellerId, productId, stock, price, itemName, itemDescription);

    int requirementCount = 3;
    int giftCount = 1;
    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId, 3, 1);

    VariantEntity variantEntity1 = generateVariantEntity(productId, variantId1, variantStock);
    VariantEntity variantEntity2 = generateVariantEntity(productId, variantId2, variantStock);

    List<VariantEntity> variantEntities = new ArrayList<>();
    variantEntities.add(variantEntity1);
    variantEntities.add(variantEntity2);

    List<VariantSpecEntity> variantSpecEntities1 = generateVariantSpecEntities(variantId1, productId);
    List<VariantSpecEntity> variantSpecEntities2 = generateVariantSpecEntities(variantId1, productId);

    when(variantRepository.findByProductId(productId)).thenReturn(Optional.of(variantEntities));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId1)).thenReturn(Optional.of(variantSpecEntities1));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId2)).thenReturn(Optional.of(variantSpecEntities2));

    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));
    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));

    //test
    ItemDetail itemDetail = itemService.getItem(Optional.of(accountId), String.valueOf(productId));

    assertNotNull(itemDetail);
    //campaign assertions
    assertEquals(Badge.builder().requirement(requirementCount).gift(giftCount).build(), itemDetail.getBadge());
    //variant assertions
    assertNotNull(itemDetail.getVariants());
    assertTrue(itemDetail.getVariants().stream().anyMatch(variant -> variant.getId().equals(variantId1)));
    assertTrue(itemDetail.getVariants().stream().anyMatch(variant -> variant.getId().equals(variantId2)));
  }

  @Test
  public void getItem_NotExists_ThrowException() {
    int accountId = 1;
    int productId = 20;

    when(itemRepository.findById(any())).thenReturn(Optional.empty());

    //test
    try {
      itemService.getItem(Optional.of(accountId), String.valueOf(productId));
    } catch (ApplicationContextException ex) {
      assertNotNull(Errors.ITEM_NOT_FOUND, ex.getMessage());
    }
  }

  @Test
  public void getItemList_UnusedCampaigns_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId1 = 20;
    int productId2 = 20;
    int productId3 = 20;
    int campaignId = 30;
    int requirementCount = 3;
    int giftCount = 1;

    ItemEntity itemEntity1 = generateItemEntity(sellerId, productId1, 25, 10.0, "ayakkabı", "ayakkabı açıklama");
    ItemEntity itemEntity2 = generateItemEntity(sellerId, productId2, 30, 25.5, "gömlek", "ayakkabı açıklama");
    ItemEntity itemEntity3 = generateItemEntity(sellerId, productId3, 20, 9.9, "saat", "ayakkabı açıklama");

    List<ItemEntity> itemEntities = new ArrayList<>();
    itemEntities.add(itemEntity1);
    itemEntities.add(itemEntity2);
    itemEntities.add(itemEntity3);

    Iterable<ItemEntity> itemEntityIterable = itemEntities;

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, 21, sellerId, requirementCount, giftCount);

    when(itemRepository.findAll()).thenReturn(itemEntityIterable);
    when(itemRepository.findById(20)).thenReturn(Optional.of(itemEntity1));
    when(itemRepository.findById(21)).thenReturn(Optional.of(itemEntity2));
    when(itemRepository.findById(22)).thenReturn(Optional.of(itemEntity3));
    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId1))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));

    //test
    List<ItemSummary> itemResponse = itemService.getItemList(Optional.of(accountId), Optional.empty());

    assertNotNull(itemResponse);
    assertEquals(3, itemResponse.size());
    assertTrue(itemResponse.stream().anyMatch(itemSummary -> itemSummary.getId().equals(productId1)));
    assertTrue(itemResponse.stream().anyMatch(itemSummary -> itemSummary.getId().equals(productId2)));
    assertTrue(itemResponse.stream().anyMatch(itemSummary -> itemSummary.getId().equals(productId3)));
    //campaign assertions
    assertEquals(Badge.builder().requirement(requirementCount).gift(giftCount).build(), itemResponse.stream().filter(itemSummary -> itemSummary.getId().equals(productId2)).findFirst().get().getBadge());
  }

  @Test
  public void getItemList_UsedAndUnsuitableCampaign_ShouldPass() {
    int accountId = 1;
    int sellerId = 10;
    int productId1 = 20;
    int productId2 = 21;
    int productId3 = 22;
    int campaignId = 30;
    int requirementCount = 3;
    int giftCount = 1;

    ItemEntity itemEntity1 = generateItemEntity(sellerId, productId1, 25, 10.0, "ayakkabı", "ayakkabı açıklama");
    ItemEntity itemEntity2 = generateItemEntity(sellerId, productId2, 30, 25.5, "gömlek", "ayakkabı açıklama");
    ItemEntity itemEntity3 = generateItemEntity(sellerId, productId3, 20, 9.9, "saat", "ayakkabı açıklama");

    List<ItemEntity> itemEntities = new ArrayList<>();
    itemEntities.add(itemEntity1);
    itemEntities.add(itemEntity2);
    itemEntities.add(itemEntity3);

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId2, sellerId, requirementCount, giftCount);

    SalesEntity salesEntity1 = generateSaleEntity(accountId, productId1, 0, 5, 0, 1564063628541L, 1);
    SalesEntity salesEntity2 = generateSaleEntity(accountId, productId1, 0, 6, 2, 1564063628541L, 2);
    SalesEntity salesEntity3 = generateSaleEntity(accountId, productId1, 0, 4, 1, 1564063628541L, 3);

    List<SalesEntity> salesEntities = new ArrayList<>();
    salesEntities.add(salesEntity1);
    salesEntities.add(salesEntity2);
    salesEntities.add(salesEntity3);

    when(itemRepository.findAll()).thenReturn(itemEntities);
    when(itemRepository.findById(20)).thenReturn(Optional.of(itemEntity1));
    when(itemRepository.findById(21)).thenReturn(Optional.of(itemEntity2));
    when(itemRepository.findById(22)).thenReturn(Optional.of(itemEntity3));
    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId2))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.existsByProductId(productId2))
        .thenReturn(true);
    when(salesRepository.findByOwnerId(accountId))
        .thenReturn(Optional.of(salesEntities));

    //test
    List<ItemSummary> itemResponse = itemService.getItemList(Optional.of(accountId), Optional.empty());

    assertNotNull(itemResponse);
    assertEquals(3, itemResponse.size());
    assertTrue(itemResponse.stream().anyMatch(itemSummary -> itemSummary.getId().equals(productId1)));
    assertTrue(itemResponse.stream().anyMatch(itemSummary -> itemSummary.getId().equals(productId2)));
    assertTrue(itemResponse.stream().anyMatch(itemSummary -> itemSummary.getId().equals(productId3)));
    //campaign assertions
    assertEquals(Badge.builder().requirement(requirementCount).gift(giftCount).build(), itemResponse.stream().filter(itemSummary -> itemSummary.getId().equals(productId2)).findFirst().get().getBadge());
  }

  private ItemEntity generateItemEntity(int sellerId, int productId, int stock, double price, String itemName, String itemDescription) {
    return ItemEntity.builder()
        .id(productId)
        .price(price)
        .name(itemName)
        .description(itemDescription)
        .sellerId(sellerId)
        .stock(stock)
        .createdAt(1564037654207L)
        .cargoType(CargoType.FREE)
        .cargoCompany("PTT")
        .build();
  }

  private VariantEntity generateVariantEntity(int productId, int variantId, int variantStock) {
    return VariantEntity.builder()
        .id(variantId)
        .stock(variantStock)
        .price(12.3)
        .productId(productId)
        .build();
  }

  private List<VariantSpecEntity> generateVariantSpecEntities(int variantId, int productId) {
    VariantSpecEntity variantSpecEntity1 = VariantSpecEntity.builder().id(variantSpecEntityId1).productId(productId).specDataId(specDataId1).variantId(variantId).build();
    VariantSpecEntity variantSpecEntity2 = VariantSpecEntity.builder().id(variantSpecEntityId2).productId(productId).specDataId(specDataId2).variantId(variantId).build();
    VariantSpecEntity variantSpecEntity3 = VariantSpecEntity.builder().id(variantSpecEntityId3).productId(productId).specDataId(specDataId3).variantId(variantId).build();

    List<VariantSpecEntity> variantSpecEntities = new ArrayList<>();
    variantSpecEntities.add(variantSpecEntity1);
    variantSpecEntities.add(variantSpecEntity2);
    variantSpecEntities.add(variantSpecEntity3);

    return variantSpecEntities;
  }

  private CampaignEntity generateCampaignEntity(int campaignId, int productId, int sellerId, int requirementCount, int giftCount) {
    return CampaignEntity.builder()
        .id(campaignId)
        .productId(productId)
        .requirementCount(requirementCount)
        .giftCount(giftCount)
        .cartLimit(2)
        .campaignLimit(2)
        .status(CampaignStatus.ACTIVE)
        .sellerId(sellerId)
        .startAt(1562939866630L)
        .endAt(1577653200000L)
        .createdAt(1563862109909L)
        .title("3 Alana 1 Bedava")
        .build();
  }

  private SalesEntity generateSaleEntity(int accountId, int productId, int variantId, int saleCount, int giftCount, Long soldAt, int saleEntityId1) {
    return SalesEntity.builder()
        .id(saleEntityId1)
        .productId(productId)
        .ownerId(accountId)
        .variantId(variantId)
        .saleCount(saleCount)
        .price(saleCount * 12.3)
        .giftCount(giftCount)
        .soldAt(soldAt)
        .build();
  }
}
package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.*;
import com.finartz.intern.campaignlogic.model.request.AddItemToCartRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemDecrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemIncrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemRemoveRequest;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.repository.*;
import com.finartz.intern.campaignlogic.security.Errors;
import com.finartz.intern.campaignlogic.service.CartService;
import com.finartz.intern.campaignlogic.service.CartServiceImpl;
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
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class CartServiceTest {

  @Spy
  private CartService cartService;

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

  private int specDataId1 = 151;
  private int specDataId2 = 152;
  private int specDataId3 = 153;
  private int variantSpecEntityId1 = 200;
  private int variantSpecEntityId2 = 201;
  private int variantSpecEntityId3 = 202;

  @Before
  public void initialize() {
    MockitoAnnotations.initMocks(this);
    cartService = new CartServiceImpl(cartRepository,
        sellerRepository,
        accountRepository,
        campaignRepository,
        itemRepository,
        salesRepository,
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

    when(specDataRepository.findById(specDataId1)).thenReturn(Optional.of(specDataEntity1));
    when(specDataRepository.findById(specDataId2)).thenReturn(Optional.of(specDataEntity2));
    when(specDataRepository.findById(specDataId3)).thenReturn(Optional.of(specDataEntity3));
    when(specDetailRepository.findById(specDetailId1)).thenReturn(Optional.of(specDetailEntity1));
    when(specDetailRepository.findById(specDetailId2)).thenReturn(Optional.of(specDetailEntity2));
    when(specDetailRepository.findById(specDetailId3)).thenReturn(Optional.of(specDetailEntity3));
  }

  @Test
  public void addToCart_CampaignItemWithVariant_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;
    int sellerId = 50;
    int campaignId = 10;
    int variantStock = 15;
    int totalStock = 25;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId, 3, 1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));

    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getSaleCount().equals(count)));
    //campaign assertions
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasCampaign());
    assertEquals(5, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(1, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
    //variant assertions
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasVariant());
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId1)));
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId2)));
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId3)));
  }

  @Test
  public void addToCart_OrdinaryItemWithVariant_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;
    int sellerId = 50;
    int variantStock = 15;
    int totalStock = 25;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(any()))
        .thenReturn(Optional.empty());
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.empty());
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.empty());
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId))
        .thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getSaleCount().equals(count)));
    //campaign assertions
    assertFalse(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasCampaign());
    //variant assertions
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasVariant());
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId1)));
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId2)));
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId3)));
  }

  @Test
  public void addToCart_CampaignItemWithoutVariant_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 25;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId))
        .thenReturn(Optional.empty());
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getSaleCount().equals(count)));
    //campaign assertions
    assertTrue(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasCampaign());
    assertEquals(5, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(1, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
    //variant assertions
    assertFalse(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasVariant());
  }

  @Test
  public void addToCart_OrdinaryItemWithoutVariant_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 25;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.empty());
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.empty());
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.empty());
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId))
        .thenReturn(Optional.empty());
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getSaleCount().equals(count)));
    //campaign assertions
    assertFalse(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasCampaign());
    //variant assertions
    assertFalse(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(productId)).findFirst().get().getHasVariant());
  }

  @Test
  public void addToCart_NotExistsItem_ThrowException() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

    when(itemRepository.findById(productId))
        .thenReturn(Optional.empty());

    //test
    try {
      cartService.addToCart(accountId, cartId, request);
    } catch (ApplicationContextException ex) {
      assertEquals(Errors.ITEM_NOT_FOUND, ex.getMessage());
    }
  }

  @Test
  public void addToCart_AlreadyExistsCampaignItemOnCart_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int count = 8;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 25;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartItem cartItem = CartItem.builder()
        .productId(productId)
        .sellerId(sellerId)
        .hasVariant(false)
        .variant(null)
        .hasCampaign(true)
        .campaignParams(CampaignParams.builder().totalItemCount(5).actualGiftCount(1).badge(Badge.builder().requirement(3).gift(1).build()).build())
        .desiredSaleCount(5)
        .saleCount(5)
        .price(12.3)
        .addedAt(1563948163800L)
        .messageKey(0)
        .build();

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(cartItems)
        .build();

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(anyInt()))
        .thenReturn(Optional.empty());
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    assertNotNull(cartResponse);
    //campaign assertions
    assertEquals(13, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
    //assertEquals(Messages.CART_LIMIT_EXCEED.getValue(), cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getMessage());
  }

  @Test
  public void incrementItem_AlreadyExistsCampaignItemOnCart_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 25;

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(productId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartItem cartItem = CartItem.builder()
        .productId(productId)
        .sellerId(sellerId)
        .hasVariant(false)
        .variant(null)
        .hasCampaign(true)
        .campaignParams(CampaignParams.builder().totalItemCount(5).actualGiftCount(1).badge(Badge.builder().requirement(3).gift(1).build()).build())
        .desiredSaleCount(5)
        .saleCount(5)
        .price(12.3)
        .addedAt(1563948163800L)
        .messageKey(0)
        .build();

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(cartItems)
        .build();

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(anyInt()))
        .thenReturn(Optional.empty());
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.incrementItem(accountId, cartId, request);

    assertNotNull(cartResponse);
    //campaign assertions
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
    //assertEquals(Messages.CART_LIMIT_EXCEED.getValue(), cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getMessage());
  }

  @Test
  public void decrementItem_AlreadyExistsCampaignItemOnCart_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 25;

    CartItemDecrementRequest request = CartItemDecrementRequest.builder()
        .productId(productId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartItem cartItem = CartItem.builder()
        .productId(productId)
        .sellerId(sellerId)
        .hasVariant(false)
        .variant(null)
        .hasCampaign(true)
        .campaignParams(CampaignParams.builder().totalItemCount(5).actualGiftCount(1).badge(Badge.builder().requirement(3).gift(1).build()).build())
        .desiredSaleCount(3)
        .saleCount(3)
        .price(12.3)
        .addedAt(1563948163800L)
        .messageKey(0)
        .build();

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(cartItems)
        .build();

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(anyInt()))
        .thenReturn(Optional.empty());
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.decrementItem(accountId, cartId, request);

    assertNotNull(cartResponse);
    //campaign assertions
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(0, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
    //assertEquals(Messages.CART_LIMIT_EXCEED.getValue(), cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getMessage());
  }

  @Test
  public void removeFromCart_AlreadyExistsCampaignItemOnCart_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 25;

    CartItemRemoveRequest request = CartItemRemoveRequest.builder()
        .productId(productId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartItem cartItem = CartItem.builder()
        .productId(productId)
        .sellerId(sellerId)
        .hasVariant(false)
        .variant(null)
        .hasCampaign(true)
        .campaignParams(CampaignParams.builder().totalItemCount(5).actualGiftCount(1).badge(Badge.builder().requirement(3).gift(1).build()).build())
        .desiredSaleCount(3)
        .saleCount(3)
        .price(12.3)
        .addedAt(1563948163800L)
        .messageKey(0)
        .build();

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(cartItems)
        .build();

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(anyInt(), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(anyInt()))
        .thenReturn(Optional.empty());
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.removeFromCart(accountId, cartId, request);

    assertNotNull(cartResponse);
    assertEquals(Lists.newArrayList(), cartResponse.getItemList());
  }

  @Test
  public void addToCart_AlreadyExistsCampaignItemWithVariantOnCart_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId1 = 40;
    int variantId2 = 41;
    int count = 5;
    int sellerId = 50;
    int campaignId = 10;
    int variantStock = 15;
    int totalStock = 25;

    //want to cart
    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId2)
        .build();

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    //exists on cart
    VariantEntity variantEntity1 = generateVariantEntity(productId, variantId1, variantStock);

    VariantEntity variantEntity2 = generateVariantEntity(productId, variantId2, variantStock);

    int specDetailId1 = 100;
    SpecDetailEntity specDetailEntity1 = SpecDetailEntity.builder().id(specDetailId1).detail("Ebat").build();

    int specDetailId2 = 101;
    SpecDetailEntity specDetailEntity2 = SpecDetailEntity.builder().id(specDetailId2).detail("Dış Renk").build();

    int specDetailId3 = 102;
    SpecDetailEntity specDetailEntity3 = SpecDetailEntity.builder().id(specDetailId3).detail("Üretim Yılı").build();

    int specDataId1 = 151;
    SpecDataEntity specDataEntity1 = SpecDataEntity.builder().id(specDataId1).specDetailId(specDetailId1).data("Small").build();

    int specDataId2 = 152;
    SpecDataEntity specDataEntity2 = SpecDataEntity.builder().id(specDataId2).specDetailId(specDetailId1).data("Medium").build();

    int specDataId3 = 153;
    SpecDataEntity specDataEntity3 = SpecDataEntity.builder().id(specDataId3).specDetailId(specDetailId2).data("Mavi").build();

    int specDataId4 = 153;
    SpecDataEntity specDataEntity4 = SpecDataEntity.builder().id(specDataId4).specDetailId(specDetailId2).data("Kırmızı").build();

    int specDataId5 = 154;
    SpecDataEntity specDataEntity5 = SpecDataEntity.builder().id(specDataId5).specDetailId(specDetailId3).data("2018").build();

    int variantSpecEntityId1 = 200;
    VariantSpecEntity variantSpecEntity1 = VariantSpecEntity.builder().id(variantSpecEntityId1).productId(productId).specDataId(specDataId1).variantId(variantId1).build();
    VariantSpec variantSpec1 = VariantSpec.builder().id(variantSpecEntityId1).specDetail(specDetailEntity1.getDetail()).specData(specDetailEntity1.getDetail()).build();

    int variantSpecEntityId2 = 201;
    VariantSpecEntity variantSpecEntity2 = VariantSpecEntity.builder().id(variantSpecEntityId2).productId(productId).specDataId(specDataId2).variantId(variantId1).build();
    VariantSpec variantSpec2 = VariantSpec.builder().id(variantSpecEntityId2).specDetail(specDetailEntity2.getDetail()).specData(specDetailEntity2.getDetail()).build();

    int variantSpecEntityId3 = 202;
    VariantSpecEntity variantSpecEntity3 = VariantSpecEntity.builder().id(variantSpecEntityId3).productId(productId).specDataId(specDataId3).variantId(variantId1).build();
    VariantSpec variantSpec3 = VariantSpec.builder().id(variantSpecEntityId3).specDetail(specDetailEntity3.getDetail()).specData(specDetailEntity3.getDetail()).build();

    List<VariantSpec> existsVariantSpecs = new ArrayList<>();
    existsVariantSpecs.add(variantSpec1);
    existsVariantSpecs.add(variantSpec2);
    existsVariantSpecs.add(variantSpec3);

    List<VariantSpecEntity> existsVariantSpecEntities = new ArrayList<>();
    existsVariantSpecEntities.add(variantSpecEntity1);
    existsVariantSpecEntities.add(variantSpecEntity2);
    existsVariantSpecEntities.add(variantSpecEntity3);

    int variantSpecEntityId4 = 203;
    VariantSpecEntity variantSpecEntity4 = VariantSpecEntity.builder().id(variantSpecEntityId4).productId(productId).specDataId(specDataId4).variantId(variantId2).build();

    int variantSpecEntityId5 = 204;
    VariantSpecEntity variantSpecEntity5 = VariantSpecEntity.builder().id(variantSpecEntityId5).productId(productId).specDataId(specDataId5).variantId(variantId2).build();

    List<VariantSpecEntity> variantSpecEntities2 = new ArrayList<>();
    variantSpecEntities2.add(variantSpecEntity4);
    variantSpecEntities2.add(variantSpecEntity5);

    CartItem cartItem = CartItem.builder()
        .productId(productId)
        .sellerId(sellerId)
        .hasVariant(true)
        .variant(Converters.variantEntityToVariant(variantEntity1, existsVariantSpecs))
        .hasCampaign(true)
        .campaignParams(CampaignParams.builder().totalItemCount(5).actualGiftCount(1).badge(Badge.builder().requirement(3).gift(1).build()).build())
        .desiredSaleCount(5)
        .saleCount(5)
        .price(12.3)
        .addedAt(1563948163800L)
        .messageKey(0)
        .build();

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(cartItems)
        .build();

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId1)).thenReturn(Optional.of(variantEntity1));
    when(variantRepository.findById(variantId2)).thenReturn(Optional.of(variantEntity2));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId1)).thenReturn(Optional.of(existsVariantSpecEntities));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId2)).thenReturn(Optional.of(variantSpecEntities2));
    when(specDataRepository.findById(specDataId1)).thenReturn(Optional.of(specDataEntity1));
    when(specDataRepository.findById(specDataId2)).thenReturn(Optional.of(specDataEntity2));
    when(specDataRepository.findById(specDataId3)).thenReturn(Optional.of(specDataEntity3));
    when(specDataRepository.findById(specDataId4)).thenReturn(Optional.of(specDataEntity4));
    when(specDataRepository.findById(specDataId5)).thenReturn(Optional.of(specDataEntity5));
    when(specDetailRepository.findById(specDetailId1)).thenReturn(Optional.of(specDetailEntity1));
    when(specDetailRepository.findById(specDetailId2)).thenReturn(Optional.of(specDetailEntity2));
    when(specDetailRepository.findById(specDetailId3)).thenReturn(Optional.of(specDetailEntity3));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItemDto -> cartItemDto.getProductId().equals(productId)));
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItemDto -> cartItemDto.getSaleCount().equals(count)));
    //campaign assertions
    assertTrue(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getHasCampaign());
    assertEquals(5, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(1, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
    //variant assertions
    assertTrue(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getHasVariant());
    assertTrue(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).collect(Collectors.toList()).stream().anyMatch(cartItemDto -> cartItemDto.getVariant().getId().equals(variantId1)));
    assertTrue(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).collect(Collectors.toList()).stream().anyMatch(cartItemDto -> cartItemDto.getVariant().getId().equals(variantId2)));
    assertTrue(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).collect(Collectors.toList()).stream().anyMatch(cartItemDto -> cartItemDto.getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId4))));
    assertTrue(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).collect(Collectors.toList()).stream().anyMatch(cartItemDto -> cartItemDto.getVariant().getVariantSpecs().stream().anyMatch(variantSpec -> variantSpec.getId().equals(variantSpecEntityId5))));
  }

  /**
   * desired sale count = 8
   * variant stock      = 10
   * campaign requirement = 3
   * campaign gift        = 1
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_1_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 8;
    int variantStock = 10;
    int totalStock = 10;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 9
   * variant stock      = 10
   * campaign requirement = 3
   * campaign gift        = 1
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_2_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 9;
    int variantStock = 10;
    int totalStock = 10;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 10
   * variant stock      = 10
   * campaign requirement = 3
   * campaign gift        = 1
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_3_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 10;
    int variantStock = 10;
    int totalStock = 10;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 8
   * variant stock      = 9
   * campaign requirement = 3
   * campaign gift        = 1
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_4_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 8;
    int variantStock = 9;
    int totalStock = 9;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(7, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(7, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 9
   * variant stock      = 9
   * campaign requirement = 3
   * campaign gift        = 1
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_5_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 9;
    int variantStock = 9;
    int totalStock = 9;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(7, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(7, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 10
   * variant stock      = 9
   * campaign requirement = 3
   * campaign gift        = 1
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_6_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 10;
    int variantStock = 9;
    int totalStock = 9;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(7, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(7, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 8
   * variant stock      = 10
   * campaign requirement = 3
   * campaign gift        = 2
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_7_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 8;
    int variantStock = 10;
    int totalStock = 10;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,2);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(4, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 9
   * variant stock      = 10
   * campaign requirement = 3
   * campaign gift        = 2
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_8_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 9;
    int variantStock = 10;
    int totalStock = 10;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,2);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(4, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 10
   * variant stock      = 10
   * campaign requirement = 3
   * campaign gift        = 2
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_9_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 10;
    int variantStock = 10;
    int totalStock = 10;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,2);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(4, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 8
   * variant stock      = 9
   * campaign requirement = 3
   * campaign gift        = 2
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_10_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 8;
    int variantStock = 9;
    int totalStock = 9;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,2);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(3, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 9
   * variant stock      = 9
   * campaign requirement = 3
   * campaign gift        = 2
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_11_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 9;
    int variantStock = 9;
    int totalStock = 9;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,2);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(3, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  /**
   * desired sale count = 10
   * variant stock      = 9
   * campaign requirement = 3
   * campaign gift        = 2
   */
  @Test //desired sale count = variant stock, then give available gift
  public void addToCart_UnsuitableCampaignItemOnStock_AvailableGift_12_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int campaignId = 10;
    int sellerId = 50;
    int desiredSaleCount = 10;
    int variantStock = 9;
    int totalStock = 9;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(desiredSaleCount)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,2);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(desiredSaleCount, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(6, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(3, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  @Test //desired sale count = variant stock, then give no gift
  public void addToCart_UnsuitableCampaignItemOnStock_NoGift_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 3;
    int sellerId = 50;
    int campaignId = 10;
    int variantStock = 3;
    int totalStock = 10;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(count, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(count, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertEquals(count, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
    assertEquals(0, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
  }

  @Test //stock available but user campaign limit exceed
  public void addToCart_UnsuitableCampaignItemOnStock_ExceedCampaignLimit_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;
    int sellerId = 50;
    int campaignId = 10;
    int variantStock = 15;
    int totalStock = 20;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

    CampaignEntity campaignEntity = generateCampaignEntity(campaignId, productId, sellerId,3,1);

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

    VariantEntity variantEntity = generateVariantEntity(productId, variantId, variantStock);

    List<VariantSpecEntity> variantSpecEntities = generateVariantSpecEntities(variantId, productId);

    ItemEntity itemEntity = generateItemEntity(productId, sellerId, totalStock);

    int saleEntityId1 = 60;
    SalesEntity salesEntity1 = generateSaleEntity(accountId, productId, variantId, 6, 1, 1563967454239L, saleEntityId1);

    int saleEntityId2 = 61;
    SalesEntity salesEntity2 = generateSaleEntity(accountId, productId, variantId, 8, 2, 1563967539863L, saleEntityId2);

    List<SalesEntity> salesEntities = new ArrayList<>();
    salesEntities.add(salesEntity1);
    salesEntities.add(salesEntity2);

    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.existsByProductId(productId))
        .thenReturn(true);
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variantEntity));
    when(variantSpecRepository.findByProductIdAndVariantId(productId, variantId)).thenReturn(Optional.of(variantSpecEntities));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));

    when(salesRepository.findByOwnerIdAndProductId(accountId, productId)).thenReturn(Optional.of(salesEntities));

    //test
    CartResponse<CartItemDto> cartResponse = cartService.addToCart(accountId, cartId, request);

    //assertions
    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getProductId().equals(productId)));
    assertEquals(count, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getDesiredSaleCount().intValue());
    assertEquals(count, cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getSaleCount().intValue());
    //campaign assertions
    assertFalse(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getHasCampaign());
    assertNull(cartResponse.getItemList().stream().filter(cartItemDto -> cartItemDto.getProductId().equals(productId)).findFirst().get().getCampaignParams());
  }

  private ItemEntity generateItemEntity(int productId, int sellerId, int totalStock) {
    return ItemEntity.builder()
        .id(productId)
        .sellerId(sellerId)
        .stock(totalStock)
        .cargoType(CargoType.FREE)
        .cargoCompany("MNG Kargo")
        .createdAt(1563892923731L)
        .name("Gömlek")
        .description("açıklama")
        .price(12.3)
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
}
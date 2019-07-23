package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.repository.CampaignRepository;
import com.finartz.intern.campaignlogic.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class CartControllerTest extends BaseTestController {

  //@Autowired
  //private MockMvc mockMvc;

  @MockBean
  private CartService cartService;

  @Autowired
  private SalesService salesService;

  @Autowired
  private SellerService sellerService;

  @Mock
  private CartServiceImpl cartServiceImpl;

  @Mock
  private CampaignRepository campaignRepository;

  @Mock
  private BaseService baseService;

  private RegisterResponse sellerAccountRegisterResponse;
  private String cartId;

  private ItemResponse itemResponse1;
  private ItemResponse itemResponse2;
  private ItemResponse itemResponse3;

  private CampaignEntity campaignEntity;

  @Before
  public void initialize() {
    MockitoAnnotations.initMocks(this);

    campaignEntity = CampaignEntity.builder()
        .id(1)
        .productId(5)
        .requirementCount(3)
        .giftCount(1)
        .cartLimit(2)
        .campaignLimit(2)
        .status(CampaignStatus.ACTIVE)
        .sellerId(1)
        .startAt(1562939866630L)
        .endAt(1577653200000L)
        .createdAt(1563862109909L)
        .title("3 Alana 1 Bedava")
        .build();

    //create seller account
//    sellerAccountRegisterResponse = generateSellerAccount();
//    log.info("SELLER ACCOUNT CREATED : " + sellerAccountRegisterResponse.toString());
//
//    //create seller
//    SellerResponse sellerResponse = sellerService.addSeller(sellerAccountRegisterResponse.getId(), AddSellerRequest.builder()
//        .name("Test Seller")
//        .address("Test Mahallesi, Seller Sokak, No: 1, Daire: 1")
//        .build());
//    log.info("SELLER CREATED : " + sellerResponse.toString());
//
//    //add item
//    itemResponse1 = generateItem(sellerAccountRegisterResponse.getId());
//    log.info("ITEM CREATED : " + itemResponse1.toString());
//    itemResponse2 = generateItem(sellerAccountRegisterResponse.getId());
//    log.info("ITEM CREATED : " + itemResponse2.toString());
//    itemResponse3 = generateItem(sellerAccountRegisterResponse.getId());
//    log.info("ITEM CREATED : " + itemResponse2.toString());
//
//    //generate campaign
//    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse1.getProductId());
//    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse3.getProductId());
  }

  /*@Test
  public void addCampaignItemToCart_Directly_ShouldPass(){
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

    CartDto cartDto = CartDto.builder()
        .accountId(accountId)
        .cartId(cartId)
        .variantId(Optional.of(variantId))
        .desiredCount(count)
        .productId(productId)
        .build();

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .build();

    VariantSpec variantSpec1 = VariantSpec.builder()
        .id(100)
        .specDetail("Dış Renk")
        .specData("Mavi")
        .build();

    VariantSpec variantSpec2 = VariantSpec.builder()
        .id(101)
        .specDetail("Ebat")
        .specData("Small")
        .build();

    VariantSpec variantSpec3 = VariantSpec.builder()
        .id(102)
        .specDetail("Üretim Yılı")
        .specData("2018")
        .build();

    List<VariantSpec> variantSpecs = new ArrayList<>();
    variantSpecs.add(variantSpec1);
    variantSpecs.add(variantSpec2);
    variantSpecs.add(variantSpec3);

    Variant variant = Variant.builder()
        .id(variantId)
        .stock(15)
        .price(12.3)
        .variantSpecs(variantSpecs)
        .build();

    when(baseService.getCampaignByProductId(productId)).thenReturn(Optional.of(campaignEntity));
    when(baseService.isItemAvailable(cartDto)).thenReturn(true);
    when(baseService.isCampaignAvailableGetById(campaignEntity.getId())).thenReturn(true);
    when(cartServiceImpl.atLeastOneAvailability(campaignEntity, count, cartId, false)).thenReturn(true);
    when(baseService.getCartEntityById(cartId)).thenReturn(cartEntity);
    when(baseService.getProductPrice(productId)).thenReturn(12.3);
    when(baseService.getProductStock(productId)).thenReturn(20);
    when(baseService.getProductVariant(productId, variantId)).thenReturn(Optional.of(variant));
    when(cartServiceImpl.getCartItems(cartEntity, productId)).thenReturn(Optional.empty());
    when(baseService.getSellerIdByProductId(productId)).thenReturn(1);


    //test
    CartResponse cartResponse = cartService.addToCart(accountId, cartId, request);

    assertNotNull(cartResponse);
  }*/

  /*@Test
  public void test_addCampaignItemToCartByDirectly() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());
    int count = 2;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemResponse1.getProductId())
        .count(count)
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .addToCart(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId()) && cartItem.getSaleCount().equals(count)));
  }

  @Test
  public void test_addNonCampaignItemToCartByDirectly() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());
    int count = 2;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemResponse1.getProductId())
        .count(count)
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .addToCart(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId()) && cartItem.getSaleCount().equals(count)));
  }

  @Test
  public void test_addCampaignItemToCartByIncrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId())));

    //test
    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse1.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .incrementItem(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    int saleCountAfterIncrement = cartResponse
        .getItemList()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId()))
        .findFirst()
        .get()
        .getSaleCount();
    assertEquals(1, saleCountAfterIncrement);
  }

  @Test
  public void test_addNonCampaignItemToCartByIncrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());
    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId())));

    //test
    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse1.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .incrementItem(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    int saleCountAfterIncrement = cartResponse
        .getItemList()
        .stream()
        .filter(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId()))
        .findFirst()
        .get()
        .getSaleCount();
    assertEquals(1, saleCountAfterIncrement);
  }

  @Test
  public void test_removeCampaignItemByDirectly() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse1.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);

    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId())));

    //test
    CartItemRemoveRequest removeRequest = CartItemRemoveRequest.builder()
        .productId(itemResponse1.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponseBeforeDecrement = cartService
        .removeFromCart(userId,
            cartId,
            removeRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId())));
  }

  @Test
  public void test_removeNonCampaignItemByDirectly() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse2.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId())));

    //test
    CartItemRemoveRequest removeRequest = CartItemRemoveRequest.builder()
        .productId(itemResponse2.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponseBeforeDecrement = cartService
        .removeFromCart(userId,
            cartId,
            removeRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId())));
  }

  @Test
  public void test_removeCampaignItemByDecrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse1.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);

    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId())));

    //test
    CartItemDecrementRequest decrementRequest = CartItemDecrementRequest.builder()
        .productId(itemResponse1.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponseBeforeDecrement = cartService
        .decrementItem(userId,
            cartId,
            decrementRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse1.getProductId())));
  }

  @Test
  public void test_removeNonCampaignItemByDecrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse2.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId())));

    //test
    CartItemDecrementRequest decrementRequest = CartItemDecrementRequest.builder()
        .productId(itemResponse1.getProductId())
        .build();

    CartResponse<CartItemDto> cartResponseBeforeDecrement = cartService
        .decrementItem(userId,
            cartId,
            decrementRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse2.getProductId())));
  }

  @Test
  public void test_addCampaignItemToCartTillStockFull() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId(), 10);
    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse.getProductId(), 3, 1, 5, 2);

    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())));

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemResponse.getProductId())
        .count(10)
        .build();

    CartResponse<CartItemDto> cartResponse = cartService
        .addToCart(userId,
            cartId,
            request);

    assertNotNull(cartResponse);
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())).findFirst().get().getCampaignParams().getActualGiftCount().intValue());
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
  }

  @Test
  public void test_addNonCampaignItemToCartTillStockFull() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId(), 10);

    CartResponse<CartItemDto> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())));

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemResponse.getProductId())
        .count(10)
        .build();


    CartResponse<CartItemDto> cartResponse = cartService
        .addToCart(userId,
            cartId,
            request);

    assertNotNull(cartResponse);
    assertNull(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())).findFirst().get().getCampaignParams());
    assertFalse(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())).findFirst().get().getHasCampaign());
    assertEquals(10, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())).findFirst().get().getSaleCount().intValue());
  }*/

  /*@Test
  public void test_addCampaignItemToCartAfterCampaignLimitExpired(){
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId(), 10);
    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse.getProductId(), 3, 1, 5, 2);

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getProductId().equals(itemResponse.getProductId())));

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(userId,
            cartId,
            itemResponse.getProductId().toString(),
            String.valueOf(2));
    assertNotNull(cartId);

    SaleResponse firstSaleResponse = salesService.addSale(userId, cartId);
    assertNotNull(firstSaleResponse);

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(userId,
            cartId,
            itemResponse.getProductId().toString(),
            String.valueOf(2));

    SaleResponse secondSaleResponse = salesService.addSale(userId, cartId);
    assertNotNull(secondSaleResponse);


  }*/
}
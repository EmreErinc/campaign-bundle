package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.request.*;
import com.finartz.intern.campaignlogic.model.response.*;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.finartz.intern.campaignlogic.service.CartService;
import com.finartz.intern.campaignlogic.service.SalesService;
import com.finartz.intern.campaignlogic.service.SellerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class CartControllerTest extends BaseTestController {

  @Autowired
  private CartService cartService;

  @Autowired
  private SalesService salesService;

  @Autowired
  private SellerService sellerService;

  private RegisterResponse sellerAccountRegisterResponse;
  private String cartId;

  private ItemResponse itemResponse1;
  private ItemResponse itemResponse2;
  private ItemResponse itemResponse3;

  @Before
  public void initialize() {
    //create seller account
    sellerAccountRegisterResponse = generateSellerAccount();
    log.info("SELLER ACCOUNT CREATED : " + sellerAccountRegisterResponse.toString());

    //create seller
    SellerResponse sellerResponse = sellerService.addSeller(sellerAccountRegisterResponse.getId(), AddSellerRequest.builder()
        .name("Test Seller")
        .address("Test Mahallesi, Seller Sokak, No: 1, Daire: 1")
        .build());
    log.info("SELLER CREATED : " + sellerResponse.toString());

    //add item
    itemResponse1 = generateItem(sellerAccountRegisterResponse.getId());
    log.info("ITEM CREATED : " + itemResponse1.toString());
    itemResponse2 = generateItem(sellerAccountRegisterResponse.getId());
    log.info("ITEM CREATED : " + itemResponse2.toString());
    itemResponse3 = generateItem(sellerAccountRegisterResponse.getId());
    log.info("ITEM CREATED : " + itemResponse2.toString());

    //generate campaign
    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse1.getItemId());
    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse3.getItemId());
  }


  @Test
  public void test_addCampaignItemToCartByDirectly() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());
    int count = 2;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .itemId(itemResponse1.getItemId())
        .count(count)
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId()) && cartItem.getSaleCount().equals(count)));
  }

  @Test
  public void test_addNonCampaignItemToCartByDirectly() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());
    int count = 2;

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .itemId(itemResponse1.getItemId())
        .count(count)
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId()) && cartItem.getSaleCount().equals(count)));
  }

  @Test
  public void test_addCampaignItemToCartByIncrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId())));

    //test
    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .itemId(itemResponse1.getItemId())
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .incrementItem(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    int saleCountAfterIncrement = cartResponse
        .getItemList()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId()))
        .findFirst()
        .get()
        .getSaleCount();
    assertEquals(1, saleCountAfterIncrement);
  }

  @Test
  public void test_addNonCampaignItemToCartByIncrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    String cartId = getCartIdFromToken(registerResponse.getToken());
    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId())));

    //test
    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .itemId(itemResponse1.getItemId())
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .incrementItem(registerResponse.getId(),
            cartId,
            request);

    assertNotNull(cartResponse);
    int saleCountAfterIncrement = cartResponse
        .getItemList()
        .stream()
        .filter(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId()))
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

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .itemId(itemResponse1.getItemId())
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);

    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId())));

    //test
    CartItemRemoveRequest removeRequest = CartItemRemoveRequest.builder()
        .itemId(itemResponse1.getItemId())
        .build();

    CartResponse<CartItem> cartResponseBeforeDecrement = cartService
        .removeFromCart(userId,
            cartId,
            removeRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId())));
  }

  @Test
  public void test_removeNonCampaignItemByDirectly() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .itemId(itemResponse2.getItemId())
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId())));

    //test
    CartItemRemoveRequest removeRequest = CartItemRemoveRequest.builder()
        .itemId(itemResponse2.getItemId())
        .build();

    CartResponse<CartItem> cartResponseBeforeDecrement = cartService
        .removeFromCart(userId,
            cartId,
            removeRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId())));
  }

  @Test
  public void test_removeCampaignItemByDecrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .itemId(itemResponse1.getItemId())
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);

    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId())));

    //test
    CartItemDecrementRequest decrementRequest = CartItemDecrementRequest.builder()
        .itemId(itemResponse1.getItemId())
        .build();

    CartResponse<CartItem> cartResponseBeforeDecrement = cartService
        .decrementItem(userId,
            cartId,
            decrementRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse1.getItemId())));
  }

  @Test
  public void test_removeNonCampaignItemByDecrementItemCount() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId())));

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .itemId(itemResponse2.getItemId())
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .incrementItem(userId,
            cartId,
            request);
    assertTrue(cartResponse
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId())));

    //test
    CartItemDecrementRequest decrementRequest = CartItemDecrementRequest.builder()
        .itemId(itemResponse1.getItemId())
        .build();

    CartResponse<CartItem> cartResponseBeforeDecrement = cartService
        .decrementItem(userId,
            cartId,
            decrementRequest);
    assertFalse(cartResponseBeforeDecrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse2.getItemId())));
  }

  @Test
  public void test_addCampaignItemToCartTillStockFull() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId(), 10);
    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse.getItemId(), 3, 1, 5, 2);

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())));

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .itemId(itemResponse.getItemId())
        .count(10)
        .build();

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(userId,
            cartId,
            request);

    assertNotNull(cartResponse);
    assertEquals(2, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())).findFirst().get().getCampaignParams().getExpectedGiftCount().intValue());
    assertEquals(8, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())).findFirst().get().getCampaignParams().getTotalItemCount().intValue());
  }

  @Test
  public void test_addNonCampaignItemToCartTillStockFull() {
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId(), 10);

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())));

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .itemId(itemResponse.getItemId())
        .count(10)
        .build();


    CartResponse<CartItem> cartResponse = cartService
        .addToCart(userId,
            cartId,
            request);

    assertNotNull(cartResponse);
    assertNull(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())).findFirst().get().getCampaignParams());
    assertFalse(cartResponse.getItemList().stream().filter(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())).findFirst().get().getHasCampaign());
    assertEquals(10, cartResponse.getItemList().stream().filter(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())).findFirst().get().getSaleCount().intValue());
  }

  /*@Test
  public void test_addCampaignItemToCartAfterCampaignLimitExpired(){
    RegisterResponse registerResponse = generateUserAccount();
    int userId = registerResponse.getId();
    String cartId = getCartIdFromToken(registerResponse.getToken());

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId(), 10);
    generateCampaign(sellerAccountRegisterResponse.getId(), itemResponse.getItemId(), 3, 1, 5, 2);

    CartResponse<CartItem> cartResponseBeforeIncrement = cartService.getCart(cartId);
    assertFalse(cartResponseBeforeIncrement
        .getItemList()
        .stream()
        .anyMatch(cartItem -> cartItem.getItemId().equals(itemResponse.getItemId())));

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(userId,
            cartId,
            itemResponse.getItemId().toString(),
            String.valueOf(2));
    assertNotNull(cartId);

    SaleResponse firstSaleResponse = salesService.addSale(userId, cartId);
    assertNotNull(firstSaleResponse);

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(userId,
            cartId,
            itemResponse.getItemId().toString(),
            String.valueOf(2));

    SaleResponse secondSaleResponse = salesService.addSale(userId, cartId);
    assertNotNull(secondSaleResponse);


  }*/
}
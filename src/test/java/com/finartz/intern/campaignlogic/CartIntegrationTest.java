package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.request.AddItemToCartRequest;
import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemIncrementRequest;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignSpecifications;
import com.finartz.intern.campaignlogic.model.value.ItemGenerateResponse;
import com.finartz.intern.campaignlogic.model.value.Messages;
import com.finartz.intern.campaignlogic.service.CartService;
import com.finartz.intern.campaignlogic.service.SellerService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class CartIntegrationTest extends BaseTestController {

  @Autowired
  private WebApplicationContext wac;
  private MockMvc mockMvc;

  @Autowired
  private CartService cartService;

  @Autowired
  private SellerService sellerService;

  private RegisterResponse sellerAccountRegisterResponse;

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON, Charset.forName("UTF-8"));

  @Before
  public void initialize() {
    DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
    this.mockMvc = builder.build();

    //create seller account
    sellerAccountRegisterResponse = generateSellerAccount();
    log.info("SELLER ACCOUNT CREATED : " + sellerAccountRegisterResponse.toString());

    //create seller
    SellerResponse sellerResponse = sellerService.addSeller(sellerAccountRegisterResponse.getId(), AddSellerRequest.builder()
        .name("Test Seller")
        .address("Test Mahallesi, Seller Sokak, No: 1, Daire: 1")
        .build());
    log.info("SELLER CREATED : " + sellerResponse.toString());
  }

  @Test
  public void test_addCampaignItemToCartByDirectly() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 2;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(20, campaignSpecifications);

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .count(count)
        .build();

    //test
    mockMvc.perform(post("/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemGenerateResponse.getPrice() * count)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(true)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.actualGiftCount", is(0)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.totalItemCount", is(2)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.requirement", is(4)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.gift", is(1)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addNonCampaignItemToCartByDirectly() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 2;

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId());
    log.info("ITEM CREATED : " + itemResponse.toString());

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemResponse.getProductId())
        .count(count)
        .build();

    //test
    mockMvc.perform(post("/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemResponse.getPrice() * count)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(false)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addCampaignItemToCartByIncrementItemCount() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(20, campaignSpecifications);

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .build();

    //test
    mockMvc.perform(post("/cart/inc")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(1)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(1)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemGenerateResponse.getPrice() * 1)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(true)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.actualGiftCount", is(0)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.totalItemCount", is(1)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.requirement", is(4)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.gift", is(1)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addNonCampaignItemToCartByIncrementItemCount() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId());
    log.info("ITEM CREATED : " + itemResponse.toString());

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse.getProductId())
        .build();

    //test
    mockMvc.perform(post("/cart/inc")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(1)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(1)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemResponse.getPrice() * 1)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(false)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addCampaignItemToCartAndDecrease_WhenHasGotGift() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 4;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(20, campaignSpecifications);

    //add to cart before test
    AddItemToCartRequest addItemToCartRequest = AddItemToCartRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .count(count)
        .build();

    CartResponse cartResponse = cartService.addToCart(registerResponse.getId(), getCartIdFromToken(registerResponse.getToken()), addItemToCartRequest);
    assertNotNull(cartResponse);

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .build();

    //test
    mockMvc.perform(post("/cart/dec")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(3)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(3)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemGenerateResponse.getPrice() * 3)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(true)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.actualGiftCount", is(0)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.totalItemCount", is(3)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.requirement", is(4)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.gift", is(1)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addNonCampaignItemToCartAndDecrease() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 4;

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId());
    log.info("ITEM CREATED : " + itemResponse.toString());

    AddItemToCartRequest addItemToCartRequest = AddItemToCartRequest.builder()
        .productId(itemResponse.getProductId())
        .count(count)
        .build();

    CartResponse cartResponse = cartService.addToCart(registerResponse.getId(), getCartIdFromToken(registerResponse.getToken()), addItemToCartRequest);
    assertNotNull(cartResponse);

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse.getProductId())
        .build();

    mockMvc.perform(post("/cart/dec")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(3)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(3)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemResponse.getPrice() * 3)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(false)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addCampaignItemToCartAndIncrease_WhenReachToCampaignRequirementCount() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 3;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(20, campaignSpecifications);

    //add to cart before test
    AddItemToCartRequest addItemToCartRequest = AddItemToCartRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .count(count)
        .build();

    CartResponse cartResponse = cartService.addToCart(registerResponse.getId(), getCartIdFromToken(registerResponse.getToken()), addItemToCartRequest);
    assertNotNull(cartResponse);

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .build();

    //test
    mockMvc.perform(post("/cart/inc")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(4)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(4)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemGenerateResponse.getPrice() * 4)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(true)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.actualGiftCount", is(1)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.totalItemCount", is(4)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.requirement", is(4)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.gift", is(1)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addNonCampaignItemToCartAndIncrease() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 3;

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId());
    log.info("ITEM CREATED : " + itemResponse.toString());

    AddItemToCartRequest addItemToCartRequest = AddItemToCartRequest.builder()
        .productId(itemResponse.getProductId())
        .count(count)
        .build();

    CartResponse cartResponse = cartService.addToCart(registerResponse.getId(), getCartIdFromToken(registerResponse.getToken()), addItemToCartRequest);
    assertNotNull(cartResponse);

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse.getProductId())
        .build();

    mockMvc.perform(post("/cart/inc")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(4)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(4)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemResponse.getPrice() * 4)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(false)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_removeCampaignItemByDirectly() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 4;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(20, campaignSpecifications);

    //add to cart before test
    AddItemToCartRequest addItemToCartRequest = AddItemToCartRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .count(count)
        .build();

    CartResponse cartResponse = cartService.addToCart(registerResponse.getId(), getCartIdFromToken(registerResponse.getToken()), addItemToCartRequest);
    assertNotNull(cartResponse);

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .build();

    //test
    mockMvc.perform(post("/cart/remove")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(0)));
  }

  @Test
  public void test_removeNonCampaignItemByDirectly() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 4;

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId());

    //add to cart before test
    AddItemToCartRequest addItemToCartRequest = AddItemToCartRequest.builder()
        .productId(itemResponse.getProductId())
        .count(count)
        .build();

    CartResponse cartResponse = cartService.addToCart(registerResponse.getId(), getCartIdFromToken(registerResponse.getToken()), addItemToCartRequest);
    assertNotNull(cartResponse);

    CartItemIncrementRequest request = CartItemIncrementRequest.builder()
        .productId(itemResponse.getProductId())
        .build();

    //test
    mockMvc.perform(post("/cart/remove")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(0)));
  }

  @Test
  public void test_addCampaignItemToCart_WhenStockSufficient() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 15;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(10, campaignSpecifications);

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .count(count)
        .build();

    //test
    mockMvc.perform(post("/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(8)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemGenerateResponse.getPrice() * 8)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(true)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.actualGiftCount", is(2)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.totalItemCount", is(8)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.requirement", is(4)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.gift", is(1)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is(Messages.CART_UPDATED.getValue())));
  }

  @Test
  public void test_addNonCampaignItemToCart_WhenStockSufficient() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 15;

    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId(), 10);

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemResponse.getProductId())
        .count(count)
        .build();

    //test
    mockMvc.perform(post("/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(10)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemResponse.getPrice() * 10)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(false)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addCampaignItemToCart_WhenCartLimitExceed() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 20;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(25, campaignSpecifications);

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .count(count)
        .build();

    //test
    mockMvc.perform(post("/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemGenerateResponse.getPrice() * count)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(true)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.actualGiftCount", is(3)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.totalItemCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.requirement", is(4)))
        .andExpect(jsonPath("$.itemList[0].campaignParams.badge.gift", is(1)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));
  }

  @Test
  public void test_addCampaignItemToCart_WhenCampaignLimitExceed() throws Exception {
    RegisterResponse registerResponse = generateUserAccount();
    int count = 7;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(4)
        .giftCount(1)
        .build();

    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(40, campaignSpecifications);

    generateSale(registerResponse.getId(), itemGenerateResponse.getProductId(), 8, 2, itemGenerateResponse.getPrice());
    generateSale(registerResponse.getId(), itemGenerateResponse.getProductId(), 10, 2, itemGenerateResponse.getPrice());
    generateSale(registerResponse.getId(), itemGenerateResponse.getProductId(), 12, 3, itemGenerateResponse.getPrice());

    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(itemGenerateResponse.getProductId())
        .count(count)
        .build();

    //test
    mockMvc.perform(post("/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.itemList", hasSize(1)))
        .andExpect(jsonPath("$.itemList[0].productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.itemList[0].sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.itemList[0].desiredSaleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].saleCount", is(count)))
        .andExpect(jsonPath("$.itemList[0].price", is(itemGenerateResponse.getPrice() * count)))
        .andExpect(jsonPath("$.itemList[0].hasCampaign", is(false)))
        .andExpect(jsonPath("$.itemList[0].hasVariant", is(false)))
        .andExpect(jsonPath("$.itemList[0].variant", nullValue()))
        .andExpect(jsonPath("$.itemList[0].message", is("")));

  }
}
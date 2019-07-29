package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignSpecifications;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.finartz.intern.campaignlogic.model.value.Messages;
import com.finartz.intern.campaignlogic.service.SalesService;
import com.finartz.intern.campaignlogic.service.SellerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class SalesIntegrationTest extends BaseTestController {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Autowired
  private WebApplicationContext wac;
  private MockMvc mockMvc;

  @Autowired
  private SellerService sellerService;

  @Autowired
  private SalesService salesService;

  private RegisterResponse sellerAccountRegisterResponse;
  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON, Charset.forName("UTF-8"));

  @Before
  public void initialize() {
    DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
    this.mockMvc = builder.build();

    //pre-save variant specs to db
    prepareVariantSpecs();

    //create seller account
    sellerAccountRegisterResponse = generateSellerAccount();
    log.info("SELLER ACCOUNT CREATED : " + sellerAccountRegisterResponse.toString());

    SellerResponse sellerResponse = sellerService.addSeller(sellerAccountRegisterResponse.getId(), AddSellerRequest.builder()
        .name("Test Seller")
        .address("Test Mahallesi, Seller Sokak, No: 1, Daire: 1")
        .build());
    log.info("SELLER CREATED : " + sellerResponse.toString());
  }

  @Test
  public void test_addSale() throws Exception {
    RegisterResponse registerResponse = generateSellerAccount();

    CartItem cartItem1 = generateCartItem(sellerAccountRegisterResponse.getId(), 10, 5, true, false, Optional.empty());
    CartItem cartItem2 = generateCartItem(sellerAccountRegisterResponse.getId(), 15, 6, false, false, Optional.empty());

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem1);
    cartItems.add(cartItem2);

    CartEntity cartEntity = CartEntity.builder()
        .id(getCartIdFromToken(registerResponse.getToken()))
        .accountId(getAccountIdFromToken(registerResponse.getToken()).get())
        .cartItems(cartItems)
        .build();

    updateCart(cartEntity);

    //test
    mockMvc.perform(post("/sale")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.saleIds", hasSize(2)));
  }

  @Test
  public void test_addSale_WhenStockChanged() throws Exception {
    thrown.expectMessage(Messages.ONE_OR_MORE_PRODUCT_ITEM_UNFIT.getValue());

    RegisterResponse registerResponse = generateSellerAccount();
    int desiredSaleCount1 = 5;
    int desiredSaleCount2 = 6;

    CartItem cartItem1 = generateCartItem(sellerAccountRegisterResponse.getId(), 10, desiredSaleCount1, true, false, Optional.empty());
    CartItem cartItem2 = generateCartItem(sellerAccountRegisterResponse.getId(), 15, desiredSaleCount2, false, false, Optional.empty());

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem1);
    cartItems.add(cartItem2);

    CartEntity cartEntity = CartEntity.builder()
        .id(getCartIdFromToken(registerResponse.getToken()))
        .accountId(getAccountIdFromToken(registerResponse.getToken()).get())
        .cartItems(cartItems)
        .build();

    updateCart(cartEntity);

    decreaseStock(cartItem1.getProductId(), Optional.of(cartItem1.getVariant().getId()), 8);
    decreaseStock(cartItem2.getProductId(), Optional.empty(), 10);

    //test
    mockMvc.perform(post("/sale")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken()))
        .andDo(print())
        .andExpect(status().is(500));
  }

  @Test
  public void test_addSale_WhenStockChanged_ThenRecalculatedCart() throws Exception {
    //should thrown
    thrown.expectMessage(Messages.ONE_OR_MORE_PRODUCT_ITEM_UNFIT.getValue());

    RegisterResponse registerResponse = generateSellerAccount();
    int desiredSaleCount1 = 5;
    int desiredSaleCount2 = 6;

    CartItem cartItem1 = generateCartItem(sellerAccountRegisterResponse.getId(), 10, desiredSaleCount1, true, false, Optional.empty());
    CartItem cartItem2 = generateCartItem(sellerAccountRegisterResponse.getId(), 15, desiredSaleCount2, false, false, Optional.empty());

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem1);
    cartItems.add(cartItem2);

    CartEntity cartEntity = CartEntity.builder()
        .id(getCartIdFromToken(registerResponse.getToken()))
        .accountId(getAccountIdFromToken(registerResponse.getToken()).get())
        .cartItems(cartItems)
        .build();

    updateCart(cartEntity);

    decreaseStock(cartItem1.getProductId(), Optional.of(cartItem1.getVariant().getId()), 8);
    decreaseStock(cartItem2.getProductId(), Optional.empty(), 10);

    //trigger recalculate
    salesService.addSale(registerResponse.getId(), cartEntity.getId());

    //recalculated cart test
    mockMvc.perform(post("/sale")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.saleIds", hasSize(2)));
  }

  @Test
  public void test_addSale_WithCampaignItem_WhenStockChanged_ThenRecalculatedCart() throws Exception {
    //should thrown
    thrown.expectMessage(Messages.ONE_OR_MORE_PRODUCT_ITEM_UNFIT.getValue());

    RegisterResponse registerResponse = generateSellerAccount();
    int desiredSaleCount1 = 5;
    int desiredSaleCount2 = 6;

    CampaignSpecifications campaignSpecifications = CampaignSpecifications.builder()
        .sellerId(sellerAccountRegisterResponse.getId())
        .cartLimit(3)
        .campaignLimit(3)
        .requirementCount(3)
        .giftCount(1)
        .build();

    CartItem cartItem1 = generateCartItem(sellerAccountRegisterResponse.getId(), 10, desiredSaleCount1, true, true, Optional.of(campaignSpecifications));
    CartItem cartItem2 = generateCartItem(sellerAccountRegisterResponse.getId(), 15, desiredSaleCount2, false, true, Optional.of(campaignSpecifications));

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem1);
    cartItems.add(cartItem2);

    CartEntity cartEntity = CartEntity.builder()
        .id(getCartIdFromToken(registerResponse.getToken()))
        .accountId(getAccountIdFromToken(registerResponse.getToken()).get())
        .cartItems(cartItems)
        .build();

    updateCart(cartEntity);

    decreaseStock(cartItem1.getProductId(), Optional.of(cartItem1.getVariant().getId()), 5);
    decreaseStock(cartItem2.getProductId(), Optional.empty(), 10);

    //trigger recalculate
    salesService.addSale(registerResponse.getId(), cartEntity.getId());

    //recalculated cart test
    mockMvc.perform(post("/sale")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.saleIds", hasSize(2)));
  }
}
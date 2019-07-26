package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.finartz.intern.campaignlogic.service.SalesService;
import com.finartz.intern.campaignlogic.service.SellerService;
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
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class SalesIntegrationTest extends BaseTestController{

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

    //create seller account
    sellerAccountRegisterResponse = generateSellerAccount();
    log.info("SELLER ACCOUNT CREATED : " + sellerAccountRegisterResponse.toString());

    SellerResponse sellerResponse = sellerService.addSeller(sellerAccountRegisterResponse.getId(), AddSellerRequest.builder()
        .name("Test Seller")
        .address("Test Mahallesi, Seller Sokak, No: 1, Daire: 1")
        .build());
    log.info("SELLER CREATED : " + sellerResponse.toString());

    prepareVariantSpecs();
  }

  @Test
  public void test_addSale() throws Exception{
    RegisterResponse registerResponse = generateSellerAccount();

    CartItem cartItem1 = generateCartItem(sellerAccountRegisterResponse.getId(), 10, 5,true);
    CartItem cartItem2 = generateCartItem(sellerAccountRegisterResponse.getId(), 15, 6,false);

    List<CartItem> cartItems = new ArrayList<>();
    cartItems.add(cartItem1);
    cartItems.add(cartItem2);

    CartEntity cartEntity = CartEntity.builder()
        .id(getCartIdFromToken(registerResponse.getToken()))
        .accountId(getAccountIdFromToken(registerResponse.getToken()).get())
        .cartItems(cartItems)
        .build();

    updateCart(cartEntity);

    //TODO variant issue when adding
    mockMvc.perform(post("/sale")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + registerResponse.getToken()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));
  }
}

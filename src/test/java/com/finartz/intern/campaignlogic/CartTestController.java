package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.finartz.intern.campaignlogic.service.CartService;
import com.finartz.intern.campaignlogic.service.SellerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class CartTestController extends BaseTestController {

  @Autowired
  private CartService cartService;


  @Autowired
  private SellerService sellerService;

  private RegisterResponse sellerAccountRegisterResponse;

  private ItemResponse itemResponse1;
  private ItemResponse itemResponse2;
  private ItemResponse itemResponse3;
  private Integer sellerAccountId;

  @Before
  public void initialize() {
    //create seller account
    sellerAccountRegisterResponse = generateSellerAccount();
    sellerAccountId = sellerAccountRegisterResponse.getId();
    log.info("SELLER ACCOUNT CREATED : " + sellerAccountRegisterResponse.toString());

    //create seller
    SellerResponse sellerResponse = sellerService.addSeller(sellerAccountId, AddSellerRequest.builder()
        .name("Test Seller")
        .address("Test Mahallesi, Seller Sokak, No: 1, Daire: 1")
        .build());
    log.info("SELLER CREATED : " + sellerResponse.toString());

    //add item
    itemResponse1 = generateItem(sellerAccountId);
    log.info("ITEM CREATED : " + itemResponse1.toString());
    itemResponse2 = generateItem(sellerAccountId);
    log.info("ITEM CREATED : " + itemResponse2.toString());
    itemResponse3 = generateItem(sellerAccountId);
    log.info("ITEM CREATED : " + itemResponse2.toString());

    //generate campaign
    generateCampaign(sellerAccountId, itemResponse1.getItemId());
    generateCampaign(sellerAccountId, itemResponse3.getItemId());
  }


  public void addItemToCart() {
    RegisterResponse userRegisterResponse = generateUserAccount();

    String cartId = getCartIdFromToken(userRegisterResponse.getToken());

    String itemId = String.valueOf(new Random().nextInt(10) + 1);

    CartResponse<CartItem> cartResponse = cartService
        .addToCart(userRegisterResponse.getId(),
            cartId,
            itemResponse1.getItemId().toString(),
            itemId);


    assertNotNull(cartResponse);
    assertTrue(cartResponse.getItemList().stream().anyMatch(cartItem -> cartItem.getItemId().toString().equals(itemId)));
  }


}

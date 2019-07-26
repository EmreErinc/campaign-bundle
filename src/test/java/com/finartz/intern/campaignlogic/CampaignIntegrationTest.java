package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class CampaignIntegrationTest extends BaseTestController {

  @Autowired
  private WebApplicationContext wac;
  private MockMvc mockMvc;

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

    SellerResponse sellerResponse = sellerService.addSeller(sellerAccountRegisterResponse.getId(), AddSellerRequest.builder()
        .name("Test Seller")
        .address("Test Mahallesi, Seller Sokak, No: 1, Daire: 1")
        .build());
    log.info("SELLER CREATED : " + sellerResponse.toString());
  }

  @Test
  public void test_addCampaign() throws Exception {
    ItemResponse itemResponse = generateItem(sellerAccountRegisterResponse.getId());

    String title = "test campaign";
    int requirementCount = 2;
    int giftCount = 1;
    int cartLimit = 3;
    int campaignLimit = 3;

    AddCampaignRequest request = AddCampaignRequest.builder()
        .productId(itemResponse.getProductId())
        .title(title)
        .requirement(requirementCount)
        .gift(giftCount)
        .cartLimit(cartLimit)
        .campaignLimit(campaignLimit)
        .startAt(1564129933353L)
        .endAt(1570654800000L)
        .build();

    mockMvc.perform(post("/campaign")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + sellerAccountRegisterResponse.getToken())
        .content(new Gson().toJson(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.productId", is(itemResponse.getProductId())))
        .andExpect(jsonPath("$.sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.title", is(title)))
        .andExpect(jsonPath("$.cartLimit", is(cartLimit)))
        .andExpect(jsonPath("$.campaignLimit", is(campaignLimit)))
        .andExpect(jsonPath("$.status", is(CampaignStatus.ACTIVE.toString())))
        .andExpect(jsonPath("$.badge.requirement", is(requirementCount)))
        .andExpect(jsonPath("$.badge.gift", is(giftCount)));
  }

  @Test
  public void test_cancelCampaign() throws Exception {
    int requirementCount = 2;
    int giftCount = 1;
    int cartLimit = 3;
    int campaignLimit = 3;
    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(sellerAccountRegisterResponse.getId(), 10, cartLimit, campaignLimit, requirementCount, giftCount);

    mockMvc.perform(get("/campaign/" + itemGenerateResponse.getCampaignId() + "/cancel")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + sellerAccountRegisterResponse.getToken()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andReturn();
  }

  @Test
  public void test_activateCampaign() throws Exception {
    int requirementCount = 2;
    int giftCount = 1;
    int cartLimit = 3;
    int campaignLimit = 3;
    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaignCanceledStatus(sellerAccountRegisterResponse.getId(), 10, cartLimit, campaignLimit, requirementCount, giftCount);

    mockMvc.perform(get("/campaign/" + itemGenerateResponse.getCampaignId() + "/active")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + sellerAccountRegisterResponse.getToken()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andReturn();
  }

  @Test
  public void test_getCampaign() throws Exception {
    int requirementCount = 2;
    int giftCount = 1;
    int cartLimit = 3;
    int campaignLimit = 3;
    ItemGenerateResponse itemGenerateResponse = generateItemWithCampaign(sellerAccountRegisterResponse.getId(), 10, cartLimit, campaignLimit, requirementCount, giftCount);

    mockMvc.perform(get("/campaign/" + itemGenerateResponse.getCampaignId())
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + sellerAccountRegisterResponse.getToken()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.productId", is(itemGenerateResponse.getProductId())))
        .andExpect(jsonPath("$.sellerId", is(sellerAccountRegisterResponse.getId())))
        .andExpect(jsonPath("$.title", is("custom campaign")))
        .andExpect(jsonPath("$.cartLimit", is(cartLimit)))
        .andExpect(jsonPath("$.campaignLimit", is(campaignLimit)))
        .andExpect(jsonPath("$.status", is(CampaignStatus.ACTIVE.toString())))
        .andExpect(jsonPath("$.badge.requirement", is(requirementCount)))
        .andExpect(jsonPath("$.badge.gift", is(giftCount)));
  }
}
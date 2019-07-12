package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.request.AddCampaignRequest;
import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.CampaignResponse;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.value.CargoType;
import com.finartz.intern.campaignlogic.security.JwtTokenProvider;
import com.finartz.intern.campaignlogic.service.AccountService;
import com.finartz.intern.campaignlogic.service.CampaignService;
import com.finartz.intern.campaignlogic.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Slf4j
public abstract class BaseTestController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private CampaignService campaignService;

  @Autowired
  private ItemService itemService;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  public ItemResponse generateItem(int accountId) {
    String[] cargoList = {"MNG Kargo", "Sürat Kargo", "Yurtiçi Kargo", "PTT", "UPS"};

    return itemService.addItem(accountId, AddItemRequest.builder()
        .name("test-item-" + Instant.now().toEpochMilli())
        .description("item-description-1")
        .price(new Random().nextDouble() * 50D)
        .stock(23)
        .cargoType(CargoType.values()[new Random().nextInt(2)])
        .cargoCompany(cargoList[new Random().nextInt(5)])
        .build());
  }

  public CampaignResponse generateCampaign(int accountId, int itemId) {
    return campaignService.addCampaign(accountId, AddCampaignRequest.builder()
        .title("other campaign")
        .itemId(itemId)
        .cartLimit(new Random().nextInt(3))
        .campaignLimit(new Random().nextInt(4) + 2)
        .requirement(new Random().nextInt(6) + 2)
        .gift(new Random().nextInt(3))
        .build());
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

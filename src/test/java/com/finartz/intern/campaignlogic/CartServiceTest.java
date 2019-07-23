package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.entity.CampaignEntity;
import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.entity.VariantEntity;
import com.finartz.intern.campaignlogic.model.request.AddItemToCartRequest;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.model.value.CampaignStatus;
import com.finartz.intern.campaignlogic.model.value.CargoType;
import com.finartz.intern.campaignlogic.repository.*;
import com.finartz.intern.campaignlogic.service.CartService;
import com.finartz.intern.campaignlogic.service.CartServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class CartServiceTest {

  @Autowired
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
  }

  @Test
  public void addCampaignItemToCart_Directly_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;
    int sellerId = 50;
    int campaignId = 10;
    AddItemToCartRequest request = AddItemToCartRequest.builder()
        .productId(productId)
        .count(count)
        .variantId(variantId)
        .build();

//    CartDto cartDto = CartDto.builder()
//        .accountId(accountId)
//        .cartId(cartId)
//        .variantId(Optional.of(variantId))
//        .desiredCount(count)
//        .productId(productId)
//        .build();

    CampaignEntity campaignEntity = CampaignEntity.builder()
        .id(campaignId)
        .productId(productId)
        .requirementCount(3)
        .giftCount(1)
        .cartLimit(2)
        .campaignLimit(2)
        .status(CampaignStatus.ACTIVE)
        .sellerId(sellerId)
        .startAt(1562939866630L)
        .endAt(1577653200000L)
        .createdAt(1563862109909L)
        .title("3 Alana 1 Bedava")
        .build();

    CartEntity cartEntity = CartEntity.builder()
        .id(cartId)
        .accountId(accountId)
        .cartItems(new ArrayList<>())
        .build();

//    VariantSpec variantSpec1 = VariantSpec.builder()
//        .id(100)
//        .specDetail("Dış Renk")
//        .specData("Mavi")
//        .build();
//
//    VariantSpec variantSpec2 = VariantSpec.builder()
//        .id(101)
//        .specDetail("Ebat")
//        .specData("Small")
//        .build();
//
//    VariantSpec variantSpec3 = VariantSpec.builder()
//        .id(102)
//        .specDetail("Üretim Yılı")
//        .specData("2018")
//        .build();
//
//    List<VariantSpec> variantSpecs = new ArrayList<>();
//    variantSpecs.add(variantSpec1);
//    variantSpecs.add(variantSpec2);
//    variantSpecs.add(variantSpec3);
//
//    Variant variant = Variant.builder()
//        .id(variantId)
//        .stock(15)
//        .price(12.3)
//        .variantSpecs(variantSpecs)
//        .build();

    VariantEntity variantEntity = VariantEntity.builder()
        .id(variantId)
        .stock(15)
        .price(12.3)
        .productId(productId)
        .build();

    ItemEntity itemEntity = ItemEntity.builder()
        .id(productId)
        .sellerId(sellerId)
        .stock(20)
        .cargoType(CargoType.FREE)
        .cargoCompany("MNG Kargo")
        .createdAt(1563892923731L)
        .name("Gömlek")
        .description("açıklama")
        .price(12.3)
        .build();

    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(cartRepository.findCart(cartId))
        .thenReturn(Optional.of(cartEntity));
    when(campaignRepository.findByProductId(productId))
        .thenReturn(Optional.of(campaignEntity));
    when(campaignRepository.findByIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(campaignEntity.getId()), anyLong(), anyLong()))
        .thenReturn(Optional.of(campaignEntity));
    when(variantRepository.findById(variantId))
        .thenReturn(Optional.of(variantEntity));
    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));
    when(campaignRepository.findById(campaignId))
        .thenReturn(Optional.of(campaignEntity));

    //test
    CartResponse cartResponse = cartService.addToCart(accountId, cartId, request);

    assertNotNull(cartResponse);
  }
}

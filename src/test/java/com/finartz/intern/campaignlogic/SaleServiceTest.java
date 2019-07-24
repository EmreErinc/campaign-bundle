package com.finartz.intern.campaignlogic;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.ItemEntity;
import com.finartz.intern.campaignlogic.model.entity.SalesEntity;
import com.finartz.intern.campaignlogic.model.response.SaleResponse;
import com.finartz.intern.campaignlogic.model.value.*;
import com.finartz.intern.campaignlogic.repository.*;
import com.finartz.intern.campaignlogic.service.CartServiceImpl;
import com.finartz.intern.campaignlogic.service.SalesService;
import com.finartz.intern.campaignlogic.service.SalesServiceImpl;
import lombok.extern.slf4j.Slf4j;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "test")
public class SaleServiceTest {

  @Spy
  private SalesService salesService;

  @Mock
  private CartServiceImpl cartServiceImpl;
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
    salesService = new SalesServiceImpl(cartRepository,
        sellerRepository,
        accountRepository,
        campaignRepository,
        itemRepository,
        salesRepository,
        variantRepository,
        variantSpecRepository,
        specDataRepository,
        specDetailRepository,
        cartServiceImpl);
  }

  @Test
  public void addSale_SuitableItemStock_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 5;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 25;
    int saleId = 70;

    CartItem cartItem = CartItem.builder()
        .productId(productId)
        .sellerId(sellerId)
        .hasVariant(false)
        .variant(null)
        .hasCampaign(true)
        .campaignParams(CampaignParams.builder().totalItemCount(5).actualGiftCount(1).badge(Badge.builder().requirement(3).gift(1).build()).build())
        .desiredSaleCount(count)
        .saleCount(count)
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

    ItemEntity itemEntity = ItemEntity.builder()
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

    SalesEntity salesEntity = SalesEntity.builder()
        .id(saleId)
        .productId(productId)
        .price(12.3)
        .soldAt(1563948163800L)
        .saleCount(5)
        .giftCount(1)
        .ownerId(accountId)
        .variantId(0)
        .build();

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
    when(salesRepository.save(any()))
        .thenReturn(salesEntity);

    //test
    SaleResponse saleResponse = salesService.addSale(accountId, cartId);
    assertNotNull(saleResponse);
    assertTrue(saleResponse.getSaleIds().stream().anyMatch(id -> id.equals(saleId)));
  }

  @Test
  public void addSale_UnsuitableItemStock_RecalculateCart_ShouldPass() {
    int accountId = 1;
    String cartId = "5d1df2814d6a4b0a745457d3";
    int productId = 1;
    int variantId = 2;
    int count = 10;
    int sellerId = 50;
    int campaignId = 10;
    int totalStock = 5;

    CartItem cartItem = CartItem.builder()
        .productId(productId)
        .sellerId(sellerId)
        .hasVariant(false)
        .variant(null)
        .hasCampaign(true)
        .campaignParams(CampaignParams.builder().totalItemCount(count).actualGiftCount(2).badge(Badge.builder().requirement(3).gift(1).build()).build())
        .desiredSaleCount(count)
        .saleCount(count)
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

    ItemEntity itemEntity = ItemEntity.builder()
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

    when(itemRepository.findById(productId))
        .thenReturn(Optional.of(itemEntity));
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

    //test
    try {
      salesService.addSale(accountId, cartId);
    } catch (ApplicationContextException ex) {
      assertEquals(Messages.ONE_OR_MORE_PRODUCT_ITEM_UNFIT.getValue(), ex.getMessage());
    }

    verify(cartServiceImpl, times(1)).recalculateCartItems(eq(accountId), eq(cartId), any());
  }
}

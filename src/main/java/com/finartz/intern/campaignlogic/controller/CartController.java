package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.AddItemToCartRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemDecrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemIncrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemRemoveRequest;
import com.finartz.intern.campaignlogic.model.response.CartResponse;
import com.finartz.intern.campaignlogic.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController extends BaseController {
  private final CartService cartService;

  @Autowired
  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PostMapping("/add")
  public CartResponse addToCart(@RequestHeader HttpHeaders headers, @RequestBody AddItemToCartRequest request) {
    return cartService.addToCart(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), request);
  }

  @PostMapping("/remove")
  public CartResponse removeFromCart(@RequestHeader HttpHeaders headers, @RequestBody CartItemRemoveRequest request) {
    return cartService.removeFromCart(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), request);
  }

  @PostMapping("/inc")
  public CartResponse incrementItem(@RequestHeader HttpHeaders headers, @RequestBody CartItemIncrementRequest request) {
    return cartService.incrementItem(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), request);
  }

  @PostMapping("/dec")
  public CartResponse decrementItem(@RequestHeader HttpHeaders headers, @RequestBody CartItemDecrementRequest request) {
    return cartService.decrementItem(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), request);
  }

  @GetMapping
  public CartResponse getMyCart(@RequestHeader HttpHeaders headers) {
    return cartService.getCart(getCartIdFromHeader(headers));
  }
}
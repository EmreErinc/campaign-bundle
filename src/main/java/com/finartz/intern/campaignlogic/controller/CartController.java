package com.finartz.intern.campaignlogic.controller;

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

  @GetMapping("/add/{itemId}/{count}")
  public CartResponse addToCart(@RequestHeader HttpHeaders headers, @PathVariable String itemId, @PathVariable String count) {
    return cartService.addToCart(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), itemId, count);
  }

  @GetMapping("/{itemId}/remove")
  public CartResponse removeFromCart(@RequestHeader HttpHeaders headers, @PathVariable String itemId) {
    return cartService.removeFromCart(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), itemId);
  }

  @GetMapping("/{itemId}/inc")
  public CartResponse incrementItem(@RequestHeader HttpHeaders headers, @PathVariable String itemId) {
    return cartService.incrementItem(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), itemId);
  }

  @GetMapping("/{itemId}/dec")
  public CartResponse decrementItem(@RequestHeader HttpHeaders headers, @PathVariable String itemId) {
    return cartService.decrementItem(getAccountIdFromHeader(headers).get(), getCartIdFromHeader(headers), itemId);
  }

  @GetMapping
  public CartResponse getMyCart(@RequestHeader HttpHeaders headers){
    return cartService.getCart(getCartIdFromHeader(headers));
  }
}
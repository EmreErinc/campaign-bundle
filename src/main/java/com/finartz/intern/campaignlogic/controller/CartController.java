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
  public boolean addToCart(@RequestHeader HttpHeaders headers, @PathVariable String itemId, @PathVariable String count) {
    return cartService.addToCart(getAccountIdFromHeader(headers), getCartIdFromHeader(headers), itemId, count);
  }

  @GetMapping("/remove/{itemId}")
  public boolean removeFromCart(@RequestHeader HttpHeaders headers, @PathVariable String itemId) {
    return cartService.removeFromCart(getAccountIdFromHeader(headers), getCartIdFromHeader(headers), itemId);
  }

  @GetMapping("/inc/{itemId}")
  public boolean incrementItem(@RequestHeader HttpHeaders headers, @PathVariable String itemId) {
    return cartService.incrementItem(getAccountIdFromHeader(headers), getCartIdFromHeader(headers), itemId);
  }

  @GetMapping("/dec/{itemId}")
  public boolean decrementItem(@RequestHeader HttpHeaders headers, @PathVariable String itemId) {
    return cartService.decrementItem(getAccountIdFromHeader(headers), getCartIdFromHeader(headers), itemId);
  }

  @GetMapping
  public CartResponse getMyCart(@RequestHeader HttpHeaders headers){
    return cartService.getCart(getCartIdFromHeader(headers));
  }
}

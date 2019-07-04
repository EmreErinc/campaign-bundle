package com.finartz.intern.campaignlogic.controller;

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
  public boolean addToCart(@RequestHeader HttpHeaders header, @PathVariable String itemId, @PathVariable String count) {
    return cartService.addToCart(getAccountIdFromHeader(header), itemId, count);
  }

  @GetMapping("/remove/{id}")
  public boolean removeFromCart(@RequestHeader HttpHeaders header, @PathVariable String id) {
    return cartService.removeFromCart(getCartIdFromHeader(header), id);
  }

  @GetMapping("/inc/{id}")
  public boolean incrementItem(@RequestHeader HttpHeaders headers, @PathVariable String id) {
    return cartService.incrementItem(getAccountIdFromHeader(headers), id);
  }

  @GetMapping("/dec/{id}")
  public boolean decrementItem(@RequestHeader HttpHeaders headers, @PathVariable String id) {
    return cartService.decrementItem(getAccountIdFromHeader(headers), id);
  }
}

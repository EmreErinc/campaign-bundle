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

  @GetMapping("/add/{id}/{count}")
  public boolean addToCart(@RequestHeader HttpHeaders header, @PathVariable String id, @PathVariable String count) {
    return cartService.addToCart(getAccountIdFromHeader(header), id, count);
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

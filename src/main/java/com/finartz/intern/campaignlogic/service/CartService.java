package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.response.CartResponse;

public interface CartService {
  CartResponse addToCart(int accountId, String cartId, String itemId, String count);

  CartResponse removeFromCart(int accountId, String cartId, String itemId);

  CartResponse incrementItem(int accountId, String cartId, String itemId);

  CartResponse decrementItem(int accountId, String cartId, String itemId);

  String createCart(int accountId);

  CartResponse getCart(String cartIdFromHeader);
}

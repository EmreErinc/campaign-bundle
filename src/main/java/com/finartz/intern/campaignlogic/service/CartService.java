package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.response.CartResponse;

public interface CartService {
  boolean addToCart(int accountId, String cartId, String itemId, String count);

  boolean removeFromCart(int accountId, String cartId, String itemId);

  boolean incrementItem(int accountId, String cartId, String itemId);

  boolean decrementItem(int accountId, String cartId, String itemId);

  String createCart(int accountId);

  CartResponse getCart(String cartIdFromHeader);
}

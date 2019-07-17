package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.AddItemToCartRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemDecrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemIncrementRequest;
import com.finartz.intern.campaignlogic.model.request.CartItemRemoveRequest;
import com.finartz.intern.campaignlogic.model.response.CartResponse;

public interface CartService {
  CartResponse addToCart(int accountId, String cartId, AddItemToCartRequest request);

  CartResponse removeFromCart(int accountId, String cartId, CartItemRemoveRequest request);

  CartResponse incrementItem(int accountId, String cartId, CartItemIncrementRequest request);

  CartResponse decrementItem(int accountId, String cartId, CartItemDecrementRequest request);

  String createCart(int accountId);

  CartResponse getCart(String cartIdFromHeader);
}

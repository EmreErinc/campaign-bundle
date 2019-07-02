package com.finartz.intern.campaignlogic.service;

public interface CartService {
  boolean addToCart(String userId, String itemId, String count);

  boolean removeFromCart(String itemId, String cartId);

  boolean incrementItem(String userId, String itemId);

  boolean decrementItem(String userId, String itemId);

  boolean createCart(String userId);


}

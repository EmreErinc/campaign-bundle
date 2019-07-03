package com.finartz.intern.campaignlogic.service;

public interface CartService {
  boolean addToCart(String accountId, String itemId, String count);

  boolean removeFromCart(String itemId, String cartId);

  boolean incrementItem(String accountId, String itemId);

  boolean decrementItem(String accountId, String itemId);

  boolean createCart(String accountId);
}

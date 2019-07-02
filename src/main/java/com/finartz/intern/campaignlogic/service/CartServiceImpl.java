package com.finartz.intern.campaignlogic.service;

import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {
  @Override
  public boolean addToCart(String userId, String itemId, String count) {
    return false;
  }

  @Override
  public boolean removeFromCart(String itemId, String cartId) {
    return false;
  }

  @Override
  public boolean incrementItem(String userId, String itemId) {
    return false;
  }

  @Override
  public boolean decrementItem(String userId, String itemId) {
    return false;
  }

  @Override
  public boolean createCart(String userId) {
    return false;
  }
}

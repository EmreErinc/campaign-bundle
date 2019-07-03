package com.finartz.intern.campaignlogic.service;

import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {
  @Override
  public boolean addToCart(String accountId, String itemId, String count) {
    return false;
  }

  @Override
  public boolean removeFromCart(String itemId, String cartId) {
    return false;
  }

  @Override
  public boolean incrementItem(String accountId, String itemId) {
    return false;
  }

  @Override
  public boolean decrementItem(String accountId, String itemId) {
    return false;
  }

  @Override
  public boolean createCart(String accountId) {
    return false;
  }
}

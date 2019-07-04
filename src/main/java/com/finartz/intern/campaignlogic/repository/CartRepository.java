package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository{
  CartEntity findCart(int cartId);

  boolean createCart();

  CartEntity updateCart(CartEntity cartEntity);
}

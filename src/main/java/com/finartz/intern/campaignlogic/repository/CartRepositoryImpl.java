package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.entity.SoldCartEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CartRepositoryImpl implements CartRepository {
  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public Optional<CartEntity> findCart(String cartId) {
    return Optional.ofNullable(mongoTemplate.findById(cartId, CartEntity.class));
  }

  @Override
  public CartEntity createCart(int accountId) {
    return mongoTemplate
        .save(CartEntity.builder()
            .accountId(accountId)
            .cartItems(Collections.emptyList())
            .build());
  }

  @Override
  public CartEntity updateCart(CartEntity cartEntity) {
    Query query = new Query();
    query.addCriteria(Criteria.where("_id").is(cartEntity.getId()));

    Update update = new Update();
    update.set("cartItems", cartEntity.getCartItems());

    mongoTemplate.updateFirst(query, update, CartEntity.class);
    return mongoTemplate.findById(cartEntity.getId(), CartEntity.class);
  }

  @Override
  public Optional<CartEntity> findByAccountId(int accountId) {
    Query query = new Query();
    query.addCriteria(Criteria.where("accountId").is(accountId));

    return Optional.ofNullable(mongoTemplate.findOne(query, CartEntity.class));
  }

  @Override
  public SoldCartEntity saveAsSold(SoldCartEntity soldCartEntity) {
    return mongoTemplate.save(soldCartEntity);
  }
}

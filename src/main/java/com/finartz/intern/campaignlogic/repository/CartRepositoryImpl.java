package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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
    return mongoTemplate.save(CartEntity.builder().accountId(accountId).build());
  }

  @Override
  public CartEntity updateCart(CartEntity cartEntity) {
    Query query = new Query();
    query.addCriteria(Criteria.where("_id").is(cartEntity.getId()));

    Update update = new Update();
    update.set("itemList", cartEntity.getItemList());

    mongoTemplate.updateFirst(query, update, CartEntity.class);
    return findCart(cartEntity.getId()).get();
  }
}

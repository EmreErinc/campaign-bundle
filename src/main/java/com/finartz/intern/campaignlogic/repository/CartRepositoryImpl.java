package com.finartz.intern.campaignlogic.repository;

import com.finartz.intern.campaignlogic.model.entity.CartEntity;
import com.finartz.intern.campaignlogic.model.value.CartItem;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class CartRepositoryImpl implements CartRepository {
  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public CartEntity getCart(String cartId) {
    return mongoTemplate.findById(cartId, CartEntity.class);
  }

  @Override
  public boolean createCart() {
    CartEntity cartEntity = mongoTemplate.save(CartEntity.builder().build());
    return cartEntity != null;
  }

  @Override
  public CartEntity updateCart(CartEntity cartEntity) {
    Query query = new Query();
    query.addCriteria(Criteria.where("_id").is(cartEntity.getCartId()));

    Update update = new Update();
    update.set("itemList", cartEntity.getItemList());

    mongoTemplate.updateFirst(query, update, CartEntity.class);
    return getCart(cartEntity.getCartId());
  }
}

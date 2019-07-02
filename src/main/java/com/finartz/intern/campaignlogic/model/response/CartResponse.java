package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.CartItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse {
  private String id;
  private List<CartItem> itemList;
}

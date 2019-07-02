package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.CartItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse<T extends CartItem> {
  private String id;
  private List<T> itemList;
}

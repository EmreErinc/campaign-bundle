package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.CartItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse<T extends CartItem> {
  private List<T> itemList;
}

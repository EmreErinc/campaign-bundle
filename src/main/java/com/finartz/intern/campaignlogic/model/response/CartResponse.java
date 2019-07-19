package com.finartz.intern.campaignlogic.model.response;

import com.finartz.intern.campaignlogic.model.value.CartItemDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse<T extends CartItemDto> {
  private List<T> itemList;
}

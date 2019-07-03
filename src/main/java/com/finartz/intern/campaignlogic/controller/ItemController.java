package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.model.response.ItemResponse;
import com.finartz.intern.campaignlogic.model.value.ItemDetail;
import com.finartz.intern.campaignlogic.model.value.ItemSummary;
import com.finartz.intern.campaignlogic.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/item")
public class ItemController extends BaseController {
  private final ItemService itemService;

  @Autowired
  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping
  public ItemResponse addItem(@RequestHeader HttpHeaders headers, @RequestBody AddItemRequest request) {
    return itemService.addItem(getAccountIdFromHeader(headers), request);
  }

  @GetMapping("/{id}")
  public ItemDetail getItem(@RequestHeader HttpHeaders headers, @PathVariable String id) {
    return itemService.getItem(Optional.ofNullable(getAccountIdFromHeader(headers)), id);
  }

  //@GetMapping("/")
  //public List<ItemSummary> getItemList(@RequestHeader HttpHeaders headers, @RequestParam(value = "search", required = false) String text) {
  //  return itemService.getItemList(Optional.ofNullable(getAccountIdFromHeader(headers)), Optional.ofNullable(text));
  //}

  @GetMapping("/seller/{sellerId}")
  public List<ItemSummary> getSellerItems(@RequestHeader HttpHeaders headers, @PathVariable String sellerId) {
    return itemService.getSellerItems(Optional.ofNullable(getAccountIdFromHeader(headers)), sellerId);
  }
}

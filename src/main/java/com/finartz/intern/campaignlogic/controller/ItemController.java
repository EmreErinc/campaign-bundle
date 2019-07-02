package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.AddItemRequest;
import com.finartz.intern.campaignlogic.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item")
public class ItemController extends BaseController {
  private final ItemService itemService;

  @Autowired
  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping
  public boolean addItem(@RequestHeader HttpHeaders headers, @RequestBody AddItemRequest request){
    return false;
  }

  @GetMapping("/{id}")
  public boolean getItem(@RequestHeader HttpHeaders headers, @PathVariable String id){
    return false;
  }

  @GetMapping("/{text}")
  public boolean getItemList(@RequestHeader HttpHeaders headers, @PathVariable String text){
    return false;
  }
}

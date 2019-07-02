package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.model.request.LoginRequest;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.LoginResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController extends BaseController {
  private AccountService accountService;

  @Autowired
  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping("/register")
  public RegisterResponse register(@RequestBody RegisterRequest request) {
    return accountService.addUser(request);
  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    return accountService.loginUser(request);
  }
}

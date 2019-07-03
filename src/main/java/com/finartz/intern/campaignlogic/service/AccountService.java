package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.model.request.LoginRequest;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.LoginResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
  RegisterResponse addUser(RegisterRequest request);
  LoginResponse loginUser(LoginRequest request);
  RegisterResponse addSellerAccount(RegisterRequest request);
}

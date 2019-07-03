package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static com.finartz.intern.campaignlogic.security.SecurityConstants.TOKEN_PREFIX;

public abstract class BaseController{
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  public String getAccountIdFromHeader(HttpHeaders headers) {
    return jwtTokenProvider
        .getIdFromToken(headers.get("Authorization")
            .get(0)
            .replace(TOKEN_PREFIX, ""));
  }

  public String getCartIdFromHeader(HttpHeaders headers){
    return jwtTokenProvider
        .getCartIdFromToken(headers.get("Authorization")
            .get(0)
            .replace(TOKEN_PREFIX, ""));
  }
}
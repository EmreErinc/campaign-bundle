package com.finartz.intern.campaignlogic.controller;

import com.finartz.intern.campaignlogic.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;

import static com.finartz.intern.campaignlogic.security.SecurityConstants.HEADER_STRING;
import static com.finartz.intern.campaignlogic.security.SecurityConstants.TOKEN_PREFIX;

@CrossOrigin()
public abstract class BaseController {
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  public Optional<Integer> getAccountIdFromHeader(HttpHeaders headers) {
    if (headers.containsKey(HEADER_STRING)) {
      return Optional.of(Integer.valueOf(jwtTokenProvider
          .getIdFromToken(headers.get(HEADER_STRING)
              .get(0)
              .replace(TOKEN_PREFIX, ""))));
    }
    return Optional.empty();
  }

  public String getCartIdFromHeader(HttpHeaders headers) {
    return jwtTokenProvider
        .getCartIdFromToken(headers.get(HEADER_STRING)
            .get(0)
            .replace(TOKEN_PREFIX, ""));
  }
}
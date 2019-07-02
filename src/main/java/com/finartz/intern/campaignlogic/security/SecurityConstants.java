package com.finartz.intern.campaignlogic.security;

public class SecurityConstants {
  public static final String SIGN_UP_URL = "/v1/register";
  public static final String SIGN_IN_URL = "/v1/login";
  public static final String SECRET = "Y9QLwCU9rsuVDdYHJDvg";
  public static final long EXPIRATION_TIME = 60 * 60 * 24 * 7 * 1000;
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String AUTHORITIES = "Scopes";
  public static final String USER_ID = "id";
  public static final String CART_ID = "cartId";

}
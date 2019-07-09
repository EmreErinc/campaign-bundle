package com.finartz.intern.campaignlogic.security;

public class SecurityConstants {
  public static final String[] WHITE_LIST = {"/register",
      "/seller/register",
      "/login",
      "/item",
      "/item/{itemId}",
      "/item/seller/{sellerId}",
      "/seller/{sellerId}"};

  public static final String[] SWAGGER_URLS = {"/v2/api-docs",
      "/configuration/ui",
      "/swagger-resources",
      "/configuration/security",
      "/swagger-ui.html",
      "/webjars/**",
      "/swagge‌​r-ui.html"};

  public static final String SECRET = "1EyF7VvkbafufWntanQo";
  public static final String SECRET_KEY = "KtsgWPBIC7FSkHKXbP3A";
  public static final String SALT_KEY = "br79RcYwdOJw7b3zIrQW";
  public static final long EXPIRATION_TIME = 60 * 60 * 24 * 7 * 1000;
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String AUTHORITIES = "Scopes";
  public static final String USER_ID = "id";
  public static final String CART_ID = "cartId";

}
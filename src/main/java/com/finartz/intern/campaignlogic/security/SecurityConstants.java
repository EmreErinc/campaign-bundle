package com.finartz.intern.campaignlogic.security;

public class SecurityConstants {
  private SecurityConstants() {
  }

  protected static final String[] WHITE_LIST = {"/register",
      "/seller/register",
      "/login",
      "/item",
      "/item/{itemId}",
      "/item/seller/{sellerId}",
      "/seller/{sellerId}"};

  protected static final String[] SWAGGER_URLS = {"/v2/api-docs",
      "/configuration/ui",
      "/configuration/security",
      "/swagger-resources",
      "/swagger-resources/**",
      "/swagger-ui.html",
      "/webjars/**"};

  public static final String SECRET = "1EyF7VvkbafufWntanQo";
  public static final String SECRET_KEY = "KtsgWPBIC7FSkHKXbP3A";
  public static final String SALT_KEY = "br79RcYwdOJw7b3zIrQW";
  public static final int EXPIRATION_TIME = 60 * 60 * 24 * 7 * 1000;
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String AUTHORITIES = "Scopes";
  public static final String USER_ID = "itemId";
  public static final String CART_ID = "cartId";

}
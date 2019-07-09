package com.finartz.intern.campaignlogic.security;

public class SecurityConstants {
  public static final String SIGN_UP_URL = "/register";
  public static final String SIGN_IN_URL = "/login";
  public static final String SELLER_SIGN_UP = "/seller/register";
  public static final String ITEM_LIST = "/item";
  public static final String ITEM_DETAIL = "/item/{itemId}";
  public static final String SELLER_ITEMS = "/item/seller/{sellerId}";
  public static final String SELLER_DETAIL = "/seller/{sellerId}";
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
package com.finartz.intern.campaignlogic.security;

import lombok.Data;

@Data
public class Errors {
  public final static String ACCOUNT_NOT_FOUND = "Hesap Bulunamadı.";
  public final static String ACCOUNT_ALREADY_EXISTS = "Hesap Zaten Kayıtlı.";
  public final static String SELLER_NOT_FOUND = "Satıcı Bulunamadı";
  public final static String SELLER_ALREADY_EXISTS = "Satıcı Zaten Kayıtlı";
  public final static String ITEM_NOT_FOUND = "Ürün Bulunamadı.";
  public final static String CAMPAIGN_NOT_FOUND = "Kampanya Bulunamadı.";
  public final static String CAMPAIGN_BADGE_NOT_FOUND = "Kampanya Rozet Bilgisi Bulunamadı.";
  public final static String CART_NOT_FOUND = "Sepet Bulunamadı.";
}

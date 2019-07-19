package com.finartz.intern.campaignlogic.model.value;

public enum Messages {
  EMPTY(0,""),
  CART_LIMIT_EXCEED(1, "Sepet limitini geçtiğiniz için hediye ürün sepetinize eklenemedi."),
  CAMPAIGN_LIMIT_EXCEED(2, "Kampanya limitini doldurduğunuz için hediye ürün sepetinize eklenemedi."),
  CART_UPDATED(3, "Stokta yeterli ürün kalmadığı için sepetiniz güncellenmiştir.");

  private final Integer key;
  private final String value;

  Messages(Integer key, String value) {
    this.key = key;
    this.value = value;
  }

  public Integer getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}

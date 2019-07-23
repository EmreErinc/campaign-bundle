package com.finartz.intern.campaignlogic.model.value;

public enum Messages {
  EMPTY(0,""),
  CART_LIMIT_EXCEED(1, "Sepet limitini geçtiğiniz için hediye ürün sepetinize eklenemedi."),
  CAMPAIGN_LIMIT_EXCEED(2, "Kampanya limitini doldurduğunuz için hediye ürün sepetinize eklenemedi."),
  CART_UPDATED(3, "Stokta yeterli ürün kalmadığı için sepetiniz güncellenmiştir."),

  CART_AVAILABLE(100, "Sepet satış için yeterli."),
  PRODUCT_STOCK_NOT_AVAILABLE(101, "Ürün stoğu yetersiz olduğu için satış iptal edildi."),
  PRODUCT_STOCK_VARIANT_NOT_AVAILABLE(102, "Ürün seçenek stoğu yetersiz olduğu için satış iptal edildi."),
  PRODUCT_STOCK_INCREASE(103,"Ürün stoğunda artış olduğu için sepetiniz güncellendi."),
  PRODUCT_STOCK_VARIANT_INCREASE(104, "Ürün seçenek stoğunda artış olduğu için sepet güncellendi."),

  ONE_OR_MORE_PRODUCT_ITEM_UNFIT(200, "Bir veya daha fazla ürünle ilgili bir sorun oluştu."),

  ITEM_NOT_FOUND_ON_CART(300, "Ürün sepette bulunamadı.");

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

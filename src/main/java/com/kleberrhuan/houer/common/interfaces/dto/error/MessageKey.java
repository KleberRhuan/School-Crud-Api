/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.error;

public record MessageKey(String base) {
  public String detailCode() {
    return base + ".detail";
  }

  public String messageCode() {
    return base + ".message";
  }

  public static MessageKey of(String base) {
    return new MessageKey(base);
  }
}

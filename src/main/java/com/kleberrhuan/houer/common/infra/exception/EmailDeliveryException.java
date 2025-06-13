package com.kleberrhuan.houer.common.infra.exception;

public class EmailDeliveryException extends RuntimeException {
  public EmailDeliveryException(String message) {
    super(message);
  }
}

package com.kleberrhuan.houer.csv.infra.exception;

public class RabbitMqException extends RuntimeException {
  public RabbitMqException(String message) {
    super(message);
  }
}

package com.kleberrhuan.houer.common.infra.exception;

public class OutboxPersistenceException extends RuntimeException {
  public OutboxPersistenceException(String message) {
    super(message);
  }
}

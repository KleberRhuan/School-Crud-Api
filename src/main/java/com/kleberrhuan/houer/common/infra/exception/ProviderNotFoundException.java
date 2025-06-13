package com.kleberrhuan.houer.common.infra.exception;

public class ProviderNotFoundException extends RuntimeException {
  public ProviderNotFoundException(String message) {
    super(message);
  }
}

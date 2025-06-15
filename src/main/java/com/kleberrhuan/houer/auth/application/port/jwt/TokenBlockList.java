/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.port.jwt;

public interface TokenBlockList {
  void block(String jti);

  boolean isBlocked(String jti);
}

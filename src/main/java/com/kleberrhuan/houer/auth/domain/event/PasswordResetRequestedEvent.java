/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.event;

public record PasswordResetRequestedEvent(
  String email,
  String name,
  String rawToken
) {}

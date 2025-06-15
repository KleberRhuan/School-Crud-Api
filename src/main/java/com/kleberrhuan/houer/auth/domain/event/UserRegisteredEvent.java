/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.event;

public record UserRegisteredEvent(
  String email,
  String name,
  String verifyLink
) {}

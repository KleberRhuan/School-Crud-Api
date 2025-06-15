/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.model;

import lombok.Getter;

@Getter
public enum Role {
  ADMIN("ADMIN"),
  CLIENT("CLIENT");

  private final String name;

  Role(String name) {
    this.name = name;
  }
}

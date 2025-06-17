/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class HashService {

  private final SecureRandom secureRandom;
  private final MessageDigest sha256;

  public HashService() throws NoSuchAlgorithmException {
    this.secureRandom = SecureRandom.getInstanceStrong();
    this.sha256 = MessageDigest.getInstance("SHA-256");
  }

  public String generateToken() {
    byte[] bytes = new byte[16];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  @SneakyThrows
  public String hash(String rawToken) {
    byte[] hash = sha256.digest(rawToken.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
  }
}

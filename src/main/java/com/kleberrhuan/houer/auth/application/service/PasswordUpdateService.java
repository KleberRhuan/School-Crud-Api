/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordUpdateService {

  private final PasswordEncoder passwordEncoder;

  public void updateUserPassword(User user, String newPassword) {
    String encodedPassword = passwordEncoder.encode(newPassword);
    user.setPasswordHash(encodedPassword);

    log.info("Password successfully reset for user ID: {}", user.getId());
  }
}

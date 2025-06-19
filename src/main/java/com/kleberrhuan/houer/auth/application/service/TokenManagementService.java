/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.application.port.jwt.TokenBlockList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManagementService {

  private final TokenBlockList tokenBlockList;

  public void blockToken(String jti) {
    tokenBlockList.block(jti);
    log.info("Logout executed - jti={}", jti);
  }
}

/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.application.factory.TokenFactory;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.RefreshToken;
import com.kleberrhuan.houer.auth.domain.service.CredentialValidationService;
import com.kleberrhuan.houer.auth.domain.service.RefreshTokenDomainService;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.user.application.mapper.UserMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import com.kleberrhuan.houer.user.interfaces.dto.response.UserResponse;
import io.micrometer.core.annotation.Counted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final CredentialValidationService credentialValidationService;
  private final RefreshTokenDomainService refreshTokenDomainService;
  private final TokenFactory tokenFactory;
  private final TokenManagementService tokenManagementService;

  @Counted(value = "auth.login.ok")
  @Transactional
  public TokenPair login(LoginRequest request) {
    User user = findUserByEmail(request.email());
    credentialValidationService.validateCredentials(user, request.password());
    credentialValidationService.validateUserEnabled(user);

    TokenPair tokenPair = tokenFactory.createTokenPair(
      user,
      request.rememberMe()
    );
    log.info("User {} logged in successfully", user.getId());

    return tokenPair;
  }

  @Counted(value = "auth.refresh.ok")
  @Transactional
  public TokenPair refresh(String refreshJwt) {
    RefreshToken refreshToken =
      refreshTokenDomainService.findAndValidateRefreshToken(refreshJwt);
    refreshTokenDomainService.markTokenAsUsed(refreshToken);

    User user = userRepository.getReferenceById(refreshToken.getUserId());
    TokenPair tokenPair = tokenFactory.createTokenPair(user, true);

    log.info("Token refreshed for user {}", user.getId());
    return tokenPair;
  }

  @Counted(value = "auth.logout")
  public void logout(String jti) {
    tokenManagementService.blockToken(jti);
  }

  public UserResponse me(Long sub) {
    User user = userRepository
      .findById(sub)
      .orElseThrow(() -> new EntityNotFoundException("Usuário", sub));

    return userMapper.map(user);
  }

  private User findUserByEmail(String email) {
    return userRepository
      .findByEmailIgnoreCaseAll(email)
      .orElseThrow(AuthException::badCredentials);
  }
}

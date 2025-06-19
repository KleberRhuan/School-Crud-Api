/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.application.factory.TokenFactory;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.service.CredentialValidationService;
import com.kleberrhuan.houer.auth.domain.service.RefreshTokenDomainService;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.user.application.mapper.UserMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private CredentialValidationService credentialValidationService;

  @Mock
  private RefreshTokenDomainService refreshTokenDomainService;

  @Mock
  private TokenFactory tokenFactory;

  @Mock
  private TokenManagementService tokenManagementService;

  @Mock
  private UserMapper userMapper;

  private AuthenticationService service;

  @BeforeEach
  void setup() {
    service =
      new AuthenticationService(
        userRepository,
        userMapper,
        credentialValidationService,
        refreshTokenDomainService,
        tokenFactory,
        tokenManagementService
      );
  }

  private User dummyUser(boolean enabled) {
    User u = new User();
    u.setId(1L);
    u.setEmail("foo@bar.com");
    u.setPasswordHash("hashed");
    u.setEnabled(enabled);
    return u;
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName(
      "deve retornar TokenPair quando credenciais forem válidas e usuário estiver habilitado"
    )
    void givenValidCredentials_whenLogin_thenReturnTokenPair() {
      User u = dummyUser(true);
      TokenPair expectedTokenPair = new TokenPair(
        "access-token",
        Optional.empty(),
        900L
      );

      when(userRepository.findByEmailIgnoreCaseAll("foo@bar.com"))
        .thenReturn(Optional.of(u));
      when(tokenFactory.createTokenPair(u, false))
        .thenReturn(expectedTokenPair);

      TokenPair pair = service.login(
        new LoginRequest("foo@bar.com", "123456", false)
      );

      assertThat(pair).isNotNull();
      assertThat(pair.access()).isEqualTo("access-token");
      assertThat(pair.refresh()).isEmpty();
      assertThat(pair.ttlSec()).isEqualTo(900L);

      verify(credentialValidationService).validateCredentials(u, "123456");
      verify(credentialValidationService).validateUserEnabled(u);
      verify(tokenFactory).createTokenPair(u, false);
    }

    @Test
    @DisplayName(
      "deve lançar AuthException.badCredentials quando senha for incorreta"
    )
    void givenInvalidPassword_whenLogin_thenThrow() {
      User u = dummyUser(true);
      when(userRepository.findByEmailIgnoreCaseAll("foo@bar.com"))
        .thenReturn(Optional.of(u));
      doThrow(AuthException.badCredentials())
        .when(credentialValidationService)
        .validateCredentials(u, "bad");

      assertThatThrownBy(() ->
          service.login(new LoginRequest("foo@bar.com", "bad", false))
        )
        .isInstanceOf(AuthException.class);

      verify(credentialValidationService).validateCredentials(u, "bad");
      verifyNoInteractions(tokenFactory);
    }

    @Test
    @DisplayName(
      "deve lançar AuthException.accountNotVerified quando usuário não estiver habilitado"
    )
    void givenDisabledUser_whenLogin_thenThrow() {
      User u = dummyUser(false);
      when(userRepository.findByEmailIgnoreCaseAll("foo@bar.com"))
        .thenReturn(Optional.of(u));
      doThrow(AuthException.accountNotVerified())
        .when(credentialValidationService)
        .validateUserEnabled(u);

      assertThatThrownBy(() ->
          service.login(new LoginRequest("foo@bar.com", "123456", false))
        )
        .isInstanceOf(AuthException.class);

      verify(credentialValidationService).validateCredentials(u, "123456");
      verify(credentialValidationService).validateUserEnabled(u);
      verifyNoInteractions(tokenFactory);
    }
  }
}

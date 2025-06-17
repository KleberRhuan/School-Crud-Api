/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.application.port.jwt.TokenBlockList;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtParser;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtTokenProvider;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock
  private UserRepository users;

  @Mock
  private PasswordEncoder encoder;

  @Mock
  private JwtTokenProvider jwt;

  @Mock
  private TokenBlockList blockList;

  @Mock
  private JwtParser parser;

  @Mock
  private com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository refreshTokens;

  @InjectMocks
  private AuthenticationService service;

  private JwtProps props;

  @BeforeEach
  void setup() throws Exception {
    // Gera chaves RSA fake apenas para preencher JwtProps
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    props =
      new JwtProps(
        "testIssuer",
        900L,
        86_400L,
        (RSAPublicKey) kp.getPublic(),
        (RSAPrivateKey) kp.getPrivate()
      );

    // substitui o campo final via reflexão (injecção manual, pois props é final)
    java.lang.reflect.Field f =
      AuthenticationService.class.getDeclaredField("props");
    f.setAccessible(true);
    f.set(service, props);
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
      when(users.findByEmailIgnoreCase("foo@bar.com"))
        .thenReturn(Optional.of(u));
      when(encoder.matches("123456", "hashed")).thenReturn(true);
      when(jwt.accessToken(eq(u), anyString())).thenReturn("access-token");

      TokenPair pair = service.login(
        new LoginRequest("foo@bar.com", "123456", false)
      );

      assertThat(pair).isNotNull();
      assertThat(pair.access()).isEqualTo("access-token");
      assertThat(pair.refresh()).isEmpty();
      assertThat(pair.ttlSec()).isEqualTo(props.accessTtlSec());
    }

    @Test
    @DisplayName(
      "deve lançar AuthException.badCredentials quando senha for incorreta"
    )
    void givenInvalidPassword_whenLogin_thenThrow() {
      User u = dummyUser(true);
      when(users.findByEmailIgnoreCase("foo@bar.com"))
        .thenReturn(Optional.of(u));
      when(encoder.matches("bad", "hashed")).thenReturn(false);

      assertThatThrownBy(() ->
          service.login(new LoginRequest("foo@bar.com", "bad", false))
        )
        .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName(
      "deve lançar AuthException.accountNotVerified quando usuário não estiver habilitado"
    )
    void givenDisabledUser_whenLogin_thenThrow() {
      User u = dummyUser(false);
      when(users.findByEmailIgnoreCase("foo@bar.com"))
        .thenReturn(Optional.of(u));
      when(encoder.matches(anyString(), anyString())).thenReturn(true);

      assertThatThrownBy(() ->
          service.login(new LoginRequest("foo@bar.com", "123456", false))
        )
        .isInstanceOf(AuthException.class);
    }
  }
}

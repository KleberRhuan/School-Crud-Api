/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.application.port.jwt.TokenBlockList;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.RefreshToken;
import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtParser;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtTokenProvider;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService - Refresh and Logout Operations")
class AuthenticationServiceRefreshLogoutTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private JwtParser jwtParser;

  @Mock
  private TokenBlockList tokenBlockList;

  private AuthenticationService authService;
  private JwtProps jwtProps;
  private User testUser;
  private RefreshToken testRefreshToken;

  @BeforeEach
  void setup() throws Exception {
    // Gera chaves RSA fake apenas para preencher JwtProps
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    jwtProps =
      new JwtProps(
        "testIssuer",
        900L,
        86_400L,
        (RSAPublicKey) kp.getPublic(),
        (RSAPrivateKey) kp.getPrivate()
      );

    authService =
      new AuthenticationService(
        userRepository,
        refreshTokenRepository,
        passwordEncoder,
        jwtTokenProvider,
        tokenBlockList,
        jwtProps,
        jwtParser
      );

    testUser = new User();
    testUser.setId(123L);
    testUser.setEmail("test@example.com");

    testRefreshToken =
      RefreshToken
        .builder()
        .series(UUID.randomUUID())
        .userId(testUser.getId())
        .expiresAt(Instant.now().plusSeconds(604800)) // 7 days
        .used(false)
        .build();
  }

  @Nested
  @DisplayName("refresh")
  class Refresh {

    @Test
    @DisplayName("deve gerar novos tokens para refresh token válido")
    void givenValidRefreshToken_whenRefresh_thenReturnNewTokens() {
      // Arrange
      String refreshJwt = "valid.jwt.format";
      String newAccessToken = "new.access.token";
      String newRefreshToken = "new.refresh.token";

      Jwt parsedJwt = createMockJwt(testRefreshToken.getSeries().toString());

      when(jwtParser.parse(refreshJwt)).thenReturn(parsedJwt);
      when(refreshTokenRepository.findById(testRefreshToken.getSeries()))
        .thenReturn(Optional.of(testRefreshToken));
      when(userRepository.getReferenceById(testUser.getId()))
        .thenReturn(testUser);
      when(jwtTokenProvider.accessToken(eq(testUser), any(String.class)))
        .thenReturn(newAccessToken);
      when(jwtTokenProvider.refreshToken(eq(testUser), any(UUID.class)))
        .thenReturn(newRefreshToken);

      // Act
      TokenPair result = authService.refresh(refreshJwt);

      // Assert
      assertThat(result.access()).isEqualTo(newAccessToken);
      assertThat(result.refresh()).hasValue(newRefreshToken);

      verify(jwtParser).parse(refreshJwt);
      verify(refreshTokenRepository).findById(testRefreshToken.getSeries());
      verify(userRepository).getReferenceById(testUser.getId());
      verify(jwtTokenProvider).accessToken(eq(testUser), any(String.class));
      verify(jwtTokenProvider).refreshToken(eq(testUser), any(UUID.class));

      // Verify that old refresh token is marked as used
      verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
      verify(refreshTokenRepository)
        .save(argThat(savedToken -> savedToken.isUsed()));
    }

    @Test
    @DisplayName("deve lançar AuthException para refresh token inexistente")
    void givenNonExistentRefreshToken_whenRefresh_thenThrowAuthException() {
      // Arrange
      String refreshJwt = "valid.jwt.format";
      UUID nonExistentSeries = UUID.randomUUID();

      Jwt parsedJwt = createMockJwt(nonExistentSeries.toString());
      when(jwtParser.parse(refreshJwt)).thenReturn(parsedJwt);
      when(refreshTokenRepository.findById(nonExistentSeries))
        .thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(refreshJwt))
        .isInstanceOf(AuthException.class);

      verify(refreshTokenRepository).findById(nonExistentSeries);
      verifyNoInteractions(userRepository, jwtTokenProvider);
    }

    @Test
    @DisplayName("deve lançar AuthException para refresh token já usado")
    void givenUsedRefreshToken_whenRefresh_thenThrowAuthException() {
      // Arrange
      String refreshJwt = "valid.jwt.format";
      RefreshToken usedToken = RefreshToken
        .builder()
        .series(UUID.randomUUID())
        .userId(testUser.getId())
        .expiresAt(Instant.now().plusSeconds(604800))
        .used(true) // Mark as already used
        .build();

      Jwt parsedJwt = createMockJwt(usedToken.getSeries().toString());
      when(jwtParser.parse(refreshJwt)).thenReturn(parsedJwt);
      when(refreshTokenRepository.findById(usedToken.getSeries()))
        .thenReturn(Optional.of(usedToken));

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(refreshJwt))
        .isInstanceOf(AuthException.class);

      verify(refreshTokenRepository).findById(usedToken.getSeries());
      verifyNoInteractions(userRepository, jwtTokenProvider);
    }

    @Test
    @DisplayName("deve lançar AuthException para refresh token expirado")
    void givenExpiredRefreshToken_whenRefresh_thenThrowAuthException() {
      // Arrange
      String refreshJwt = "valid.jwt.format";
      RefreshToken expiredToken = RefreshToken
        .builder()
        .series(UUID.randomUUID())
        .userId(testUser.getId())
        .expiresAt(Instant.now().minusSeconds(3600)) // Expired 1 hour ago
        .used(false)
        .build();

      Jwt parsedJwt = createMockJwt(expiredToken.getSeries().toString());
      when(jwtParser.parse(refreshJwt)).thenReturn(parsedJwt);
      when(refreshTokenRepository.findById(expiredToken.getSeries()))
        .thenReturn(Optional.of(expiredToken));

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(refreshJwt))
        .isInstanceOf(AuthException.class);

      verify(refreshTokenRepository).findById(expiredToken.getSeries());
      verifyNoInteractions(userRepository, jwtTokenProvider);
    }

    @Test
    @DisplayName("deve lançar AuthException para JWT malformado")
    void givenMalformedJwt_whenRefresh_thenThrowAuthException() {
      // Arrange
      String malformedJwt = "malformed.jwt";

      when(jwtParser.parse(malformedJwt))
        .thenThrow(AuthException.malformedToken());

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(malformedJwt))
        .isInstanceOf(AuthException.class);

      verify(jwtParser).parse(malformedJwt);
      verifyNoInteractions(
        refreshTokenRepository,
        userRepository,
        jwtTokenProvider
      );
    }
  }

  @Nested
  @DisplayName("logout")
  class Logout {

    @Test
    @DisplayName("deve adicionar JTI válido à blocklist")
    void givenValidJti_whenLogout_thenAddToBlocklist() {
      // Arrange
      String jti = "valid-jti-123";

      // Act
      authService.logout(jti);

      // Assert
      verify(tokenBlockList).block(jti);
    }

    @Test
    @DisplayName("deve lidar graciosamente com JTI nulo")
    void givenNullJti_whenLogout_thenHandleGracefully() {
      // Act & Assert - Should not throw exception
      assertThatCode(() -> authService.logout(null)).doesNotThrowAnyException();

      verify(tokenBlockList).block(null);
    }

    @Test
    @DisplayName("deve lidar graciosamente com JTI vazio")
    void givenEmptyJti_whenLogout_thenHandleGracefully() {
      // Arrange
      String emptyJti = "";

      // Act & Assert - Should not throw exception
      assertThatCode(() -> authService.logout(emptyJti))
        .doesNotThrowAnyException();

      verify(tokenBlockList).block(emptyJti);
    }

    @Test
    @DisplayName("deve lidar graciosamente com logout múltiplo do mesmo JTI")
    void givenSameJti_whenLogoutMultipleTimes_thenHandleGracefully() {
      // Arrange
      String jti = "same-jti-123";

      // Act
      authService.logout(jti);
      authService.logout(jti);

      // Assert
      verify(tokenBlockList, times(2)).block(jti);
    }

    @Test
    @DisplayName("deve lidar independentemente com múltiplos JTIs")
    void givenMultipleJtis_whenLogout_thenHandleIndependently() {
      // Arrange
      String jti1 = "jti-1";
      String jti2 = "jti-2";
      String jti3 = "jti-3";

      // Act
      authService.logout(jti1);
      authService.logout(jti2);
      authService.logout(jti3);

      // Assert
      verify(tokenBlockList).block(jti1);
      verify(tokenBlockList).block(jti2);
      verify(tokenBlockList).block(jti3);
    }
  }

  private Jwt createMockJwt(String jti) {
    return Jwt
      .withTokenValue("token")
      .header("alg", "RS256")
      .claim("jti", jti)
      .subject("user@example.com")
      .claim("iat", Instant.now())
      .claim("exp", Instant.now().plusSeconds(3600))
      .build();
  }
}

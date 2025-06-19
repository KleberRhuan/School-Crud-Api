/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.application.factory.TokenFactory;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.RefreshToken;
import com.kleberrhuan.houer.auth.domain.service.CredentialValidationService;
import com.kleberrhuan.houer.auth.domain.service.RefreshTokenDomainService;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.user.application.mapper.UserMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService - Refresh and Logout Operations")
class AuthenticationServiceRefreshLogoutTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private CredentialValidationService credentialValidationService;

  @Mock
  private RefreshTokenDomainService refreshTokenDomainService;

  @Mock
  private TokenFactory tokenFactory;

  @Mock
  private UserMapper userMapper;

  @Mock
  private TokenManagementService tokenManagementService;

  private AuthenticationService authService;
  private User testUser;
  private RefreshToken testRefreshToken;

  @BeforeEach
  void setup() throws Exception {
    authService =
      new AuthenticationService(
        userRepository,
        userMapper,
        credentialValidationService,
        refreshTokenDomainService,
        tokenFactory,
        tokenManagementService
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
      TokenPair expectedTokenPair = new TokenPair(
        newAccessToken,
        Optional.of(newRefreshToken),
        900L
      );

      when(refreshTokenDomainService.findAndValidateRefreshToken(refreshJwt))
        .thenReturn(testRefreshToken);
      when(userRepository.getReferenceById(testUser.getId()))
        .thenReturn(testUser);
      when(tokenFactory.createTokenPair(testUser, true))
        .thenReturn(expectedTokenPair);

      // Act
      TokenPair result = authService.refresh(refreshJwt);

      // Assert
      assertThat(result.access()).isEqualTo(newAccessToken);
      assertThat(result.refresh()).hasValue(newRefreshToken);

      verify(refreshTokenDomainService).findAndValidateRefreshToken(refreshJwt);
      verify(refreshTokenDomainService).markTokenAsUsed(testRefreshToken);
      verify(userRepository).getReferenceById(testUser.getId());
      verify(tokenFactory).createTokenPair(testUser, true);
    }

    @Test
    @DisplayName("deve lançar AuthException para refresh token inexistente")
    void givenNonExistentRefreshToken_whenRefresh_thenThrowAuthException() {
      // Arrange
      String refreshJwt = "valid.jwt.format";

      when(refreshTokenDomainService.findAndValidateRefreshToken(refreshJwt))
        .thenThrow(AuthException.refreshNotFound());

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(refreshJwt))
        .isInstanceOf(AuthException.class);

      verify(refreshTokenDomainService).findAndValidateRefreshToken(refreshJwt);
      verifyNoInteractions(userRepository, tokenFactory);
      verify(refreshTokenDomainService, never()).markTokenAsUsed(any());
    }

    @Test
    @DisplayName("deve lançar AuthException para refresh token já usado")
    void givenUsedRefreshToken_whenRefresh_thenThrowAuthException() {
      // Arrange
      String refreshJwt = "valid.jwt.format";

      when(refreshTokenDomainService.findAndValidateRefreshToken(refreshJwt))
        .thenThrow(AuthException.refreshExpired());

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(refreshJwt))
        .isInstanceOf(AuthException.class);

      verify(refreshTokenDomainService).findAndValidateRefreshToken(refreshJwt);
      verifyNoInteractions(userRepository, tokenFactory);
      verify(refreshTokenDomainService, never()).markTokenAsUsed(any());
    }

    @Test
    @DisplayName("deve lançar AuthException para refresh token expirado")
    void givenExpiredRefreshToken_whenRefresh_thenThrowAuthException() {
      // Arrange
      String refreshJwt = "valid.jwt.format";

      when(refreshTokenDomainService.findAndValidateRefreshToken(refreshJwt))
        .thenThrow(AuthException.refreshExpired());

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(refreshJwt))
        .isInstanceOf(AuthException.class);

      verify(refreshTokenDomainService).findAndValidateRefreshToken(refreshJwt);
      verifyNoInteractions(userRepository, tokenFactory);
      verify(refreshTokenDomainService, never()).markTokenAsUsed(any());
    }

    @Test
    @DisplayName("deve lançar AuthException para JWT malformado")
    void givenMalformedJwt_whenRefresh_thenThrowAuthException() {
      // Arrange
      String malformedJwt = "malformed.jwt";

      when(refreshTokenDomainService.findAndValidateRefreshToken(malformedJwt))
        .thenThrow(AuthException.malformedToken());

      // Act & Assert
      assertThatThrownBy(() -> authService.refresh(malformedJwt))
        .isInstanceOf(AuthException.class);

      verify(refreshTokenDomainService)
        .findAndValidateRefreshToken(malformedJwt);
      verifyNoInteractions(userRepository, tokenFactory);
      verify(refreshTokenDomainService, never()).markTokenAsUsed(any());
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
      verify(tokenManagementService).blockToken(jti);
    }

    @Test
    @DisplayName("deve lidar graciosamente com JTI nulo")
    void givenNullJti_whenLogout_thenHandleGracefully() {
      // Act & Assert - Should not throw exception
      assertThatCode(() -> authService.logout(null)).doesNotThrowAnyException();

      verify(tokenManagementService).blockToken(null);
    }

    @Test
    @DisplayName("deve lidar graciosamente com JTI vazio")
    void givenEmptyJti_whenLogout_thenHandleGracefully() {
      // Arrange
      String emptyJti = "";

      // Act & Assert - Should not throw exception
      assertThatCode(() -> authService.logout(emptyJti))
        .doesNotThrowAnyException();

      verify(tokenManagementService).blockToken(emptyJti);
    }

    @Test
    @DisplayName("deve lidar graciosamente com logout múltiplo do mesmo JTI")
    void givenSameJti_whenLogoutMultipleTimes_thenHandleGracefully() {
      // Arrange
      String jti = "duplicate-jti";

      // Act
      authService.logout(jti);
      authService.logout(jti);

      // Assert
      verify(tokenManagementService, times(2)).blockToken(jti);
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
      verify(tokenManagementService).blockToken(jti1);
      verify(tokenManagementService).blockToken(jti2);
      verify(tokenManagementService).blockToken(jti3);
    }
  }
}

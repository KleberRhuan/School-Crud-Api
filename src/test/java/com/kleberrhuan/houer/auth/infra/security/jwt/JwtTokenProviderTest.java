/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.domain.model.Role;
import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import com.kleberrhuan.houer.user.domain.model.User;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

  @Mock
  private JwtEncoder encoder;

  @InjectMocks
  private JwtTokenProvider tokenProvider;

  private JwtProps props;
  private User testUser;

  @BeforeEach
  void setup() throws Exception {
    // Generate test RSA keys
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair keyPair = gen.generateKeyPair();

    props =
      new JwtProps(
        "test-issuer",
        900L, // 15 minutes access
        86400L, // 24 hours refresh
        (RSAPublicKey) keyPair.getPublic(),
        (RSAPrivateKey) keyPair.getPrivate()
      );

    // Inject props via reflection (final field)
    var field = JwtTokenProvider.class.getDeclaredField("props");
    field.setAccessible(true);
    field.set(tokenProvider, props);

    // Setup test user
    testUser = new User();
    testUser.setId(123L);
    testUser.setEmail("test@example.com");
    testUser.setRoles(Set.of(Role.CLIENT, Role.ADMIN));
  }

  @Nested
  @DisplayName("accessToken")
  class AccessToken {

    @Test
    @DisplayName("deve gerar access token com claims corretos")
    void givenValidUserAndJti_whenGenerateAccessToken_thenReturnTokenWithCorrectClaims() {
      // Arrange
      String jti = "test-jti-123";
      String expectedToken = "encoded.jwt.token";

      when(encoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(createMockJwt(expectedToken));

      // Act
      String result = tokenProvider.accessToken(testUser, jti);

      // Assert
      assertThat(result).isEqualTo(expectedToken);

      ArgumentCaptor<JwtEncoderParameters> paramsCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
      verify(encoder).encode(paramsCaptor.capture());

      JwtClaimsSet claims = paramsCaptor.getValue().getClaims();
      assertThat(claims.getId()).isEqualTo(jti);
      assertThat(claims.getSubject()).isEqualTo("123");
      assertThat((String) claims.getClaim("iss")).isEqualTo("test-issuer");
      assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(Instant.now());
      assertThat(claims.getExpiresAt()).isAfter(Instant.now());
      assertThat(claims.getExpiresAt())
        .isBeforeOrEqualTo(Instant.now().plusSeconds(props.accessTtlSec()));

      // Verify roles claim
      String rolesClaim = claims.getClaim("roles");
      assertThat(rolesClaim).contains("CLIENT", "ADMIN");
    }

    @Test
    @DisplayName("deve gerar token com TTL correto")
    void givenUser_whenGenerateAccessToken_thenTokenHasCorrectTTL() {
      // Arrange
      when(encoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(createMockJwt("token"));

      // Act
      tokenProvider.accessToken(testUser, "jti");

      // Assert
      ArgumentCaptor<JwtEncoderParameters> paramsCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
      verify(encoder).encode(paramsCaptor.capture());

      JwtClaimsSet claims = paramsCaptor.getValue().getClaims();
      long actualTtl =
        claims.getExpiresAt().getEpochSecond() -
        claims.getIssuedAt().getEpochSecond();

      assertThat(actualTtl).isEqualTo(props.accessTtlSec());
    }

    @Test
    @DisplayName("deve lidar com usuário sem roles")
    void givenUserWithoutRoles_whenGenerateAccessToken_thenEmptyRolesClaim() {
      // Arrange
      User userWithoutRoles = new User();
      userWithoutRoles.setId(456L);
      userWithoutRoles.setRoles(Set.of());

      when(encoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(createMockJwt("token"));

      // Act
      tokenProvider.accessToken(userWithoutRoles, "jti");

      // Assert
      ArgumentCaptor<JwtEncoderParameters> paramsCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
      verify(encoder).encode(paramsCaptor.capture());

      JwtClaimsSet claims = paramsCaptor.getValue().getClaims();
      String rolesClaim = claims.getClaim("roles");
      assertThat(rolesClaim).isEmpty();
    }

    @Test
    @DisplayName("deve lançar exceção quando JTI é nulo")
    void givenNullJti_whenGenerateAccessToken_thenThrowException() {
      // Arrange & Act & Assert
      assertThatThrownBy(() -> tokenProvider.accessToken(testUser, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("value cannot be null");
    }
  }

  @Nested
  @DisplayName("refreshToken")
  class RefreshToken {

    @Test
    @DisplayName("deve gerar refresh token com series UUID como ID")
    void givenValidUserAndSeries_whenGenerateRefreshToken_thenReturnTokenWithSeriesAsId() {
      // Arrange
      UUID series = UUID.randomUUID();
      String expectedToken = "refresh.jwt.token";

      when(encoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(createMockJwt(expectedToken));

      // Act
      String result = tokenProvider.refreshToken(testUser, series);

      // Assert
      assertThat(result).isEqualTo(expectedToken);

      ArgumentCaptor<JwtEncoderParameters> paramsCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
      verify(encoder).encode(paramsCaptor.capture());

      JwtClaimsSet claims = paramsCaptor.getValue().getClaims();
      assertThat(claims.getId()).isEqualTo(series.toString());
      assertThat(claims.getSubject()).isEqualTo("123");
    }

    @Test
    @DisplayName("deve gerar refresh token com TTL correto")
    void givenUser_whenGenerateRefreshToken_thenTokenHasCorrectTTL() {
      // Arrange
      UUID series = UUID.randomUUID();
      when(encoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(createMockJwt("token"));

      // Act
      tokenProvider.refreshToken(testUser, series);

      // Assert
      ArgumentCaptor<JwtEncoderParameters> paramsCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
      verify(encoder).encode(paramsCaptor.capture());

      JwtClaimsSet claims = paramsCaptor.getValue().getClaims();
      long actualTtl =
        claims.getExpiresAt().getEpochSecond() -
        claims.getIssuedAt().getEpochSecond();

      assertThat(actualTtl).isEqualTo(props.refreshTtlSec());
    }

    @Test
    @DisplayName("deve incluir roles no refresh token")
    void givenUserWithRoles_whenGenerateRefreshToken_thenTokenContainsRoles() {
      // Arrange
      UUID series = UUID.randomUUID();
      when(encoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(createMockJwt("token"));

      // Act
      tokenProvider.refreshToken(testUser, series);

      // Assert
      ArgumentCaptor<JwtEncoderParameters> paramsCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
      verify(encoder).encode(paramsCaptor.capture());

      JwtClaimsSet claims = paramsCaptor.getValue().getClaims();
      String rolesClaim = claims.getClaim("roles");
      assertThat(rolesClaim).contains("CLIENT", "ADMIN");
    }
  }

  private Jwt createMockJwt(String tokenValue) {
    return Jwt
      .withTokenValue(tokenValue)
      .header("alg", "RS256")
      .claim("sub", "123")
      .build();
  }
}

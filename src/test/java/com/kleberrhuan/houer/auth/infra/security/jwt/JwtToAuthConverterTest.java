/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import static org.assertj.core.api.Assertions.*;

import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@DisplayName("JwtToAuthConverter")
class JwtToAuthConverterTest {

  private JwtToAuthConverter converter;

  @BeforeEach
  void setup() {
    converter = new JwtToAuthConverter();
  }

  @Nested
  @DisplayName("convert")
  class Convert {

    @Test
    @DisplayName(
      "deve converter JWT com roles para Authentication com authorities corretas"
    )
    void givenJwtWithRoles_whenConvert_thenReturnAuthenticationWithRoleAuthorities() {
      // Arrange
      Jwt jwt = createJwtWithRolesList(List.of("ADMIN", "CLIENT"));

      // Act
      AbstractAuthenticationToken result = converter.convert(jwt);

      // Assert
      assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
      JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) result;
      assertThat(jwtAuth.getToken()).isEqualTo(jwt);
      assertThat(jwtAuth.getName()).isEqualTo("user123");

      Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
      assertThat(authorities).hasSize(2);
      assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CLIENT");
    }

    @Test
    @DisplayName("deve converter JWT com single role para Authentication")
    void givenJwtWithSingleRole_whenConvert_thenReturnAuthenticationWithSingleAuthority() {
      // Arrange
      Jwt jwt = createJwtWithRolesList(List.of("ADMIN"));

      // Act
      AbstractAuthenticationToken result = converter.convert(jwt);

      // Assert
      assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
      JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) result;
      Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
      assertThat(authorities).hasSize(1);
      assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("deve lidar com JWT sem roles claim")
    void givenJwtWithoutRolesClaim_whenConvert_thenReturnAuthenticationWithNoAuthorities() {
      // Arrange
      Jwt jwt = Jwt
        .withTokenValue("token")
        .header("alg", "RS256")
        .claim("sub", "user789")
        .claim("iat", Instant.now())
        .claim("exp", Instant.now().plusSeconds(300))
        .build();

      // Act
      AbstractAuthenticationToken result = converter.convert(jwt);

      // Assert
      assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
      JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) result;
      assertThat(jwtAuth.getName()).isEqualTo("user789");
      Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
      assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("deve lidar com roles claim vazio")
    void givenJwtWithEmptyRoles_whenConvert_thenReturnAuthenticationWithNoAuthorities() {
      // Arrange
      Jwt jwt = createJwtWithRolesList(List.of());

      // Act
      AbstractAuthenticationToken result = converter.convert(jwt);

      // Assert
      assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
      JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) result;
      Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
      assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("deve lidar com roles claim ausente")
    void givenJwtWithMissingRolesClaim_whenConvert_thenReturnAuthenticationWithNoAuthorities() {
      // Arrange - JWT sem roles claim para testar getClaimAsStringList retornando
      // null
      Jwt jwt = Jwt
        .withTokenValue("token")
        .header("alg", "RS256")
        .claim("sub", "user000")
        .claim("iat", Instant.now())
        .claim("exp", Instant.now().plusSeconds(300))
        .build();

      // Act
      AbstractAuthenticationToken result = converter.convert(jwt);

      // Assert
      assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
      JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) result;
      Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
      assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("deve converter roles em maiúsculo com prefixo ROLE_")
    void givenJwtWithLowercaseRoles_whenConvert_thenReturnAuthenticationWithUppercaseRoleAuthorities() {
      // Arrange
      Jwt jwt = createJwtWithRolesList(List.of("admin", "client"));

      // Act
      AbstractAuthenticationToken result = converter.convert(jwt);

      // Assert
      assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
      JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) result;
      Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
      assertThat(authorities).hasSize(2);
      assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CLIENT");
    }

    @Test
    @DisplayName("deve preservar todas as propriedades do JWT original")
    void givenJwtWithMultipleClaims_whenConvert_thenPreserveAllTokenProperties()
      throws Exception {
      // Arrange
      Instant issuedAt = Instant.now();
      Instant expiresAt = issuedAt.plusSeconds(900);

      Jwt jwt = Jwt
        .withTokenValue("detailed.jwt.token")
        .header("alg", "RS256")
        .header("typ", "JWT")
        .claim("sub", "user222")
        .claim("iss", new URL("https://test-issuer.com"))
        .claim("jti", "unique-id-123")
        .claim("roles", List.of("ADMIN"))
        .claim("custom", "value")
        .claim("iat", issuedAt)
        .claim("exp", expiresAt)
        .build();

      // Act
      AbstractAuthenticationToken result = converter.convert(jwt);

      // Assert
      assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
      JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) result;
      Assertions.assertNotNull(jwtAuth);
      Jwt resultToken = jwtAuth.getToken();
      assertThat(resultToken.getTokenValue()).isEqualTo("detailed.jwt.token");
      assertThat(resultToken.getSubject()).isEqualTo("user222");
      assertThat(resultToken.getIssuer())
        .hasToString("https://test-issuer.com");
      assertThat(resultToken.getId()).isEqualTo("unique-id-123");
      assertThat(resultToken.getIssuedAt()).isEqualTo(issuedAt);
      assertThat(resultToken.getExpiresAt()).isEqualTo(expiresAt);
      assertThat((String) resultToken.getClaim("custom")).isEqualTo("value");
    }

    @Test
    @DisplayName("deve ser idempotente para múltiplas conversões")
    void givenSameJwt_whenConvertMultipleTimes_thenReturnEquivalentAuthentications() {
      // Arrange
      Jwt jwt = createJwtWithRolesList(List.of("ADMIN", "CLIENT"));

      // Act
      AbstractAuthenticationToken result1 = converter.convert(jwt);
      AbstractAuthenticationToken result2 = converter.convert(jwt);

      // Assert
      assertThat(result1.getName()).isEqualTo(result2.getName());
      assertThat(result1.getAuthorities())
        .containsExactlyInAnyOrderElementsOf(result2.getAuthorities());

      JwtAuthenticationToken jwt1 = (JwtAuthenticationToken) result1;
      JwtAuthenticationToken jwt2 = (JwtAuthenticationToken) result2;
      assertThat(jwt1.getToken().getTokenValue())
        .isEqualTo(jwt2.getToken().getTokenValue());
    }
  }

  private Jwt createJwtWithRolesList(List<String> roles) {
    return Jwt
      .withTokenValue("token")
      .header("alg", "RS256")
      .claim("sub", "user123")
      .claim("roles", roles)
      .claim("iat", Instant.now())
      .claim("exp", Instant.now().plusSeconds(300))
      .build();
  }
}

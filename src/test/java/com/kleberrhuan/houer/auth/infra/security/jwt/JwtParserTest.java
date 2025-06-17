/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

@DisplayName("JwtParser")
class JwtParserTest {

  private JwtParser parser;
  private RSAPrivateKey privateKey;

  @BeforeEach
  void setup() throws NoSuchAlgorithmException {
    parser = new JwtParser();

    // Generate test RSA keys
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair keyPair = gen.generateKeyPair();
    privateKey = (RSAPrivateKey) keyPair.getPrivate();
  }

  @Nested
  @DisplayName("parse")
  class Parse {

    @Test
    @DisplayName("deve parsear token JWT válido corretamente")
    void givenValidJwtToken_whenParse_thenReturnJwtWithCorrectClaims()
      throws JOSEException {
      // Arrange
      String jti = UUID.randomUUID().toString();
      String subject = "123";
      Instant now = Instant.now();
      Instant expiry = now.plusSeconds(900);

      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .jwtID(jti)
        .subject(subject)
        .issuer("https://test-issuer.com")
        .issueTime(Date.from(now))
        .expirationTime(Date.from(expiry))
        .claim("scope", "read write")
        .claim("roles", "ADMIN CLIENT")
        .build();

      SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.RS256),
        claimsSet
      );
      signedJWT.sign(new RSASSASigner(privateKey));
      String tokenString = signedJWT.serialize();

      // Act
      Jwt result = parser.parse(tokenString);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getTokenValue()).isEqualTo(tokenString);
      assertThat(result.getId()).isEqualTo(jti);
      assertThat(result.getSubject()).isEqualTo(subject);
      assertThat(result.getIssuedAt()).isCloseTo(now, within(1, SECONDS));
      assertThat(result.getExpiresAt()).isCloseTo(expiry, within(1, SECONDS));
      assertThat((String) result.getClaim("scope")).isEqualTo("read write");
      assertThat((String) result.getClaim("roles")).isEqualTo("ADMIN CLIENT");
    }

    @Test
    @DisplayName("deve parsear token com claims mínimos")
    void givenTokenWithMinimalClaims_whenParse_thenReturnJwtWithBasicClaims()
      throws JOSEException {
      // Arrange
      Instant now = Instant.now();
      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject("user123")
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusSeconds(300)))
        .build();

      SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.RS256),
        claimsSet
      );
      signedJWT.sign(new RSASSASigner(privateKey));
      String tokenString = signedJWT.serialize();

      // Act
      Jwt result = parser.parse(tokenString);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getSubject()).isEqualTo("user123");
      assertThat(result.getId()).isNull(); // No JTI claim
    }

    @Test
    @DisplayName("deve lançar AuthException para token malformado")
    void givenMalformedToken_whenParse_thenThrowAuthException() {
      // Arrange
      String malformedToken = "not.a.valid.jwt.token";

      // Act & Assert
      assertThatThrownBy(() -> parser.parse(malformedToken))
        .isInstanceOf(AuthException.class)
        .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName("deve lançar AuthException para token com estrutura inválida")
    void givenTokenWithInvalidStructure_whenParse_thenThrowAuthException() {
      // Arrange - token com apenas 2 partes em vez de 3
      String invalidStructureToken = "header.payload";

      // Act & Assert
      assertThatThrownBy(() -> parser.parse(invalidStructureToken))
        .isInstanceOf(AuthException.class)
        .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName("deve lançar AuthException para token vazio")
    void givenEmptyToken_whenParse_thenThrowAuthException() {
      // Arrange
      String emptyToken = "";

      // Act & Assert
      assertThatThrownBy(() -> parser.parse(emptyToken))
        .isInstanceOf(AuthException.class)
        .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName("deve lançar AuthException para token nulo")
    void givenNullToken_whenParse_thenThrowAuthException() {
      // Act & Assert
      assertThatThrownBy(() -> parser.parse(null))
        .isInstanceOf(AuthException.class)
        .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName(
      "deve lançar AuthException para token com payload JSON inválido"
    )
    void givenTokenWithInvalidJsonPayload_whenParse_thenThrowAuthException() {
      // Arrange - JWT com payload inválido (não é JSON válido)
      String header = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9"; // valid header
      String invalidPayload = "invalid_base64_json";
      String signature = "signature";
      String invalidToken = header + "." + invalidPayload + "." + signature;

      // Act & Assert
      assertThatThrownBy(() -> parser.parse(invalidToken))
        .isInstanceOf(AuthException.class)
        .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName("deve preservar todos os claims customizados")
    void givenTokenWithCustomClaims_whenParse_thenPreserveAllClaims()
      throws JOSEException {
      // Arrange
      Instant now = Instant.now();
      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject("user123")
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusSeconds(300)))
        .claim("custom_claim", "custom_value")
        .claim("numeric_claim", 42)
        .claim("boolean_claim", true)
        .build();

      SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.RS256),
        claimsSet
      );
      signedJWT.sign(new RSASSASigner(privateKey));
      String tokenString = signedJWT.serialize();

      // Act
      Jwt result = parser.parse(tokenString);

      // Assert
      assertThat((String) result.getClaim("custom_claim"))
        .isEqualTo("custom_value");
      assertThat(((Number) result.getClaim("numeric_claim")).intValue())
        .isEqualTo(42);
      assertThat((Boolean) result.getClaim("boolean_claim")).isEqualTo(true);
    }
  }
}

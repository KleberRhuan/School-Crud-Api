/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.web;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.auth.application.service.AuthenticationService;
import com.kleberrhuan.houer.auth.application.service.RegistrationService;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.common.application.port.ratelimit.RateLimiter;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.infra.properties.AuditProperties;
import com.kleberrhuan.houer.common.infra.properties.RateLimitProperties;
import com.kleberrhuan.houer.common.interfaces.documentation.controllers.AuthControllerDocumentation;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.Cookie;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@Disabled("Disabled temporarily for unit test focus")
@AutoConfigureWebMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
  properties = {
    "jwt.public-key=classpath:certificates/public.pub",
    "jwt.private-key=classpath:certificates/private.pem",
    "jwt.issuer=test-issuer",
    "jwt.access-ttl-sec=900",
    "jwt.refresh-ttl-sec=604800",
    "app.cors.allowed-origins=http://localhost:3000",
    "app.rate-limit.enabled=false",
    "app.frontend-url=http://localhost:3000",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.audit.enabled=false",
    "spring.jpa.properties.hibernate.listeners.envers.autoRegister=false",
    "app.brevo.api-key=test-key",
    "app.brevo.name=Test Name",
    "app.brevo.email=test@example.com",
    "app.brevo.url=https://api.brevo.com",
    "app.brevo.timeout.connect=5000",
    "app.brevo.timeout.read=10000",
  }
)
@DisplayName("AuthController")
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthenticationService authService;

  @MockitoBean
  private RegistrationService registrationService;

  @MockitoBean
  private AuthControllerDocumentation authControllerDocumentation;

  @MockitoBean
  private MeterRegistry meterRegistry;

  @MockitoBean
  private RateLimiter rateLimiter;

  @MockitoBean
  private ApiErrorResponseFactory errorFactory;

  @MockitoBean
  private RateLimitProperties rateLimitProperties;

  @MockitoBean
  private AuditProperties auditProperties;

  @BeforeEach
  void setup() {
    reset(authService, registrationService);

    // Configurar mock do MeterRegistry para retornar Timer válido
    Timer mockTimer = mock(Timer.class);
    when(meterRegistry.timer(anyString(), any(String[].class)))
      .thenReturn(mockTimer);
    when(meterRegistry.timer(anyString())).thenReturn(mockTimer);
  }

  @Nested
  @DisplayName("POST /v1/auth/login")
  class Login {

    @Test
    @DisplayName("deve fazer login com sucesso e retornar access token")
    void givenValidCredentials_whenLogin_thenReturnToken() throws Exception {
      // Given
      LoginRequest request = new LoginRequest(
        "test@example.com",
        "password123",
        false
      );
      TokenPair tokenPair = new TokenPair(
        "access-token",
        Optional.empty(),
        900L
      );

      when(authService.login(request)).thenReturn(tokenPair);

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").value(900))
        .andExpect(header().doesNotExist("Set-Cookie"));

      verify(authService).login(request);
    }

    @Test
    @DisplayName(
      "deve fazer login com remember me e retornar refresh token no cookie"
    )
    void givenRememberMe_whenLogin_thenSetRefreshCookie() throws Exception {
      // Given
      LoginRequest request = new LoginRequest(
        "test@example.com",
        "password123",
        true
      );
      TokenPair tokenPair = new TokenPair(
        "access-token",
        Optional.of("refresh-token"),
        900L
      );

      when(authService.login(request)).thenReturn(tokenPair);

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(header().exists("Set-Cookie"));

      verify(authService).login(request);
    }

    @Test
    @DisplayName("deve retornar 401 para credenciais inválidas")
    void givenInvalidCredentials_whenLogin_thenReturn401() throws Exception {
      // Given
      LoginRequest request = new LoginRequest(
        "test@example.com",
        "wrongpassword",
        false
      );

      when(authService.login(request))
        .thenThrow(AuthException.badCredentials());

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isUnauthorized());

      verify(authService).login(request);
    }

    @Test
    @DisplayName("deve retornar 400 para dados de entrada inválidos")
    void givenInvalidInput_whenLogin_thenReturn400() throws Exception {
      // Given - email inválido
      LoginRequest request = new LoginRequest(
        "invalid-email",
        "password123",
        false
      );

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());

      verify(authService, never()).login(any());
    }
  }

  @Nested
  @DisplayName("POST /v1/auth/register")
  class Register {

    @Test
    @DisplayName("deve registrar usuário com sucesso")
    void givenValidData_whenRegister_thenReturn201() throws Exception {
      // Given
      RegisterRequest request = new RegisterRequest(
        "Test User",
        "test@example.com",
        "Password@123"
      );

      doNothing().when(registrationService).register(eq(request), anyString());

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated());

      verify(registrationService).register(eq(request), anyString());
    }

    @Test
    @DisplayName("deve retornar 400 para dados inválidos")
    void givenInvalidData_whenRegister_thenReturn400() throws Exception {
      // Given - senha muito fraca
      RegisterRequest request = new RegisterRequest(
        "Test User",
        "test@example.com",
        "123"
      );

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());

      verify(registrationService, never()).register(any(), anyString());
    }

    @Test
    @DisplayName("deve retornar 400 para email inválido")
    void givenInvalidEmail_whenRegister_thenReturn400() throws Exception {
      // Given
      RegisterRequest request = new RegisterRequest(
        "Test User",
        "invalid-email",
        "Password@123"
      );

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());

      verify(registrationService, never()).register(any(), anyString());
    }

    @Test
    @DisplayName("deve retornar 400 para nome em branco")
    void givenBlankName_whenRegister_thenReturn400() throws Exception {
      // Given
      RegisterRequest request = new RegisterRequest(
        "",
        "test@example.com",
        "Password@123"
      );

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());

      verify(registrationService, never()).register(any(), anyString());
    }
  }

  @Nested
  @DisplayName("GET /v1/auth/verify")
  class Verify {

    @Test
    @DisplayName("deve verificar token com sucesso e redirecionar")
    void givenValidToken_whenVerify_thenRedirect() throws Exception {
      // Given
      UUID token = UUID.randomUUID();

      doNothing().when(registrationService).verify(token);

      // When & Then
      mockMvc
        .perform(get("/v1/auth/verify").param("token", token.toString()))
        .andExpect(status().isFound())
        .andExpect(
          header().string("Location", "http://localhost:3000/auth/verified")
        );

      verify(registrationService).verify(token);
    }

    @Test
    @DisplayName("deve retornar erro para token inválido")
    void givenInvalidToken_whenVerify_thenReturnError() throws Exception {
      // Given
      UUID token = UUID.randomUUID();

      doThrow(AuthException.verificationInvalid())
        .when(registrationService)
        .verify(token);

      // When & Then
      mockMvc
        .perform(get("/v1/auth/verify").param("token", token.toString()))
        .andExpect(status().isGone());

      verify(registrationService).verify(token);
    }

    @Test
    @DisplayName("deve retornar 400 para token malformado")
    void givenMalformedToken_whenVerify_thenReturn400() throws Exception {
      // When & Then
      mockMvc
        .perform(get("/v1/auth/verify").param("token", "invalid-uuid"))
        .andExpect(status().isBadRequest());

      verify(registrationService, never()).verify(any());
    }
  }

  @Nested
  @DisplayName("POST /v1/auth/refresh")
  class Refresh {

    @Test
    @DisplayName("deve renovar token com sucesso")
    void givenValidRefreshToken_whenRefresh_thenReturnNewTokens()
      throws Exception {
      // Given
      String refreshToken = "valid-refresh-token";
      TokenPair tokenPair = new TokenPair(
        "new-access-token",
        Optional.of("new-refresh-token"),
        900L
      );

      when(authService.refresh(refreshToken)).thenReturn(tokenPair);

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/refresh")
            .cookie(new Cookie("REFRESH_TOKEN", refreshToken))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access-token"))
        .andExpect(header().exists("Set-Cookie"));

      verify(authService).refresh(refreshToken);
    }

    @Test
    @DisplayName("deve retornar 401 para refresh token inválido")
    void givenInvalidRefreshToken_whenRefresh_thenReturn401() throws Exception {
      // Given
      String refreshToken = "invalid-refresh-token";

      when(authService.refresh(refreshToken))
        .thenThrow(AuthException.refreshNotFound());

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/refresh")
            .cookie(new Cookie("REFRESH_TOKEN", refreshToken))
        )
        .andExpect(status().isUnauthorized());

      verify(authService).refresh(refreshToken);
    }

    @Test
    @DisplayName("deve retornar 400 quando cookie não estiver presente")
    void givenMissingCookie_whenRefresh_thenReturn400() throws Exception {
      // When & Then
      mockMvc
        .perform(post("/v1/auth/refresh"))
        .andExpect(status().isBadRequest());

      verify(authService, never()).refresh(any());
    }
  }

  @Nested
  @DisplayName("POST /v1/auth/logout")
  class Logout {

    @Test
    @WithMockUser
    @DisplayName("deve fazer logout com sucesso")
    void givenAuthenticatedUser_whenLogout_thenReturn200() throws Exception {
      // Given
      String jti = "token-jti";

      doNothing().when(authService).logout(jti);

      // When & Then
      mockMvc
        .perform(
          post("/v1/auth/logout")
            .header("Authorization", "Bearer jwt-token")
            .requestAttr("jti", jti)
        )
        .andExpect(status().isOk());

      verify(authService).logout(jti);
    }
  }
}

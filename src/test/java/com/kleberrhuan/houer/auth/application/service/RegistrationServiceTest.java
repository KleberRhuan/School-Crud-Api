/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.application.factory.VerificationTokenFactory;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import com.kleberrhuan.houer.auth.domain.repository.VerificationTokenRepository;
import com.kleberrhuan.houer.auth.domain.service.VerificationTokenDomainService;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.user.application.mapper.UserMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RegistrationService")
class RegistrationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private VerificationTokenRepository verificationTokenRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private VerificationTokenFactory tokenFactory;

  @Mock
  private VerificationTokenDomainService tokenDomainService;

  @Mock
  private UserVerificationService userVerificationService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private MeterRegistry meter;

  @Mock
  private Counter counter;

  @InjectMocks
  private RegistrationService service;

  @BeforeEach
  void setup() {
    when(meter.counter(anyString())).thenReturn(counter);
  }

  private User dummyUser() {
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setEnabled(false);
    return user;
  }

  private RegisterRequest dummyRequest() {
    return new RegisterRequest("Test User", "test@example.com", "Password@123");
  }

  @Nested
  @DisplayName("register")
  class Register {

    @Test
    @DisplayName("deve registrar usuário com sucesso e publicar evento")
    void givenValidRequest_whenRegister_thenCreateUserAndPublishEvent() {
      // Given
      RegisterRequest request = dummyRequest();
      User user = dummyUser();
      VerificationToken token = new VerificationToken(
        UUID.randomUUID(),
        user.getId(),
        Instant.now().plus(1, ChronoUnit.HOURS),
        false
      );
      String baseUrl = "http://localhost:8080";

      when(userMapper.toEntity(request, passwordEncoder)).thenReturn(user);
      when(userRepository.save(user)).thenReturn(user);
      when(tokenFactory.createForUser(user.getId())).thenReturn(token);
      when(verificationTokenRepository.save(token)).thenReturn(token);

      // When
      service.register(request, baseUrl);

      // Then
      verify(userRepository).save(user);
      verify(tokenFactory).createForUser(user.getId());
      verify(verificationTokenRepository).save(token);
      verify(notificationService)
        .publishUserRegisteredEvent(
          user.getEmail(),
          user.getName(),
          baseUrl,
          token.getToken()
        );
    }

    @Test
    @DisplayName("deve propagar exceção quando mapper falhar")
    void givenMapperThrows_whenRegister_thenPropagateException() {
      // Given
      RegisterRequest request = dummyRequest();
      String baseUrl = "http://localhost:8080";

      when(userMapper.toEntity(request, passwordEncoder))
        .thenThrow(new RuntimeException("Mapping failed"));

      // When & Then
      assertThatThrownBy(() -> service.register(request, baseUrl))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Mapping failed");

      verify(userRepository, never()).save(any(User.class));
      verify(verificationTokenRepository, never())
        .save(any(VerificationToken.class));
      verify(notificationService, never())
        .publishUserRegisteredEvent(
          anyString(),
          anyString(),
          anyString(),
          any(UUID.class)
        );
    }

    @Test
    @DisplayName("deve propagar exceção quando salvar usuário falhar")
    void givenUserSaveFails_whenRegister_thenPropagateException() {
      // Given
      RegisterRequest request = dummyRequest();
      User user = dummyUser();
      String baseUrl = "http://localhost:8080";

      when(userMapper.toEntity(request, passwordEncoder)).thenReturn(user);
      when(userRepository.save(user))
        .thenThrow(new RuntimeException("Database error"));

      // When & Then
      assertThatThrownBy(() -> service.register(request, baseUrl))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Database error");

      verify(tokenFactory, never()).createForUser(anyLong());
      verify(verificationTokenRepository, never())
        .save(any(VerificationToken.class));
      verify(notificationService, never())
        .publishUserRegisteredEvent(
          anyString(),
          anyString(),
          anyString(),
          any(UUID.class)
        );
    }
  }

  @Nested
  @DisplayName("verify")
  class Verify {

    @Test
    @DisplayName("deve verificar token válido com sucesso")
    void givenValidToken_whenVerify_thenActivateUser() {
      // Given
      UUID token = UUID.randomUUID();
      User user = dummyUser();
      VerificationToken vt = new VerificationToken(
        token,
        user.getId(),
        Instant.now().plus(1, ChronoUnit.HOURS),
        false
      );

      when(tokenDomainService.findValidToken(token)).thenReturn(vt);
      when(userVerificationService.findUserForVerification(user.getId(), token))
        .thenReturn(user);

      // When
      service.verify(token);

      // Then
      verify(tokenDomainService).findValidToken(token);
      verify(userVerificationService)
        .findUserForVerification(user.getId(), token);
      verify(userVerificationService).enableUser(user, token);
      verify(tokenDomainService).markTokenAsUsed(vt);
    }

    @Test
    @DisplayName("deve lançar exceção para token não encontrado")
    void givenTokenNotFound_whenVerify_thenThrowException() {
      // Given
      UUID token = UUID.randomUUID();

      when(tokenDomainService.findValidToken(token))
        .thenThrow(AuthException.verificationInvalid());

      // When & Then
      assertThatThrownBy(() -> service.verify(token))
        .isInstanceOf(AuthException.class);

      verify(tokenDomainService).findValidToken(token);
      verifyNoInteractions(userVerificationService);
    }

    @Test
    @DisplayName("deve lançar exceção para token expirado")
    void givenExpiredToken_whenVerify_thenThrowException() {
      // Given
      UUID token = UUID.randomUUID();

      when(tokenDomainService.findValidToken(token))
        .thenThrow(AuthException.verificationExpired());

      // When & Then
      assertThatThrownBy(() -> service.verify(token))
        .isInstanceOf(AuthException.class);

      verify(tokenDomainService).findValidToken(token);
      verifyNoInteractions(userVerificationService);
    }

    @Test
    @DisplayName("deve lançar exceção para token já usado")
    void givenUsedToken_whenVerify_thenThrowException() {
      // Given
      UUID token = UUID.randomUUID();

      when(tokenDomainService.findValidToken(token))
        .thenThrow(AuthException.verificationInvalid());

      // When & Then
      assertThatThrownBy(() -> service.verify(token))
        .isInstanceOf(AuthException.class);

      verify(tokenDomainService).findValidToken(token);
      verifyNoInteractions(userVerificationService);
    }

    @Test
    @DisplayName("deve funcionar mesmo no limite da expiração")
    void givenTokenAtExpirationBoundary_whenVerify_thenHandle() {
      // Given
      UUID token = UUID.randomUUID();
      User user = dummyUser();
      VerificationToken vt = new VerificationToken(
        token,
        user.getId(),
        Instant.now().plus(1, ChronoUnit.SECONDS), // Expires very soon
        false
      );

      when(tokenDomainService.findValidToken(token)).thenReturn(vt);
      when(userVerificationService.findUserForVerification(user.getId(), token))
        .thenReturn(user);

      // When & Then - Should not throw exception
      assertThatCode(() -> service.verify(token)).doesNotThrowAnyException();

      verify(tokenDomainService).findValidToken(token);
      verify(userVerificationService)
        .findUserForVerification(user.getId(), token);
      verify(userVerificationService).enableUser(user, token);
      verify(tokenDomainService).markTokenAsUsed(vt);
    }
  }
}

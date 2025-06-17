/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.domain.event.UserRegisteredEvent;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import com.kleberrhuan.houer.auth.domain.repository.VerificationTokenRepository;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.user.application.mapper.UserMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RegistrationService")
class RegistrationServiceTest {

  @Mock
  private UserRepository users;

  @Mock
  private VerificationTokenRepository vtRepo;

  @Mock
  private UserMapper mapper;

  @Mock
  private PasswordEncoder encoder;

  @Mock
  private ApplicationEventPublisher events;

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
      String baseUrl = "http://localhost:8080";

      when(mapper.toEntity(request, encoder)).thenReturn(user);
      when(users.save(user)).thenReturn(user);

      // When
      service.register(request, baseUrl);

      // Then
      verify(users).save(user);
      verify(vtRepo).save(any(VerificationToken.class));
      verify(events).publishEvent(any(UserRegisteredEvent.class));

      // Verify verification token is created correctly
      ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(
        VerificationToken.class
      );
      verify(vtRepo).save(tokenCaptor.capture());
      VerificationToken savedToken = tokenCaptor.getValue();

      assertThat(savedToken.getToken()).isNotNull();
      assertThat(savedToken.getUserId()).isEqualTo(user.getId());
      assertThat(savedToken.getExpiresAt())
        .isAfter(Instant.now())
        .isBefore(Instant.now().plus(25, ChronoUnit.HOURS)); // Allow margin
      assertThat(savedToken.isUsed()).isFalse();

      // Verify event is published correctly
      ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(
        UserRegisteredEvent.class
      );
      verify(events).publishEvent(eventCaptor.capture());
      UserRegisteredEvent publishedEvent = eventCaptor.getValue();

      assertThat(publishedEvent.email()).isEqualTo(user.getEmail());
      assertThat(publishedEvent.name()).isEqualTo(user.getName());
      assertThat(publishedEvent.verifyLink())
        .startsWith(baseUrl + "/auth/verify?token=")
        .contains(savedToken.getToken().toString());
    }

    @Test
    @DisplayName("deve propagar exceção quando mapper falhar")
    void givenMapperThrows_whenRegister_thenPropagateException() {
      // Given
      RegisterRequest request = dummyRequest();
      String baseUrl = "http://localhost:8080";

      when(mapper.toEntity(request, encoder))
        .thenThrow(new RuntimeException("Mapping failed"));

      // When & Then
      assertThatThrownBy(() -> service.register(request, baseUrl))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Mapping failed");

      verify(users, never()).save(any(User.class));
      verify(vtRepo, never()).save(any(VerificationToken.class));
      verify(events, never()).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    @DisplayName("deve propagar exceção quando salvar usuário falhar")
    void givenUserSaveFails_whenRegister_thenPropagateException() {
      // Given
      RegisterRequest request = dummyRequest();
      User user = dummyUser();
      String baseUrl = "http://localhost:8080";

      when(mapper.toEntity(request, encoder)).thenReturn(user);
      when(users.save(user)).thenThrow(new RuntimeException("Database error"));

      // When & Then
      assertThatThrownBy(() -> service.register(request, baseUrl))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Database error");

      verify(vtRepo, never()).save(any(VerificationToken.class));
      verify(events, never()).publishEvent(any(UserRegisteredEvent.class));
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

      when(vtRepo.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(vt));
      when(users.getReferenceById(user.getId())).thenReturn(user);

      // When
      service.verify(token);

      // Then
      assertThat(vt.isUsed()).isTrue();
      assertThat(user.isEnabled()).isTrue();
      verify(counter).increment();
    }

    @Test
    @DisplayName("deve lançar exceção para token não encontrado")
    void givenTokenNotFound_whenVerify_thenThrowException() {
      // Given
      UUID token = UUID.randomUUID();
      Counter invalidCounter = mock(Counter.class);

      when(vtRepo.findByTokenAndUsedFalse(token)).thenReturn(Optional.empty());
      when(meter.counter("auth.verification.invalid"))
        .thenReturn(invalidCounter);

      // When & Then
      assertThatThrownBy(() -> service.verify(token))
        .isInstanceOf(AuthException.class);

      verify(invalidCounter).increment();
      verify(users, never()).getReferenceById(anyLong());
    }

    @Test
    @DisplayName("deve lançar exceção para token expirado")
    void givenExpiredToken_whenVerify_thenThrowException() {
      // Given
      UUID token = UUID.randomUUID();
      VerificationToken vt = new VerificationToken(
        token,
        1L,
        Instant.now().minus(1, ChronoUnit.HOURS), // Expired
        false
      );
      Counter expiredCounter = mock(Counter.class);

      when(vtRepo.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(vt));
      when(meter.counter("auth.verification.expired"))
        .thenReturn(expiredCounter);

      // When & Then
      assertThatThrownBy(() -> service.verify(token))
        .isInstanceOf(AuthException.class);

      verify(expiredCounter).increment();
      verify(users, never()).getReferenceById(anyLong());
      assertThat(vt.isUsed()).isFalse(); // Should not mark as used
    }

    @Test
    @DisplayName("deve lançar exceção para token já usado")
    void givenUsedToken_whenVerify_thenThrowException() {
      // Given
      UUID token = UUID.randomUUID();
      VerificationToken vt = new VerificationToken(
        token,
        1L,
        Instant.now().plus(1, ChronoUnit.HOURS),
        true // Already used
      );
      Counter invalidCounter = mock(Counter.class);

      when(vtRepo.findByTokenAndUsedFalse(token)).thenReturn(Optional.empty());
      when(meter.counter("auth.verification.invalid"))
        .thenReturn(invalidCounter);

      // When & Then
      assertThatThrownBy(() -> service.verify(token))
        .isInstanceOf(AuthException.class);

      verify(invalidCounter).increment();
      verify(users, never()).getReferenceById(anyLong());
    }

    @Test
    @DisplayName("deve funcionar mesmo no limite da expiração")
    void givenTokenAtExpirationBoundary_whenVerify_thenHandle() {
      // Given - Token expiring in 1 second
      UUID token = UUID.randomUUID();
      User user = dummyUser();
      VerificationToken vt = new VerificationToken(
        token,
        user.getId(),
        Instant.now().plus(1, ChronoUnit.SECONDS),
        false
      );

      when(vtRepo.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(vt));
      when(users.getReferenceById(user.getId())).thenReturn(user);

      // When & Then - Should succeed as it's still valid
      assertThatCode(() -> service.verify(token)).doesNotThrowAnyException();

      assertThat(vt.isUsed()).isTrue();
      assertThat(user.isEnabled()).isTrue();
      verify(counter).increment();
    }
  }
}

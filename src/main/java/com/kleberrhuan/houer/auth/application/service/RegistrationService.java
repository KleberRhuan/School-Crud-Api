/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.domain.event.UserRegisteredEvent;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import com.kleberrhuan.houer.auth.domain.repository.VerificationTokenRepository;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.user.application.mapper.UserMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

  private final UserRepository users;
  private final VerificationTokenRepository vtRepo;
  private final UserMapper mapper;
  private final PasswordEncoder encoder;
  private final ApplicationEventPublisher events;
  private final MeterRegistry meter;

  private Counter verifyOk() {
    return meter.counter("auth.verification.success");
  }

  private Counter verifyInvalid() {
    return meter.counter("auth.verification.invalid");
  }

  private Counter verifyExpired() {
    return meter.counter("auth.verification.expired");
  }

  /* ---------------- register ---------------- */
  @Counted(value = "auth.registration.success")
  @Transactional
  public void register(RegisterRequest dto, String baseUrl) {
    User u = mapper.toEntity(dto, encoder);
    users.save(u);

    UUID token = UUID.randomUUID();
    vtRepo.save(
      new VerificationToken(
        token,
        u.getId(),
        Instant.now().plus(Duration.ofDays(1)),
        false
      )
    );

    events.publishEvent(
      new UserRegisteredEvent(
        u.getEmail(),
        u.getName(),
        baseUrl + "/auth/verify?token=" + token
      )
    );

    log.info("New user registered: {}", u.getEmail());
  }

  /* ---------------- verify ------------------ */
  @Transactional
  public void verify(UUID token) {
    VerificationToken vt = vtRepo
      .findByTokenAndUsedFalse(token)
      .orElseThrow(() -> {
        verifyInvalid().increment();
        log.warn("Invalid verification token: {}", token);
        return AuthException.verificationInvalid();
      });

    if (vt.getExpiresAt().isBefore(Instant.now())) {
      verifyExpired().increment();
      log.error("Expired token: {}", token);
      throw AuthException.verificationExpired();
    }

    vt.setUsed(true);
    users.getReferenceById(vt.getUserId()).setEnabled(true);
    verifyOk().increment();
    log.info("User verification successful: token={}", token);
  }
}

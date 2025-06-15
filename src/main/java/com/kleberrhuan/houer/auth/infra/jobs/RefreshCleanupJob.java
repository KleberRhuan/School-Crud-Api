/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.jobs;

import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshCleanupJob {

  private final RefreshTokenRepository repo;

  @Scheduled(cron = "0 0 * * * *")
  public void purge() {
    repo.deleteExpired(Instant.now());
  }
}

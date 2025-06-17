/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshCleanupJob")
class RefreshCleanupJobTest {

  @Mock
  RefreshTokenRepository repo;

  @Test
  @DisplayName("purge deve chamar deleteExpired com timestamp atual")
  void purgeCallsRepo() {
    RefreshCleanupJob job = new RefreshCleanupJob(repo);

    job.purge();

    ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
    verify(repo).deleteExpired(captor.capture());
    Instant ts = captor.getValue();
    assertThat(Duration.between(ts, Instant.now()).abs().toSeconds())
      .isLessThanOrEqualTo(1);
  }
}

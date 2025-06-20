/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class EmailRateLimitService {

  @Qualifier("emailRateLimitCache")
  private final Cache<String, Integer> rateLimitCache;

  @Value("${app.concurrency.email.rate-limit.max-per-hour:15}")
  private int maxEmailsPerHour;

  @Value("${app.concurrency.email.rate-limit.enabled:true}")
  private boolean rateLimitEnabled;

  public EmailRateLimitService(
    @Qualifier("emailRateLimitCache") Cache<String, Integer> rateLimitCache
  ) {
    this.rateLimitCache = rateLimitCache;
  }

  public boolean canSendEmail(String email, String emailType) {
    if (!rateLimitEnabled) {
      return true;
    }

    String rateLimitKey = generateRateLimitKey(email, emailType);

    Integer currentCount = rateLimitCache.getIfPresent(rateLimitKey);
    int count = currentCount != null ? currentCount : 0;

    if (count >= maxEmailsPerHour) {
      log.warn(
        "Rate limit atingido para email {} tipo {}: {}/{}",
        email,
        emailType,
        count,
        maxEmailsPerHour
      );
      return false;
    }

    rateLimitCache.put(rateLimitKey, count + 1);

    log.debug(
      "Rate limit check para email {} tipo {}: {}/{}",
      email,
      emailType,
      count + 1,
      maxEmailsPerHour
    );

    return true;
  }

  private String generateRateLimitKey(String email, String emailType) {
    String hash = DigestUtils.md5DigestAsHex(email.getBytes());
    return String.format("rate_limit:email:%s:%s", emailType, hash);
  }
}

/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.listener;

import com.kleberrhuan.houer.auth.domain.event.UserRegisteredEvent;
import com.kleberrhuan.houer.common.application.dispatcher.notification.NotificationDispatcher;
import com.kleberrhuan.houer.common.application.service.EmailRateLimitService;
import com.kleberrhuan.houer.common.application.service.notification.MailTemplateService;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import io.micrometer.core.annotation.Counted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotentVerificationMailListener {

  private static final String CACHE_NAME = "email-idempotency";
  private static final String VERIFY_CHANNEL = "verify";
  private static final String PROCESSING_SUFFIX = ":processing";
  private static final String SENT_SUFFIX = ":sent";

  private final MailTemplateService templating;
  private final NotificationDispatcher dispatcher;
  private final CacheManager cacheManager;
  private final EmailRateLimitService rateLimitService;

  @Async("mailTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Counted("email.sent.verify.idempotent")
  public void handle(UserRegisteredEvent evt) {
    if (!rateLimitService.canSendEmail(evt.email(), VERIFY_CHANNEL)) {
      log.warn("Rate limit excedido para {}", evt.email());
      return;
    }

    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache == null) {
        log.error(
                "Cache '{}' não encontrado, enviando sem idempotência",
                CACHE_NAME
        );
      sendVerificationEmail(evt);
      return;
    }

    String keyBase = generateKey(evt.email(), evt.verifyLink());
    if (alreadySent(cache, keyBase) || alreadyProcessing(cache, keyBase)) {
      return;
    }

    try {
      markProcessing(cache, keyBase);
      sendVerificationEmail(evt);
      markSent(cache, keyBase);
      log.info("Email de verificação enviado para {}", evt.email());
    } catch (Exception ex) {
        log.error(
                "Erro ao enviar email para {}: {}",
                evt.email(),
                ex.getMessage(),
                ex
        );
      cache.evict(keyBase + PROCESSING_SUFFIX);
      throw ex;
    } finally {
      cache.evict(keyBase + PROCESSING_SUFFIX);
    }
  }

  private String generateKey(String email, String token) {
    String raw = email + ':' + token;
      String hash = DigestUtils.md5DigestAsHex(
              raw.getBytes(StandardCharsets.UTF_8)
      );
    return VERIFY_CHANNEL + ':' + hash;
  }

  private boolean alreadySent(Cache cache, String baseKey) {
    String sentKey = baseKey + SENT_SUFFIX;
    if (cache.get(sentKey) != null) {
      log.info("Já enviado nas últimas 24h para {}", baseKey);
      return true;
    }
    return false;
  }

  private boolean alreadyProcessing(Cache cache, String baseKey) {
    String procKey = baseKey + PROCESSING_SUFFIX;
    if (cache.get(procKey) != null) {
      log.info("Já em processamento para {}", baseKey);
      return true;
    }
    return false;
  }

  private void markProcessing(Cache cache, String baseKey) {
    cache.put(baseKey + PROCESSING_SUFFIX, Boolean.TRUE);
  }

  private void markSent(Cache cache, String baseKey) {
    cache.put(baseKey + SENT_SUFFIX, Boolean.TRUE);
  }

  private void sendVerificationEmail(UserRegisteredEvent evt) {
    String html = templating.render(
            "verify-account",
            Map.of("name", evt.name(), "verifyLink", evt.verifyLink())
    );

    NotificationModel mail = new NotificationModel(
            Channel.EMAIL,
            evt.email(),
            "Confirme sua conta",
            html
    );

    dispatcher.dispatch(mail);
  }
}

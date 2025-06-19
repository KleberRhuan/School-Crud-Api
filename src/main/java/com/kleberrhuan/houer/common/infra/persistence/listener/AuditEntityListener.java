/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.common.application.service.AuditEventPersister;
import com.kleberrhuan.houer.common.domain.model.AuditEvent;
import jakarta.persistence.Id;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class AuditEntityListener {

  private final AuditEventPersister persister;
  private final ObjectMapper mapper;
  private static final Map<Class<?>, Field> ID_CACHE =
    new ConcurrentHashMap<>();

  @PostPersist
  public void postInsert(Object entity) {
    save(entity, AuditAction.INSERT);
  }

  @PostUpdate
  public void postUpdate(Object entity) {
    save(entity, AuditAction.UPDATE);
  }

  @PostRemove
  public void postDelete(Object entity) {
    save(entity, AuditAction.DELETE);
  }

  private void save(Object entity, AuditAction action) {
    try {
      AuditEvent ev = buildEvent(entity, action);
      persister.persist(ev);
    } catch (Exception ex) {
      log.error(
        "Failed to persist audit event for {} {}",
        action,
        entity.getClass().getSimpleName(),
        ex
      );
    }
  }

  private AuditEvent buildEvent(Object entity, AuditAction action) {
    AuditEvent ev = new AuditEvent();

    /* actor ---------------------------------------------------- */
    Authentication auth = SecurityContextHolder
      .getContext()
      .getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      String sub = jwt.getClaimAsString("sub");
      String idStr = (sub != null && !sub.isBlank())
        ? sub
        : jwt.getClaimAsString("userId");
      if (idStr != null && !idStr.isBlank()) {
        try {
          ev.setActor(Long.parseLong(idStr));
        } catch (NumberFormatException ex) {
          log.warn("Invalid user id claim: {}", idStr);
          ev.setSystemActor();
        }
      } else {
        ev.setSystemActor();
      }
    } else {
      ev.setSystemActor();
    }

    /* meta ----------------------------------------------------- */
    ev.setEntity(entity.getClass().getSimpleName());
    Object extractedId = extractId(entity);
    ev.setEntityId(extractedId != null ? String.valueOf(extractedId) : null);
    ev.setAction(action.name());
    ev.setTs(Instant.now());

    /* payload -------------------------------------------------- */
    ev.setPayload(mapper.valueToTree(entity));
    return ev;
  }

  private Object extractId(Object entity) {
    Field idField = ID_CACHE.computeIfAbsent(
      entity.getClass(),
      this::findIdField
    );
    if (idField == null) return null;

    ReflectionUtils.makeAccessible(idField);
    return ReflectionUtils.getField(idField, entity);
  }

  private Field findIdField(Class<?> type) {
    for (Field f : type.getDeclaredFields()) {
      if (AnnotationUtils.findAnnotation(f, Id.class) != null) {
        return f;
      }
    }
    log.warn("No @Id field found for {}", type.getSimpleName());
    return null;
  }

  /* ---------- helper enum -------------------------------------- */
  private enum AuditAction {
    INSERT,
    UPDATE,
    DELETE,
  }
}

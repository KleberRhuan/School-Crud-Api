/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service;

import com.kleberrhuan.houer.common.domain.model.AuditEvent;
import com.kleberrhuan.houer.common.domain.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste {@link AuditEvent} em uma transação independente para não interferir na ActionQueue do Hibernate invocante
 * (evitando ConcurrentModificationException quando o listener é executado durante flush do provedor JPA).
 */
@Service
@RequiredArgsConstructor
public class AuditEventPersister {

  private final AuditEventRepository repo;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void persist(AuditEvent event) {
    repo.save(event);
  }
}

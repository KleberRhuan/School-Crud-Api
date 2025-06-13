/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.repository;

import com.kleberrhuan.houer.common.domain.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {}

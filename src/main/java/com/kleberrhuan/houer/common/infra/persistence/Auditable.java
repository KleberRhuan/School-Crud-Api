/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence;

import com.kleberrhuan.houer.common.infra.persistence.listener.AuditEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class, AuditEntityListener.class })
@Getter
public abstract class Auditable<U> {

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @CreatedBy
  @JoinColumn(updatable = false, name = "created_by")
  private U createdBy;

  @LastModifiedBy
  @JoinColumn(nullable = false, name = "updated_by")
  private U updatedBy;
}

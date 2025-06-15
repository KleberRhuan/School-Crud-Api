/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

@MappedSuperclass
@SQLRestriction("deleted = false")
@Getter
public abstract class SoftDeletableAuditable<U> extends Auditable<U> {

  @Column(nullable = false)
  private boolean deleted = false;

  @Column
  private LocalDateTime deletedAt;

  public void delete() {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
  }
}

/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "audit_event", schema = "audit")
@Getter
@Setter
@NoArgsConstructor
public class AuditEvent {

  @AllArgsConstructor
  @Getter
  enum ActorType {
    USER("USER"),
    SYSTEM("SYSTEM");

    private final String value;

    @Override
    public String toString() {
      return this.value;
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String entity;

  @Column(name = "entity_id")
  private String entityId;

  private String action;
  private Instant ts;

  @Enumerated(EnumType.STRING)
  @Column(name = "actor_type", nullable = false)
  private ActorType actorType = ActorType.USER;

  @Column(name = "actor_id")
  private Long actorId;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Object payload;

  public void setActor(Long userId) {
    this.actorType = ActorType.USER;
    this.actorId = userId;
  }

  public void setSystemActor() {
    this.actorType = ActorType.SYSTEM;
    this.actorId = null;
  }
}

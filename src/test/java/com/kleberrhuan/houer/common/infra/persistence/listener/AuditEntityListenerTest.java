/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.common.domain.model.AuditEvent;
import com.kleberrhuan.houer.common.domain.repository.AuditEventRepository;
import jakarta.persistence.Id;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditEntityListener")
class AuditEntityListenerTest {

  @Mock
  AuditEventRepository repo;

  @Mock
  ObjectMapper mapper;

  AuditEntityListener listener;

  void setup() {
    listener = new AuditEntityListener(repo, mapper);
    SecurityContextHolder.clearContext(); // garante System actor
  }

  // ----- dummy entidades ---------------------------------------------------
  static class DummyEntity {

    @Id
    Long id;

    String name;

    DummyEntity(Long id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  static class EntityWithoutId {

    String something;

    EntityWithoutId(String something) {
      this.something = something;
    }
  }

  @Nested
  class PostInsert {

    @Test
    @DisplayName("deve persistir AuditEvent correto para INSERT")
    void shouldPersistAuditEventForInsert() {
      setup();
      DummyEntity entity = new DummyEntity(42L, "foo");
      when(mapper.valueToTree(any()))
        .thenReturn(com.fasterxml.jackson.databind.node.NullNode.getInstance());

      listener.postInsert(entity);

      ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(
        AuditEvent.class
      );
      verify(repo).save(captor.capture());
      AuditEvent ev = captor.getValue();

      assertThat(ev.getEntity()).isEqualTo(DummyEntity.class.getSimpleName());
      assertThat(ev.getEntityId()).isEqualTo("42");
      assertThat(ev.getAction()).isEqualTo("INSERT");
      assertThat(ev.getTs()).isBeforeOrEqualTo(Instant.now());
      assertThat(ev.getActorId()).isNull();
    }
  }

  @Nested
  class EntitySemId {

    @Test
    @DisplayName("deve salvar AuditEvent mesmo quando entidade não possui @Id")
    void shouldHandleEntityWithoutId() {
      setup();
      EntityWithoutId entity = new EntityWithoutId("bar");
      when(mapper.valueToTree(any()))
        .thenReturn(com.fasterxml.jackson.databind.node.NullNode.getInstance());

      listener.postInsert(entity);

      ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(
        AuditEvent.class
      );
      verify(repo).save(captor.capture());
      AuditEvent ev = captor.getValue();

      assertThat(ev.getEntityId()).isNull();
      assertThat(ev.getAction()).isEqualTo("INSERT");
    }
  }
}

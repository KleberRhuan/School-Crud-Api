/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.notification;

import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxStore {
  void save(OutboxMessage msg);

  void delete(UUID id);

  StoreHealth health();

  Optional<OutboxMessage> pollNextDue();

  default List<OutboxMessage> pollNextDue(int batchSize) {
    List<OutboxMessage> list = new ArrayList<>(batchSize);
    for (int i = 0; i < batchSize; i++) {
      Optional<OutboxMessage> opt = pollNextDue();
      if (opt.isEmpty()) break;
      list.add(opt.get());
    }
    return list;
  }

  default boolean isUp() {
    return health() == StoreHealth.UP;
  }

  enum StoreHealth {
    UP,
    DOWN,
  }
}

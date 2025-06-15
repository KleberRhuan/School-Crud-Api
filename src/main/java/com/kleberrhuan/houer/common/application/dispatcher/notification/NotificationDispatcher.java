/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import com.kleberrhuan.houer.common.application.port.dispatcher.DispatcherService;
import com.kleberrhuan.houer.common.application.port.notification.Notification;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import com.kleberrhuan.houer.common.domain.model.notification.Provider;
import com.kleberrhuan.houer.common.infra.exception.ProviderNotFoundException;
import com.kleberrhuan.houer.common.infra.properties.ProviderProps;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationDispatcher
  implements DispatcherService<NotificationModel> {

  private final List<Notification> services;
  private final ProviderProps props;
  private Map<String, Map<String, Notification>> reg;

  @PostConstruct
  void init() {
    reg =
      services
        .stream()
        .collect(
          Collectors.groupingBy(
            s -> s.provider().channel().name(),
            Collectors.toMap(s -> s.provider().name(), Function.identity())
          )
        );
  }

  @Override
  public void dispatch(NotificationModel n) {
    String providerName = props.forChannel(n.channel());
    if (providerName == null) throw new IllegalArgumentException(
      "Canal não mapeado: " + n.channel()
    );
    Notification svc = Optional
      .ofNullable(reg.get(n.channel().name()))
      .map(m -> m.get(providerName))
      .orElseThrow(() ->
        new ProviderNotFoundException(new Provider(n.channel(), providerName))
      );
    svc.send(n);
  }
}

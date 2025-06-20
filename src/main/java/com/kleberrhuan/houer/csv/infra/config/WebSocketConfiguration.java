/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração WebSocket para notificações em tempo real de importação CSV.
 *
 * <p>Endpoints disponíveis: - /ws: Endpoint principal STOMP com fallback SockJS
 *
 * <p>Canais de mensagem: - /topic/csv-import/{jobId}: Notificações de progresso de jobs específicos -
 * /user/{userId}/queue/csv-import: Notificações privadas do usuário - /user/{userId}/queue/csv-progress: Atualizações
 * de progresso em tempo real
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration
  implements WebSocketMessageBrokerConfigurer {

  @Value(
    "${school.import.websocket.allowed-origins:http://localhost:3000,http://localhost:3001,https://houer-test.rhuan.cloud}"
  )
  private String allowedOrigins;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue", "/user");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Endpoint principal com SockJS habilitado
    registry
      .addEndpoint("/ws")
      .setAllowedOriginPatterns(allowedOrigins.split(","))
      .withSockJS()
      .setHeartbeatTime(25000) // Heartbeat SockJS
      .setDisconnectDelay(30000) // Timeout para desconexão
      .setHttpMessageCacheSize(1000) // Cache de mensagens HTTP
      .setStreamBytesLimit(128 * 1024) // Limite de bytes por stream
      .setClientLibraryUrl(
        "https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"
      ); // SockJS via HTTPS

    // Endpoint nativo WebSocket (sem SockJS) - funciona melhor com HTTPS
    registry
      .addEndpoint("/ws-native")
      .setAllowedOriginPatterns(allowedOrigins.split(","));
  }
}

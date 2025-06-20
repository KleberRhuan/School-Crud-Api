/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.config;

import com.kleberrhuan.houer.auth.infra.config.TokenHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration
  implements WebSocketMessageBrokerConfigurer {

  @Value(
    "${school.import.websocket.allowed-origins:http://localhost:3000,http://localhost:3001,https://houer-test.rhuan.cloud}"
  )
  private String allowedOrigins;

  private final TokenHandshakeInterceptor tokenHandshakeInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue", "/user");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
      .addEndpoint("/ws")
      .setAllowedOriginPatterns(allowedOrigins.split(","))
      .addInterceptors(tokenHandshakeInterceptor)
      .withSockJS()
      .setHeartbeatTime(25000) // Heartbeat SockJS
      .setDisconnectDelay(30000) // Timeout para desconexão
      .setHttpMessageCacheSize(1000) // Cache de mensagens HTTP
      .setStreamBytesLimit(128 * 1024) // Limite de bytes por stream
      .setClientLibraryUrl(
        "https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"
      );
    
    registry
      .addEndpoint("/ws-native")
      .setAllowedOriginPatterns(allowedOrigins.split(","))
      .addInterceptors(
        tokenHandshakeInterceptor,
        new HttpSessionHandshakeInterceptor()
      );
  }
}

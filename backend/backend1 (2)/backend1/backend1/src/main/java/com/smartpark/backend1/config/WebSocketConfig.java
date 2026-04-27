package com.smartpark.backend1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // ✅ Endpoint 1 : WebSocket natif (pour Angular avec brokerURL)
    registry.addEndpoint("/ws")
      .setAllowedOriginPatterns("*");

    // ✅ Endpoint 2 : SockJS fallback (optionnel, pour anciens navigateurs)
    registry.addEndpoint("/ws")
      .setAllowedOriginPatterns("*")
      .withSockJS();
  }
}

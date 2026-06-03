package com.unibe.chatwebsocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker //Enciende el servidor de WebSocket con STOMP
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //La puerta de entrada
        // Los clientes (navegador/móvil) se conectarán a: ws://localhost:8080/chat-websocket
        registry.addEndpoint("/chat-websocket")
                .setAllowedOriginPatterns("*")//Permite que cualquier página web se conecte sin bloqueos de seguridad CORS
                .withSockJS(); //Para que la libreria de JS funcione
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //El cartero (Broker):
        //Habilitamos dos "rutas de reparto":
        //"/topic" -> para chats grupales (todos escuchas lo mismo)
        //"/queue" -> para chats privados tipo WhatsApp (mensajes directos 1 a 1)
        registry.enableSimpleBroker("/topic", "/queue");

        //Buzon de salida
        registry.setApplicationDestinationPrefixes("/app");
    }
}

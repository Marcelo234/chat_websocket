package com.unibe.chatwebsocket.controller;

import com.unibe.chatwebsocket.model.ChatMessage;
import com.unibe.chatwebsocket.repository.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {

    //Herramienta especifica para enviar mensajes a usuarios especificos (chat privado)
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    //Metodo auxiliar para no repetir codigo: obtiene la hora actual en formato "14:30"
    private String obtenerHoraActual() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    //Chat grupal (sala publica)
    //Si un cliente envia un mensaje a "/app/chat.enviar", este metodo lo atrapa
    @MessageMapping("/chat.enviar")
    //y automaticamente lo retransmite a todos los suscritos a "/topic/publico"
    @SendTo("/topic/publico")
    public ChatMessage enviarMensajeGrupal(@Payload ChatMessage mensaje) {
        ChatMessage mensajeConHora = new ChatMessage(
                mensaje.getId(),
                mensaje.getContenido(),
                mensaje.getRemitente(),
                mensaje.getDestinatario(),
                mensaje.getTipo(),
                obtenerHoraActual(),
                mensaje.getEstado()
        );
        return chatMessageRepository.save(mensajeConHora);
    }

    //Chat privado (Estilo WhatsApp 1 a 1)
    //Si un cliente envia un mensaje a "/app/chat.privado", entra aqui
    @MessageMapping("/chat.privado")
    public void enviarMensajePrivado(@Payload ChatMessage mensaje) {
        //Creamos el mensaje con la hora antes de enviarlo por el tunel privado
        ChatMessage mensajeConHora = new ChatMessage(
                mensaje.getId(),
                mensaje.getContenido(),
                mensaje.getRemitente(),
                mensaje.getDestinatario(),
                mensaje.getTipo(),
                obtenerHoraActual(),
                mensaje.getEstado()
        );

        //Guardamos el chat privado en PostgreSQL para que no se pierda el historial
        chatMessageRepository.save(mensajeConHora);

        messagingTemplate.convertAndSendToUser(
                mensaje.getDestinatario(),
                "/queue/mensajes",
                mensajeConHora
        );
    }

    //Registro de nuevo usuario
    @MessageMapping("/chat.unirse")
    @SendTo("/topic/publico")
    public ChatMessage registrarUsuario(@Payload ChatMessage mensaje, SimpMessageHeaderAccessor headerAccessor) {
        //Guardamos el nombre del usuario en su sesion de WebSocket para saber quien es
        headerAccessor.getSessionAttributes().put("username", mensaje.getRemitente());
        //Retornamos el aviso de "Se unio" con la hora exacta
        return new ChatMessage(
                mensaje.getId(),
                mensaje.getContenido(),
                mensaje.getRemitente(),
                mensaje.getDestinatario(),
                mensaje.getTipo(),
                obtenerHoraActual(),
                mensaje.getEstado()
        );
    }
}

package com.unibe.chatwebsocket.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mensajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private String remitente;
    private String destinatario;

    @Enumerated(EnumType.STRING)
    private TipoMensaje tipo;

    private String hora;
    private String estado;
}

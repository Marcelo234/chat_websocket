package com.unibe.chatwebsocket.model;

//Creamos un pequeño catalogo para saber si el mensaje es de texto normal, o si es una
//notificacion de que alguien entro o salio del chat

public enum TipoMensaje {
    CHAT,   //Un mensaje de texto normal
    UNIRSE, //Cuando alguien entra a la sala
    SALIR,   //Cuando alguien se desconecta
    LEIDO
}


let stompClient = null;
let nombreUsuario = null;
const chatDiv = document.getElementById('chat');

const btnEmoji = document.getElementById('btnEmoji');
const emojiPicker = document.getElementById('emojiPicker');
const inputMensaje = document.getElementById('inputMensaje');

//Aqui enlazamos los botones de HTML con las funciones de JavaScript
document.getElementById('btnConectar').addEventListener('click', conectar);
document.getElementById('btnEnviar').addEventListener('click', enviarMensaje);

//Permite conectar presionando 'Enter' en el campo de nombre
document.getElementById('inputNombre').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') conectar();
});

//Permitir enviar mensaje presionando 'Enter' en el campo de texto
document.getElementById('inputMensaje').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') enviarMensaje();
});

btnEmoji.addEventListener("click", (e) => {
    e.stopPropagation();
    const estaOculto = emojiPicker.style.display === 'none' || emojiPicker.style.display === '';
    emojiPicker.style.display = estaOculto ? 'grid' : 'none';
});

//Agrega el emoji seleccionado al input de texto
emojiPicker.addEventListener('click', (e) => {
    if (e.target.tagName === 'SPAN') {
        inputMensaje.value += e.target.innerHTML;
        inputMensaje.focus();
        emojiPicker.style.display = 'none';
    }
});

//Si el usuario hace clic fuera de la barra de controles, cerramos el panel
document.addEventListener('click', () => {
    emojiPicker.style.display = 'none';
});

function conectar() {
    nombreUsuario = document.getElementById('inputNombre').value.trim();
    if(nombreUsuario) {
        //Cambiamos de pantalla
        document.getElementById('seccionLogin').style.display = 'none';
        document.getElementById('seccionChat').style.display = 'flex';

        document.getElementById('tituloChat').innerText = '🔄 Conectando...';

        //Iniciamos conexion STOMP - comunicacion en tiempo real con el servidor
        const socket = new SockJS('/chat-websocket');
        stompClient = Stomp.over(socket); //STOMP es un protocolo de envio y recepcion de mensajes

        stompClient.heartbeat.outgoing = 20000;
        stompClient.heartbeat.incoming = 20000;
        stompClient.connect({}, onConnected, onError);
    }
}

function onConnected() {
    document.getElementById('tituloChat').innerText = 'Sala de Chat Grupo 1';
    //Nos suscribimos para escuchar a todos
    stompClient.subscribe('/topic/publico', onMensajeRecibido);

    //Enviamos el mensaje de bienvenida al servidor usando la estructura de tu Record Java
    stompClient.send("/app/chat.unirse", {}, JSON.stringify({
        id: null,
        contenido: null,
        remitente: nombreUsuario,
        destinatario: null,
        tipo: 'UNIRSE',
        estado: null
    }));
}

function onError(error) {
    document.getElementById('tituloChat').innerText = '⚠️ Sin conexión (Reintentando...)';

    setTimeout(() => {
        const socket = new SockJS('/chat-websocket');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }, 5000);
}

function enviarMensaje() {
    const input = document.getElementById('inputMensaje');
    const mensaje = input.value.trim();

    if(mensaje && stompClient) {
        //Generamos un ID unico en el navegador para este mensaje especifico
        const mensajeId = crypto.randomUUID();

        const chatMessage = {
            id: mensajeId,
            remitente: nombreUsuario,
            contenido: mensaje,
            destinatario: null,
            tipo: 'CHAT',
            estado: 'ENVIADO'
        };
        stompClient.send("/app/chat.enviar", {}, JSON.stringify(chatMessage));
        input.value = ''; //Limpiamos la caja
    }
}

function onMensajeRecibido(payload) {
    //Desempaquetamos lo que envio el servidor Java
    const mensaje = JSON.parse(payload.body);

    if (mensaje.tipo === 'LEIDO') {
        //Buscamos la tilde gris de ese mensaje especifico usando su ID unico
        const checkElemento = document.getElementById(`check-${mensaje.id}`);
        if (checkElemento) {
            checkElemento.innerHTML = '✔✔'; //Lo transformamos en doble visto
            checkElemento.style.color = '#34b7f1'; //Lo pintamos de azul
        }
        return;
    }

    //Analizamos que tipo de mensaje es
    if (mensaje.tipo === 'UNIRSE') {
        chatDiv.innerHTML += `<div class="alerta-sistema"><span>🟢 ${mensaje.remitente} se unió</span></div>`;
    }
    else if (mensaje.tipo === 'SALIR') {
        chatDiv.innerHTML += `<div class="alerta-sistema"><span>🔴 ${mensaje.remitente} salió</span></div>`;
    }
    else if (mensaje.tipo === 'CHAT') {
        let esMio = (mensaje.remitente === nombreUsuario);
        let claseCss = esMio ? "mensaje-mio" : "mensaje-otro";

        let checksHTML = esMio ? `<span id="check-${mensaje.id}" style="color: gray; margin-left: 5px;">✔</span>` : '';

        const burbujaHTML = `
            <div class="burbuja ${claseCss}">
                ${!esMio ? `<div class="autor">${mensaje.remitente}</div>` : ''}
                <div class="texto">${mensaje.contenido}</div>
                <div class="meta-mensaje">
                    ${mensaje.hora || ''} ${checksHTML}
                </div>
            </div>
        `;
        chatDiv.innerHTML += burbujaHTML;

        if (!esMio) {
            const acuseLectura = {
                id: mensaje.id,
                contenido: null,
                remitente: nombreUsuario,
                destinatario: mensaje.remitente,
                tipo: 'LEIDO',
                estado: 'LEIDO'
            };
            //Lo enviamos al backend por el canal general
            stompClient.send("/app/chat.enviar", {}, JSON.stringify(acuseLectura));
        }
    }

    //Auto-scroll al fondo para ver el mensaje mas reciente
    chatDiv.scrollTop = chatDiv.scrollHeight;
}

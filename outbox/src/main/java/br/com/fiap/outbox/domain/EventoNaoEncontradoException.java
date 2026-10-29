package br.com.fiap.outbox.domain;

import java.util.UUID;

public class EventoNaoEncontradoException extends RuntimeException {
    public EventoNaoEncontradoException(UUID eventId) {
        super("Evento " + eventId + " não existe na outbox");
    }
}

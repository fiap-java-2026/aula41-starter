package br.com.fiap.outbox.api;

import br.com.fiap.outbox.domain.OutboxEvent;

import java.util.UUID;

/** Devolve o eventId para a turma reaproveitá-lo no consumidor (Postman guarda em {{eventId}}). */
public record PedidoCriadoResponse(long pedidoId, UUID eventId, String tipo, int versao, boolean enviado) {
    public static PedidoCriadoResponse de(OutboxEvent e) {
        return new PedidoCriadoResponse(e.pedidoId, e.eventId, e.tipo, e.versao, e.enviado);
    }
}

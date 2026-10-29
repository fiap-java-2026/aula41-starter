package br.com.fiap.outbox.relay;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * "Broker" em memória (slide 18: a simulação isola o padrão antes da mensageria real).
 * Não participa da transação do banco — exatamente como um Kafka não participaria.
 * Por isso uma mensagem publicada antes de um rollback continua aqui.
 */
@ApplicationScoped
public class BrokerSimulado {

    public record Mensagem(UUID eventId, long pedidoId, BigDecimal valor, String payload, OffsetDateTime publicadoEm) {}

    private final List<Mensagem> mensagens = new CopyOnWriteArrayList<>();

    public void publicar(Mensagem m) {
        mensagens.add(m);
    }

    public List<Mensagem> mensagens() {
        return List.copyOf(mensagens);
    }

    public void limpar() {
        mensagens.clear();
    }
}

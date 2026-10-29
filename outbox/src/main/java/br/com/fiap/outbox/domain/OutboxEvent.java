package br.com.fiap.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Intenção de publicar, persistida na MESMA transação do pedido (slides 8 e 15).
 * O eventId é gerado uma única vez, aqui: pertence ao evento, não à tentativa de envio (slide 11).
 * tipo, versao e criadoEm são os campos que o desafio (slide 21) pede para completar.
 */
@Entity
@Table(name = "outbox")
public class OutboxEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    public UUID eventId;

    @Column(name = "pedido_id", nullable = false)
    public long pedidoId;

    @Column(nullable = false)
    public String tipo = "PedidoCriado";

    @Column(nullable = false)
    public int versao = 1;

    @Column(nullable = false, columnDefinition = "text")
    public String payload;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(nullable = false)
    public boolean enviado = false;

    @Column(nullable = false)
    public int tentativas = 0;

    @Column(name = "enviado_em")
    public OffsetDateTime enviadoEm;
}

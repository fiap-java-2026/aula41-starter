package br.com.fiap.outbox.consumer;

import br.com.fiap.outbox.domain.FalhaSimuladaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.UUID;

/** Consumidor idempotente: inbox + efeito na mesma transação (slides 10, 11 e 16). */
@ApplicationScoped
public class FaturamentoConsumer {

    public static final String CONSUMIDOR = "faturamento";
    private static final Logger LOG = Logger.getLogger(FaturamentoConsumer.class);

    @Inject
    EntityManager em;

    /**
     * @return true se o efeito foi aplicado agora; false se o eventId já estava na inbox (duplicata).
     * @param falharEfeito quando true, o efeito lança exceção: a linha da inbox reverte junto
     *                     e uma nova tentativa com o mesmo eventId volta a funcionar (slide 22).
     */
    @Transactional
    public boolean consumir(UUID eventId, long pedidoId, BigDecimal valor, boolean falharEfeito) {
        // TODO (desafio): implementar conforme o slide 16: INSERT ... ON CONFLICT DO NOTHING + efeito na mesma transação
        throw new UnsupportedOperationException("slide 16: INSERT ... ON CONFLICT DO NOTHING + efeito na mesma transação");
    }

    private void aplicarEfeito(UUID eventId, long pedidoId, BigDecimal valor, boolean falhar) {
        em.createNativeQuery("""
                INSERT INTO faturamento(pedido_id, event_id, valor)
                VALUES (:pedidoId, :eventId, :valor)
                """)
                .setParameter("pedidoId", pedidoId)
                .setParameter("eventId", eventId)
                .setParameter("valor", valor)
                .executeUpdate();

        if (falhar) {
            throw new FalhaSimuladaException("falha simulada no efeito: inbox e faturamento revertem juntos");
        }
    }
}

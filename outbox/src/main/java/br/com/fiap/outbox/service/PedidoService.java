package br.com.fiap.outbox.service;

import br.com.fiap.outbox.domain.FalhaSimuladaException;
import br.com.fiap.outbox.domain.OutboxEvent;
import br.com.fiap.outbox.domain.Pedido;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/** Produtor: pedido + intenção de publicar no mesmo commit (slide 15). */
@ApplicationScoped
public class PedidoService {

    private static final Logger LOG = Logger.getLogger(PedidoService.class);

    @Inject
    EntityManager entityManager;

    @Inject
    ObjectMapper objectMapper;

    /**
     * @param falhar quando true, lança exceção DEPOIS dos dois persist e ANTES do commit:
     *               a transação reverte pedido e outbox juntos (slide 20, passo 2).
     */
    @Transactional
    public OutboxEvent criar(long clienteId, BigDecimal valor, boolean falhar) {
        // TODO (desafio): implementar conforme o slide 15: pedido + outbox na mesma transação
        throw new UnsupportedOperationException("slide 15: pedido + outbox na mesma transação");
    }

    private String serializar(Pedido pedido) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "pedidoId", pedido.id,
                    "clienteId", pedido.clienteId,
                    "valor", pedido.valor));
        } catch (JsonProcessingException e) {
            // exceção verificada convertida em RuntimeException para provocar rollback (nota do slide 15)
            throw new IllegalStateException("não foi possível serializar o pedido " + pedido.id, e);
        }
    }
}

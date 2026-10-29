package br.com.fiap.outbox.relay;

import br.com.fiap.outbox.domain.FalhaSimuladaException;
import br.com.fiap.outbox.domain.OutboxEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Relay didático de worker único (slide 19): lê enviado=false, publica, marca enviado=true.
 * Disparado por POST /relay/run em vez de @Scheduled, para a turma controlar cada passo.
 */
@ApplicationScoped
public class OutboxRelay {

    private static final Logger LOG = Logger.getLogger(OutboxRelay.class);

    @Inject
    EntityManager em;

    @Inject
    BrokerSimulado broker;

    public record Resultado(int publicados, int marcados) {}

    /**
     * @param falharAntesDeMarcar simula a queda entre "broker confirmou" e "marquei enviado=true"
     *                            (slide 9). As publicações já aconteceram; a marcação reverte.
     *                            Rodar de novo publica os mesmos eventIds: at-least-once ao vivo.
     */
    @Transactional
    public Resultado executar(boolean falharAntesDeMarcar) {
        // TODO (desafio): implementar conforme o slide 19: ler enviado=false, publicar, marcar enviado=true
        throw new UnsupportedOperationException("slide 19: ler enviado=false, publicar, marcar enviado=true");
    }

    @SuppressWarnings("unchecked")
    private BigDecimal valorDoPayload(String payload) {
        try {
            Map<String, Object> m = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);
            return new BigDecimal(String.valueOf(m.get("valor")));
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
}

package br.com.fiap.outbox.api;

import br.com.fiap.outbox.consumer.FaturamentoConsumer;
import br.com.fiap.outbox.domain.EventoNaoEncontradoException;
import br.com.fiap.outbox.domain.FalhaSimuladaException;
import br.com.fiap.outbox.domain.OutboxEvent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * "Broker simulado" do slide 18: entrega o mesmo eventId ao consumidor quantas vezes quisermos.
 * O pedidoId e o valor são lidos da própria linha da outbox, para o Postman só precisar do eventId.
 */
@Path("/simular")
@Produces(MediaType.APPLICATION_JSON)
public class SimulacaoResource {

    @Inject
    FaturamentoConsumer consumer;

    @Inject
    EntityManager em;

    /** Uma entrega. Chame duas vezes: a segunda devolve efeitoAplicado=false. */
    @POST
    @Path("/consumir/{eventId}")
    public Map<String, Object> consumir(@PathParam("eventId") UUID eventId,
                                        @QueryParam("falharEfeito") @DefaultValue("false") boolean falharEfeito) {
        var e = evento(eventId);
        boolean aplicado = consumer.consumir(e.eventId, e.pedidoId, valor(e), falharEfeito);
        return Map.of("eventId", eventId, "pedidoId", e.pedidoId, "efeitoAplicado", aplicado);
    }

    /** N entregas do mesmo eventId (desafio, slide 21: "repita cinco vezes"). */
    @POST
    @Path("/consumir/{eventId}/vezes/{n}")
    public Map<String, Object> consumirVarias(@PathParam("eventId") UUID eventId, @PathParam("n") int n) {
        var e = evento(eventId);
        List<Boolean> resultados = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            resultados.add(consumer.consumir(e.eventId, e.pedidoId, valor(e), false));
        }
        long efeitos = resultados.stream().filter(b -> b).count();
        return Map.of("eventId", eventId, "entregas", n, "efeitosAplicados", efeitos, "resultados", resultados);
    }

    /** Uma entrega cujo efeito falha: nada fica na inbox; a próxima tentativa normal funciona (slide 22). */
    @POST
    @Path("/consumir/{eventId}/falhar")
    public Map<String, Object> consumirComFalha(@PathParam("eventId") UUID eventId) {
        var e = evento(eventId);
        try {
            boolean aplicado = consumer.consumir(e.eventId, e.pedidoId, valor(e), true);
            // só chega aqui se era duplicata (aplicado=false): o efeito nem executou, logo não falhou
            return Map.of("eventId", eventId, "efeitoAplicado", aplicado, "revertido", false,
                    "observacao", "já estava na inbox: efeito não executou");
        } catch (FalhaSimuladaException ex) {
            return Map.of("eventId", eventId, "efeitoAplicado", false, "revertido", true, "mensagem", ex.getMessage());
        }
    }

    private OutboxEvent evento(UUID eventId) {
        var e = em.find(OutboxEvent.class, eventId);
        if (e == null) {
            throw new EventoNaoEncontradoException(eventId);
        }
        return e;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal valor(OutboxEvent e) {
        try {
            Map<String, Object> m = new com.fasterxml.jackson.databind.ObjectMapper().readValue(e.payload, Map.class);
            return new BigDecimal(String.valueOf(m.get("valor")));
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
}

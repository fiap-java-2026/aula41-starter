package br.com.fiap.outbox.api;

import br.com.fiap.outbox.consumer.FaturamentoConsumer;
import br.com.fiap.outbox.relay.BrokerSimulado;
import br.com.fiap.outbox.relay.OutboxRelay;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RelayResource {

    @Inject
    OutboxRelay relay;

    @Inject
    BrokerSimulado broker;

    @Inject
    FaturamentoConsumer consumer;

    /** POST /relay/run?falharAntesDeMarcar=true — queda entre publicar e marcar (slide 9/19). */
    @POST
    @Path("/relay/run")
    public OutboxRelay.Resultado run(@QueryParam("falharAntesDeMarcar") @DefaultValue("false") boolean falhar) {
        return relay.executar(falhar);
    }

    /** O que o "broker" recebeu. Depois de uma falha do relay, o mesmo eventId aparece duas vezes. */
    @GET
    @Path("/broker/mensagens")
    public List<BrokerSimulado.Mensagem> mensagens() {
        return broker.mensagens();
    }

    /** Entrega tudo o que está no broker ao consumidor (cada entrega é uma transação própria). */
    @POST
    @Path("/broker/entregar")
    public Map<String, Object> entregar() {
        var msgs = broker.mensagens();
        long efeitos = 0;
        for (var m : msgs) {
            if (consumer.consumir(m.eventId(), m.pedidoId(), m.valor(), false)) {
                efeitos++;
            }
        }
        return Map.of("entregues", msgs.size(), "efeitosNovos", efeitos,
                "duplicatasIgnoradas", msgs.size() - efeitos);
    }

    @DELETE
    @Path("/broker/mensagens")
    public Map<String, String> limpar() {
        broker.limpar();
        return Map.of("status", "broker limpo");
    }
}

package br.com.fiap.outbox.api;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Evidência (slide 20/22): os COUNT(*) que o README pede, expostos para o Postman
 * conferir sem psql. Em sala, mostre também o SQL equivalente no psql.
 */
@Path("/contagens")
@Produces(MediaType.APPLICATION_JSON)
public class ContagemResource {

    @Inject
    EntityManager em;

    @GET
    public Map<String, Long> contagens() {
        var m = new LinkedHashMap<String, Long>();
        m.put("pedidos", count("select count(*) from pedido"));
        m.put("outbox", count("select count(*) from outbox"));
        m.put("outboxPendentes", count("select count(*) from outbox where enviado = false"));
        m.put("inbox", count("select count(*) from inbox"));
        m.put("faturamento", count("select count(*) from faturamento"));
        return m;
    }

    private long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }
}

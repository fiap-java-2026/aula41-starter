package br.com.fiap.outbox.api;

import br.com.fiap.outbox.domain.Pedido;
import br.com.fiap.outbox.service.PedidoService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PedidoResource {

    @Inject
    PedidoService service;

    @Inject
    EntityManager em;

    /** POST /pedidos?falhar=true provoca a exceção antes do commit (rollback conjunto). */
    @POST
    public Response criar(PedidoRequest req, @QueryParam("falhar") @DefaultValue("false") boolean falhar) {
        var evento = service.criar(req.clienteId(), req.valor(), falhar);
        return Response.status(Response.Status.CREATED).entity(PedidoCriadoResponse.de(evento)).build();
    }

    @GET
    @Path("/{id}")
    public Pedido buscar(@PathParam("id") long id) {
        var p = em.find(Pedido.class, id);
        if (p == null) {
            throw new NotFoundException("Pedido " + id + " não existe");
        }
        return p;
    }
}

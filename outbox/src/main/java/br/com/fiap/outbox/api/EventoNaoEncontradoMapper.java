package br.com.fiap.outbox.api;

import br.com.fiap.outbox.domain.EventoNaoEncontradoException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EventoNaoEncontradoMapper implements ExceptionMapper<EventoNaoEncontradoException> {
    @Override
    public Response toResponse(EventoNaoEncontradoException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResponse("evento-nao-encontrado", e.getMessage()))
                .build();
    }
}

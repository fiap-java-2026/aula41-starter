package br.com.fiap.outbox.api;

import br.com.fiap.outbox.domain.FalhaSimuladaException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Mesmo molde do ExceptionMapper do vídeo (23:30) e da Aula 40: corpo estável em vez de stack trace. */
@Provider
public class FalhaSimuladaMapper implements ExceptionMapper<FalhaSimuladaException> {
    @Override
    public Response toResponse(FalhaSimuladaException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResponse("falha-simulada", e.getMessage()))
                .build();
    }
}

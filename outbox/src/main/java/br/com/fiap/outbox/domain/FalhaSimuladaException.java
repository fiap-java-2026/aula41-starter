package br.com.fiap.outbox.domain;

/** Falha provocada de propósito na demo (?falhar=true). RuntimeException => rollback (JTA). */
public class FalhaSimuladaException extends RuntimeException {
    public FalhaSimuladaException(String mensagem) {
        super(mensagem);
    }
}

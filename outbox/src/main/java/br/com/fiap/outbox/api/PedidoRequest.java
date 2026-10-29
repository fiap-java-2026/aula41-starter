package br.com.fiap.outbox.api;

import java.math.BigDecimal;

public record PedidoRequest(long clienteId, BigDecimal valor) {}

# Aula 41 — Transactional Outbox, Inbox e idempotência (Quarkus)

Formação Backend Java e Quarkus · Turma 2 · 29/10/2026

Projeto Quarkus **3.17.5** · Java 21 · extensões `rest-jackson`, `hibernate-orm`, `jdbc-postgresql`
(+ `smallrye-openapi` para o Swagger UI). Sem Kafka, sem Debezium: o broker é simulado em memória.

## Como obter

No repositório GitHub da aula: **Code → Download ZIP** (ou `git clone`). Descompacte e entre na pasta do pacote
que o professor indicou — `starter-aula41/outbox` para o desafio (três métodos em TODO, 7 testes vermelhos) ou
`solucao-aula41/outbox` para comparar com a sua solução depois da correção. Só esses três arquivos diferem entre os dois:
`service/PedidoService.java`, `consumer/FaturamentoConsumer.java` e `relay/OutboxRelay.java`.

Pré-requisitos: Java 21, Docker Desktop no ar (o Dev Services sobe o PostgreSQL sozinho) e Postman
(importe os dois arquivos de `../postman/` e selecione o ambiente **FIAP Aula 41 — local**).

## Subir

```bash
cd starter-aula41/outbox    # (Windows: .\mvnw.cmd em vez de ./mvnw)
./mvnw quarkus:dev          # Dev Services sobe um PostgreSQL em container (precisa de Docker)
```

Primeira execução: o Maven baixa as dependências (alguns minutos). Esperado no fim: `Listening on: http://0.0.0.0:4100`.

Se já houver um PostgreSQL disponível, exporte antes (ou use o docker-compose do repositório):

```bash
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/fiap
export QUARKUS_DATASOURCE_USERNAME=fiap QUARKUS_DATASOURCE_PASSWORD=fiap
```

Swagger UI: http://localhost:4100/q/swagger-ui · Dev UI: http://localhost:4100/q/dev

## Endpoints

| Método | Caminho | O que faz |
|---|---|---|
| POST | `/pedidos` `{clienteId, valor}` | pedido + linha de outbox no **mesmo** commit; `?falhar=true` lança exceção antes do commit |
| GET | `/pedidos/{id}` | consulta |
| POST | `/relay/run` | worker único: lê `enviado=false`, "publica" no broker simulado, marca `enviado=true`; `?falharAntesDeMarcar=true` cai entre publicar e marcar |
| GET | `/broker/mensagens` | o que o broker recebeu (duplicatas aparecem aqui) |
| POST | `/broker/entregar` | entrega tudo do broker ao consumidor, uma transação por entrega |
| POST | `/simular/consumir/{eventId}` | uma entrega ao consumidor; `?falharEfeito=true` faz o efeito falhar |
| POST | `/simular/consumir/{eventId}/vezes/{n}` | n entregas do mesmo eventId |
| POST | `/simular/consumir/{eventId}/falhar` | entrega cujo efeito falha e reverte a inbox |
| GET | `/contagens` | `pedidos`, `outbox`, `outboxPendentes`, `inbox`, `faturamento` |

## Roteiro da demonstração (slide 20)

1. `POST /pedidos` → `GET /contagens`: +1 pedido, +1 outbox (`enviado=false`).
2. `POST /pedidos?falhar=true` → 500 → `GET /contagens`: nada mudou (rollback conjunto).
3. `POST /relay/run?falharAntesDeMarcar=true` → 500; `POST /relay/run` → publica de novo; `GET /broker/mensagens` mostra o **mesmo eventId duas vezes** (at-least-once).
4. `POST /simular/consumir/{eventId}` duas vezes → `efeitoAplicado` true, depois false → `GET /contagens`: +1 inbox, +1 faturamento.
5. `POST /simular/consumir/{eventId}/falhar` → inbox e faturamento não mudam; a próxima entrega normal funciona.
6. `POST /simular/consumir/{eventId}/vezes/5` → 5 entregas, 1 efeito.

A coleção Postman em `../postman/` faz tudo isso com asserções.

## SQL de conferência (psql)

```sql
SELECT count(*) AS pedidos     FROM pedido;
SELECT count(*) AS outbox, count(*) FILTER (WHERE NOT enviado) AS pendentes FROM outbox;
SELECT count(*) AS inbox       FROM inbox;
SELECT count(*) AS faturamento FROM faturamento;

-- a mesma linha nas três tabelas, pelo event_id
SELECT o.event_id, o.pedido_id, o.tipo, o.versao, o.enviado, o.tentativas,
       i.recebido_em                       AS inbox_em,
       (SELECT count(*) FROM faturamento f WHERE f.event_id = o.event_id) AS efeitos
FROM outbox o
LEFT JOIN inbox i ON i.event_id = o.event_id AND i.consumidor = 'faturamento'
ORDER BY o.criado_em;
```

Critério de aceite (slide 22): `efeitos` nunca passa de 1 por `event_id`, mesmo depois de cinco entregas;
`pedidos = outbox` sempre; depois de um `?falhar=true`, as duas contagens não mudam.

## Testes

```bash
./mvnw test        # 7 testes em OutboxInboxTest, um por passo do roteiro
```

## Estrutura

```
br.com.fiap.outbox
├── domain     Pedido, OutboxEvent (entidades JPA), exceções
├── service    PedidoService.criar()            ← slide 15
├── consumer   FaturamentoConsumer.consumir()   ← slide 16
├── relay      OutboxRelay, BrokerSimulado      ← slide 19
└── api        recursos REST + ExceptionMappers
src/main/resources/import.sql                   ← inbox e faturamento (slide 14)
```

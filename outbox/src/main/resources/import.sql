-- Executado pelo Hibernate depois do drop-and-create (quarkus.hibernate-orm.sql-load-script).
-- As tabelas pedido e outbox vêm das entidades JPA; estas duas são "do consumidor" (slide 14).

DROP TABLE IF EXISTS faturamento;
DROP TABLE IF EXISTS inbox;

-- Inbox: a chave única (consumidor, event_id) decide a corrida (slides 10 e 11)
CREATE TABLE inbox (
    consumidor   text NOT NULL,
    event_id     uuid NOT NULL,
    recebido_em  timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (consumidor, event_id)
);

-- Efeito de negócio do consumidor (slide 21: "tabela de efeitos de faturamento")
CREATE TABLE faturamento (
    id         bigserial PRIMARY KEY,
    pedido_id  bigint NOT NULL,
    event_id   uuid NOT NULL,
    valor      numeric(12,2) NOT NULL,
    criado_em  timestamptz NOT NULL DEFAULT now()
);

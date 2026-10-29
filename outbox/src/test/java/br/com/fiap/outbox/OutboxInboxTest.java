package br.com.fiap.outbox;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Os sete testes do roteiro (slide 20 + critérios do slide 22), pela API.
 * Precisa de PostgreSQL: Dev Services (Docker) ou QUARKUS_DATASOURCE_JDBC_URL do docker-compose.
 */
@QuarkusTest
class OutboxInboxTest {

    @SuppressWarnings("unchecked")
    private Map<String, Integer> contagens() {
        return given().get("/contagens").then().statusCode(200).extract().as(Map.class);
    }

    private String criarPedido() {
        return given().contentType(ContentType.JSON)
                .body("{\"clienteId\": 42, \"valor\": 150.00}")
                .post("/pedidos")
                .then().statusCode(201)
                .body("eventId", notNullValue())
                .body("enviado", is(false))
                .extract().path("eventId");
    }

    @Test
    @DisplayName("1. criar pedido grava pedido e outbox no mesmo commit")
    void criarGravaPedidoEOutbox() {
        var antes = contagens();
        criarPedido();
        var depois = contagens();
        assertEquals(antes.get("pedidos") + 1, (int) depois.get("pedidos"));
        assertEquals(antes.get("outbox") + 1, (int) depois.get("outbox"));
    }

    @Test
    @DisplayName("2. falha antes do commit deixa zero pedido e zero outbox novos")
    void rollbackConjunto() {
        var antes = contagens();
        given().contentType(ContentType.JSON)
                .body("{\"clienteId\": 42, \"valor\": 150.00}")
                .queryParam("falhar", true)
                .post("/pedidos")
                .then().statusCode(500)
                .body("erro", equalTo("falha-simulada"));
        var depois = contagens();
        assertEquals(antes.get("pedidos"), depois.get("pedidos"));
        assertEquals(antes.get("outbox"), depois.get("outbox"));
    }

    @Test
    @DisplayName("3. relay marca enviado=true; segunda execução não publica nada")
    void relayMarcaEnviado() {
        criarPedido();
        given().post("/relay/run").then().statusCode(200);
        assertEquals(0, (int) contagens().get("outboxPendentes"));
        given().post("/relay/run").then().statusCode(200).body("publicados", is(0));
    }

    @Test
    @DisplayName("4. relay que cai antes de marcar publica de novo na próxima execução (at-least-once)")
    void relayQueCaiRepublica() {
        given().delete("/broker/mensagens").then().statusCode(200);
        given().post("/relay/run").then().statusCode(200);          // zera pendências anteriores
        given().delete("/broker/mensagens").then().statusCode(200);
        String eventId = criarPedido();

        given().queryParam("falharAntesDeMarcar", true).post("/relay/run")
                .then().statusCode(500).body("erro", equalTo("falha-simulada"));
        given().post("/relay/run").then().statusCode(200).body("publicados", is(1));

        long vezes = given().get("/broker/mensagens").then().statusCode(200)
                .extract().jsonPath().getList("eventId").stream().filter(eventId::equals).count();
        assertEquals(2, vezes, "o mesmo eventId chegou duas vezes ao broker");
    }

    @Test
    @DisplayName("5. consumir duas vezes o mesmo eventId gera uma inbox e um efeito")
    void consumirDuasVezesUmEfeito() {
        String eventId = criarPedido();
        var antes = contagens();
        given().post("/simular/consumir/" + eventId).then().statusCode(200).body("efeitoAplicado", is(true));
        given().post("/simular/consumir/" + eventId).then().statusCode(200).body("efeitoAplicado", is(false));
        var depois = contagens();
        assertEquals(antes.get("inbox") + 1, (int) depois.get("inbox"));
        assertEquals(antes.get("faturamento") + 1, (int) depois.get("faturamento"));
    }

    @Test
    @DisplayName("6. efeito que falha reverte a inbox; nova tentativa com o mesmo eventId funciona")
    void efeitoQueFalhaRevertInbox() {
        String eventId = criarPedido();
        var antes = contagens();
        given().post("/simular/consumir/" + eventId + "/falhar").then().statusCode(200).body("revertido", is(true));
        assertEquals(antes.get("inbox"), contagens().get("inbox"));
        assertEquals(antes.get("faturamento"), contagens().get("faturamento"));
        given().post("/simular/consumir/" + eventId).then().statusCode(200).body("efeitoAplicado", is(true));
        assertEquals(antes.get("faturamento") + 1, (int) contagens().get("faturamento"));
    }

    @Test
    @DisplayName("7. cinco entregas do mesmo evento geram um efeito de faturamento")
    void cincoEntregasUmEfeito() {
        String eventId = criarPedido();
        var antes = contagens();
        given().post("/simular/consumir/" + eventId + "/vezes/5").then().statusCode(200)
                .body("entregas", is(5)).body("efeitosAplicados", is(1));
        assertEquals(antes.get("faturamento") + 1, (int) contagens().get("faturamento"));
    }
}

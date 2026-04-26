package com.remitly.assignment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuditLogApiTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnOnlySuccessfulWalletOperationsInOrder() {
        RestClient client = createClient();
        String successfulWalletId = "audit_wallet_success";
        String failedWalletId = "audit_wallet_failed";

        setBankState(client, """
                {
                    "stocks": [
                        {"name": "AAPL", "quantity": 1.0}
                    ]
                }
                """);

        assertThatThrownBy(() -> executeOperation(client, successfulWalletId, "AAPL", "sell"))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);

        ResponseEntity<Void> buyResponse = executeOperation(client, successfulWalletId, "AAPL", "buy");
        assertThat(buyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThatThrownBy(() -> executeOperation(client, failedWalletId, "AAPL", "buy"))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);

        ResponseEntity<Void> sellResponse = executeOperation(client, successfulWalletId, "AAPL", "sell");
        assertThat(sellResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> logResponse = client.get()
                .uri("/log")
                .retrieve()
                .toEntity(String.class);

        assertThat(logResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logResponse.getBody()).contains("\"type\":\"buy\"", "\"wallet_id\":\"audit_wallet_success\"", "\"stock_name\":\"AAPL\"");
        assertThat(logResponse.getBody()).contains("\"type\":\"sell\"", "\"wallet_id\":\"audit_wallet_success\"", "\"stock_name\":\"AAPL\"");
        assertThat(logResponse.getBody()).doesNotContain("\"wallet_id\":\"audit_wallet_failed\"");

        int buyIndex = logResponse.getBody().indexOf("\"type\":\"buy\"");
        int sellIndex = logResponse.getBody().indexOf("\"type\":\"sell\"");
        assertThat(buyIndex).isLessThan(sellIndex);
    }

    private RestClient createClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private ResponseEntity<Void> executeOperation(RestClient client, String walletId, String stockName, String type) {
        return client.post()
                .uri("/wallets/{walletId}/stocks/{stockName}", walletId, stockName)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "type": "%s"
                        }
                        """.formatted(type))
                .retrieve()
                .toBodilessEntity();
    }

    private void setBankState(RestClient client, String requestBody) {
        ResponseEntity<Void> response = client.post()
                .uri("/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

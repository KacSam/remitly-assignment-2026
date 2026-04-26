package com.remitly.assignment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankApiTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldSetAndGetBankState() {

        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        String postBody = """
                {
                    "stocks": [
                        {"name": "AAPL", "quantity": 150.0},
                        {"name": "GOOGL", "quantity": 2800.0}
                    ]
                }
                """;
        ResponseEntity<Void> postResponse = client.post()
            .uri("/stocks")
            .contentType(MediaType.APPLICATION_JSON)
            .body(postBody)
            .retrieve()
            .toBodilessEntity();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = client.get()
            .uri("/stocks")
            .retrieve()
            .toEntity(String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).contains("AAPL", "150.0", "GOOGL", "2800.0");
    }

}
package com.remitly.assignment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "chaos.shutdown-enabled=false")
class ChaosApiTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldReturn200WhenChaosIsTriggered() {
        RestClient client = createClient();

        ResponseEntity<Void> response = client.post()
                .uri("/chaos")
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private RestClient createClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }
}

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
class WalletApiTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldBuyStockCreateWalletAndUpdateBank() {
        RestClient client = createClient();

        setBankState(client, """
                {
                    "stocks": [
                        {"name": "AAPL", "quantity": 2.0}
                    ]
                }
                """);

        ResponseEntity<Void> buyResponse = client.post()
                .uri("/wallets/w1/stocks/AAPL")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "type": "buy"
                        }
                        """)
                .retrieve()
                .toBodilessEntity();

        assertThat(buyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> walletResponse = client.get()
                .uri("/wallets/w1")
                .retrieve()
                .toEntity(String.class);

        assertThat(walletResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(walletResponse.getBody()).contains("w1", "AAPL", "1.0");

        ResponseEntity<String> walletStockResponse = client.get()
                .uri("/wallets/w1/stocks/AAPL")
                .retrieve()
                .toEntity(String.class);

        assertThat(walletStockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(walletStockResponse.getBody()).isEqualTo("1.0");

        ResponseEntity<String> bankResponse = client.get()
                .uri("/stocks")
                .retrieve()
                .toEntity(String.class);

        assertThat(bankResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bankResponse.getBody()).contains("AAPL", "1.0");
    }

    @Test
    void shouldReturn404WhenBuyingUnknownStock() {
        RestClient client = createClient();

        setBankState(client, """
                {
                    "stocks": [
                        {"name": "AAPL", "quantity": 2.0}
                    ]
                }
                """);

        assertThatThrownBy(() -> client.post()
                .uri("/wallets/w2/stocks/MSFT")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "type": "buy"
                        }
                        """)
                .retrieve()
                .toBodilessEntity()).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void shouldReturn400WhenBuyingStockThatIsUnavailableInBank() {
        RestClient client = createClient();

        setBankState(client, """
                {
                    "stocks": [
                        {"name": "AAPL", "quantity": 0.0}
                    ]
                }
                """);

        assertThatThrownBy(() -> client.post()
                .uri("/wallets/w3/stocks/AAPL")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "type": "buy"
                        }
                        """)
                .retrieve()
                .toBodilessEntity()).isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void shouldReturn400WhenSellingStockNotOwnedByWallet() {
        RestClient client = createClient();

        setBankState(client, """
                {
                    "stocks": [
                        {"name": "AAPL", "quantity": 2.0}
                    ]
                }
                """);

        assertThatThrownBy(() -> client.post()
                .uri("/wallets/w4/stocks/AAPL")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "type": "sell"
                        }
                        """)
                .retrieve()
                .toBodilessEntity()).isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void shouldSellStockFromWalletAndIncreaseBankQuantity() {
        RestClient client = createClient();

        setBankState(client, """
                {
                    "stocks": [
                        {"name": "AAPL", "quantity": 1.0}
                    ]
                }
                """);

        ResponseEntity<Void> buyResponse = client.post()
                .uri("/wallets/w5/stocks/AAPL")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "type": "buy"
                        }
                        """)
                .retrieve()
                .toBodilessEntity();

        assertThat(buyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> sellResponse = client.post()
                .uri("/wallets/w5/stocks/AAPL")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "type": "sell"
                        }
                        """)
                .retrieve()
                .toBodilessEntity();

        assertThat(sellResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> walletStockResponse = client.get()
                .uri("/wallets/w5/stocks/AAPL")
                .retrieve()
                .toEntity(String.class);

        assertThat(walletStockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(walletStockResponse.getBody()).isEqualTo("0.0");

        ResponseEntity<String> bankResponse = client.get()
                .uri("/stocks")
                .retrieve()
                .toEntity(String.class);

        assertThat(bankResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bankResponse.getBody()).contains("AAPL", "1.0");
    }

    private RestClient createClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private void setBankState(RestClient client, String requestBody) {
        ResponseEntity<Void> setBankResponse = client.post()
                .uri("/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();

        assertThat(setBankResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

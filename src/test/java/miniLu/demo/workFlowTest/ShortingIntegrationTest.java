package miniLu.demo.workFlowTest;

import miniLu.demo.MiniLuiApplication;
import miniLu.demo.dto.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MiniLuiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShortingIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void createShortLink_ShouldReturnSuccess() {
        Link requestLink = new Link();
        requestLink.setLink("https://www.spring.io");

        ResponseEntity<String> postResponse = restClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestLink)
                .retrieve()
                .toEntity(String.class);

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void redirectToShortLink_ShouldRedirect() {
        // Сначала создаем короткую ссылку
        Link requestLink = new Link();
        requestLink.setLink("https://www.google.com");

        ResponseEntity<String> createResponse = restClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestLink)
                .retrieve()
                .toEntity(String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Потом пробуем перейти по короткой ссылке
        // (замените "test123" на реальную короткую ссылку из ответа)
        ResponseEntity<String> redirectResponse = restClient.get()
                .uri("/test123")
                .retrieve()
                .toEntity(String.class);

        assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void homePage_ShouldReturnIndexPage() {
        ResponseEntity<String> response = restClient.get()
                .uri("/")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void authPage_ShouldReturnAuthPage() {
        ResponseEntity<String> response = restClient.get()
                .uri("/auth")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
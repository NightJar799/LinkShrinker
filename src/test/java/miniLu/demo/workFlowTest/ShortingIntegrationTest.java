package miniLu.demo.workFlowTest;

import miniLu.demo.MiniLuiApplication;
import miniLu.demo.dto.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MiniLuiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShortingIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void fFCreateShortLinkAndRedirect() {
        Link requestLink = new Link();
        requestLink.setLink("https://www.spring.io");

        ResponseEntity<String> postResponse = restClient.post()
                .uri("/")
                .body(requestLink)
                .retrieve()
                .toEntity(String.class);
        
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
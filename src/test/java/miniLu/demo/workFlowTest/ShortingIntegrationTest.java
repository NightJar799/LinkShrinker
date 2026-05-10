package miniLu.demo.workFlowTest;

import miniLu.demo.MiniLuiApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MiniLuiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShortingIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private RestTemplate createNoRedirectRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        return new RestTemplate(factory);
    }

    @Test
    void authPage_ShouldReturnAuthPage() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "/auth", String.class);

        System.out.println("Auth page status: " + response.getStatusCode());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void regPage_ShouldReturnRegPage() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "/reg", String.class);

        System.out.println("Reg page status: " + response.getStatusCode());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void homePage_WithoutAuth_ShouldRedirectToLogin() {
        RestTemplate noRedirectRestTemplate = createNoRedirectRestTemplate();
        
        ResponseEntity<String> response = noRedirectRestTemplate.getForEntity(
            getBaseUrl() + "/", String.class);

        System.out.println("Home page status (no auth): " + response.getStatusCode());
        System.out.println("Home page headers: " + response.getHeaders());

        assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
    }

    @Test
    void fullWorkflow_RegisterLoginCreateLinkAndRedirect() {
        RestTemplate noRedirectRestTemplate = createNoRedirectRestTemplate();

        ResponseEntity<String> regPageResponse = noRedirectRestTemplate.getForEntity(
            getBaseUrl() + "/reg", String.class);

        System.out.println("=== REG PAGE ===");
        System.out.println("Status: " + regPageResponse.getStatusCode());
        System.out.println("Headers: " + regPageResponse.getHeaders());
        
        String htmlBody = regPageResponse.getBody();
        if (htmlBody != null) {
            int csrfIndex = htmlBody.indexOf("_csrf");
            if (csrfIndex >= 0) {
                System.out.println("CSRF found at index: " + csrfIndex);
                System.out.println("Context: " + htmlBody.substring(Math.max(0, csrfIndex - 50), 
                    Math.min(htmlBody.length(), csrfIndex + 150)));
            } else {
                System.out.println("CSRF NOT FOUND in HTML");
                System.out.println("First 1000 chars: " + htmlBody.substring(0, Math.min(1000, htmlBody.length())));
            }
        }

        String csrfToken = extractCsrfToken(regPageResponse.getBody());
        String jsessionid = extractJSessionId(regPageResponse.getHeaders().get(HttpHeaders.SET_COOKIE));

        System.out.println("CSRF Token: " + csrfToken);
        System.out.println("JSESSIONID: " + jsessionid);

        if (csrfToken == null && regPageResponse.getStatusCode() == HttpStatus.OK) {
            System.out.println("WARNING: CSRF token is null but page returned 200 OK");
            System.out.println("Full HTML: " + htmlBody);
        }

        assertThat(csrfToken).as("CSRF token should not be null").isNotNull();

        HttpHeaders regHeaders = new HttpHeaders();
        regHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        regHeaders.add(HttpHeaders.COOKIE, "JSESSIONID=" + jsessionid);

        MultiValueMap<String, String> regFormData = new LinkedMultiValueMap<>();
        regFormData.add("_csrf", csrfToken);
        regFormData.add("email", "workflow@test.com");
        regFormData.add("password", "password123");
        regFormData.add("passwordCheck", "password123");
        regFormData.add("name", "Workflow User");

        HttpEntity<MultiValueMap<String, String>> regRequest = new HttpEntity<>(regFormData, regHeaders);
        ResponseEntity<String> regResponse = noRedirectRestTemplate.postForEntity(
            getBaseUrl() + "/reg", regRequest, String.class);

        System.out.println("=== REG RESPONSE ===");
        System.out.println("Status: " + regResponse.getStatusCode());
        System.out.println("Location: " + regResponse.getHeaders().getLocation());
        System.out.println("Set-Cookie: " + regResponse.getHeaders().get(HttpHeaders.SET_COOKIE));

        assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(regResponse.getHeaders().getLocation()).isNotNull();
        assertThat(regResponse.getHeaders().getLocation().getPath()).isEqualTo("/auth");

        String authJSessionId = extractJSessionId(regResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
        if (authJSessionId == null) {
            authJSessionId = jsessionid;
        }
        System.out.println("Auth JSESSIONID: " + authJSessionId);

        HttpHeaders authPageHeaders = new HttpHeaders();
        authPageHeaders.add(HttpHeaders.COOKIE, "JSESSIONID=" + authJSessionId);
        HttpEntity<Void> authPageRequest = new HttpEntity<>(authPageHeaders);

        ResponseEntity<String> authPageResponse = noRedirectRestTemplate.exchange(
            getBaseUrl() + "/auth", HttpMethod.GET, authPageRequest, String.class);

        System.out.println("=== AUTH PAGE ===");
        System.out.println("Status: " + authPageResponse.getStatusCode());

        String loginCsrfToken = extractCsrfToken(authPageResponse.getBody());
        if (loginCsrfToken == null) {
            loginCsrfToken = csrfToken;
        }
        System.out.println("Login CSRF Token: " + loginCsrfToken);

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        loginHeaders.add(HttpHeaders.COOKIE, "JSESSIONID=" + authJSessionId);

        MultiValueMap<String, String> loginFormData = new LinkedMultiValueMap<>();
        loginFormData.add("_csrf", loginCsrfToken);
        loginFormData.add("email", "workflow@test.com");
        loginFormData.add("password", "password123");

        HttpEntity<MultiValueMap<String, String>> loginRequest = new HttpEntity<>(loginFormData, loginHeaders);
        ResponseEntity<String> loginResponse = noRedirectRestTemplate.postForEntity(
            getBaseUrl() + "/auth", loginRequest, String.class);

        System.out.println("=== LOGIN RESPONSE ===");
        System.out.println("Status: " + loginResponse.getStatusCode());
        System.out.println("Location: " + loginResponse.getHeaders().getLocation());

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(loginResponse.getHeaders().getLocation()).isNotNull();

        String loggedInJSessionId = extractJSessionId(loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
        if (loggedInJSessionId == null) {
            loggedInJSessionId = authJSessionId;
        }
        System.out.println("Logged in JSESSIONID: " + loggedInJSessionId);

        HttpHeaders homeHeaders = new HttpHeaders();
        homeHeaders.add(HttpHeaders.COOKIE, "JSESSIONID=" + loggedInJSessionId);
        HttpEntity<Void> homeRequest = new HttpEntity<>(homeHeaders);

        ResponseEntity<String> homeResponse = noRedirectRestTemplate.exchange(
            getBaseUrl() + "/", HttpMethod.GET, homeRequest, String.class);

        System.out.println("=== HOME PAGE ===");
        System.out.println("Status: " + homeResponse.getStatusCode());

        String homeCsrfToken = extractCsrfToken(homeResponse.getBody());
        if (homeCsrfToken == null) {
            homeCsrfToken = loginCsrfToken;
        }
        System.out.println("Home CSRF Token: " + homeCsrfToken);

        HttpHeaders createLinkHeaders = new HttpHeaders();
        createLinkHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        createLinkHeaders.add(HttpHeaders.COOKIE, "JSESSIONID=" + loggedInJSessionId);

        MultiValueMap<String, String> linkFormData = new LinkedMultiValueMap<>();
        linkFormData.add("_csrf", homeCsrfToken);
        linkFormData.add("link", "https://www.github.com");

        HttpEntity<MultiValueMap<String, String>> linkRequest = new HttpEntity<>(linkFormData, createLinkHeaders);
        ResponseEntity<String> createLinkResponse = noRedirectRestTemplate.postForEntity(
            getBaseUrl() + "/", linkRequest, String.class);

        System.out.println("=== CREATE LINK ===");
        System.out.println("Status: " + createLinkResponse.getStatusCode());

        ResponseEntity<String> redirectResponse = noRedirectRestTemplate.getForEntity(
            getBaseUrl() + "/0", String.class);

        System.out.println("=== REDIRECT ===");
        System.out.println("Status: " + redirectResponse.getStatusCode());
        System.out.println("Location: " + redirectResponse.getHeaders().getLocation());

        assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    }

    private String extractCsrfToken(String htmlBody) {
        if (htmlBody == null) return null;

        String[] patterns = {
            "name=\"_csrf\"[^>]*value=\"([^\"]+)\"",
            "name=\"_csrf\"[^>]*content=\"([^\"]+)\"",
            "\"_csrf\"[^>]*value=\"([^\"]+)\"",
            "csrf.*?value=\"([^\"]+)\"",
            "name=\"_csrf\".*?value=\"([^\"]+)\""
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(htmlBody);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    private String extractJSessionId(List<String> cookies) {
        if (cookies == null) return null;

        for (String cookie : cookies) {
            if (cookie.contains("JSESSIONID=")) {
                Pattern pattern = Pattern.compile("JSESSIONID=([^;]+)");
                Matcher matcher = pattern.matcher(cookie);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }
}
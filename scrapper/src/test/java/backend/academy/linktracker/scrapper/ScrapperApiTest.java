package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.commondto.dto.AddLink;
import backend.academy.linktracker.commondto.dto.Link;
import backend.academy.linktracker.commondto.dto.ListLinks;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestPropertySource(properties = {"STACKOVERFLOW_KEY=mock", "app.access-type=in-memory"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScrapperApiTest {

    static PostgreSQLContainer postres = new PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("botdb")
            .withUsername("botuser")
            .withPassword("botpass");

    static {
        postres.start();
    }

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private ScrapperRepository scrapperRepository;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postres::getJdbcUrl);
        registry.add("spring.datatource.username", postres::getUsername);
        registry.add("spring.datasource.password", postres::getPassword);
    }

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @AfterEach
    void cleanUp() {
        scrapperRepository.deleteChat(1);
    }

    private ResponseEntity<Void> registerChat() {
        return restClient.post().uri("/tg-chat/1").retrieve().toBodilessEntity();
    }

    private ResponseEntity<Link> addLink(AddLink addLink) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", "1")
                .body(addLink)
                .retrieve()
                .toEntity(Link.class);
    }

    private ResponseEntity<Link> deleteLink(AddLink addLink) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", "1")
                .body(addLink)
                .retrieve()
                .toEntity(Link.class);
    }

    private ResponseEntity<ListLinks> getLinks() {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", "1")
                .retrieve()
                .toEntity(ListLinks.class);
    }

    private ResponseEntity<Void> deleteChat() {
        return restClient.delete().uri("/tg-chat/{chatId}", 1).retrieve().toBodilessEntity();
    }

    @Test
    void addAndGetLink() {
        ResponseEntity<Void> registerResponse = registerChat();
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        AddLink addLink = new AddLink(URI.create("https://github.com/user/repo"), List.of());

        ResponseEntity<Link> addResponse = addLink(addLink);
        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(addResponse.getBody()).isNotNull();
        assertThat(addResponse.getBody().url()).isEqualTo(URI.create("https://github.com/user/repo"));

        ResponseEntity<ListLinks> getResponse = getLinks();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().size()).isEqualTo(1);
        assertThat(getResponse.getBody().links().size()).isEqualTo(1);
        assertThat(getResponse.getBody().links().getFirst().url())
                .isEqualTo(URI.create("https://github.com/user/repo"));
    }

    @Test
    void addAndDeleteLink() {
        registerChat();

        AddLink addLink = new AddLink(URI.create("https://github.com/user/repo"), List.of());

        ResponseEntity<Link> addResponse = addLink(addLink);
        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(addResponse.getBody()).isNotNull();
        assertThat(addResponse.getBody().url()).isEqualTo(addLink.link());

        ResponseEntity<Link> deleteResponse = deleteLink(addLink);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).isNotNull();
        assertThat(deleteResponse.getBody().url()).isEqualTo(addLink.link());

        ResponseEntity<ListLinks> listResponse = getLinks();
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().size()).isZero();
        assertThat(listResponse.getBody().links().size()).isZero();
    }

    @Test
    void deleteLinkFromNonExistingChat() {
        registerChat();

        AddLink addLink = new AddLink(URI.create("https://github.com/user/repo"), List.of());
        addLink(addLink);

        ResponseEntity<Void> deleteResponse = restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", "999")
                .body(addLink)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                .toBodilessEntity();
        assertThat(deleteResponse.getStatusCode().is4xxClientError()).isTrue();

        ResponseEntity<ListLinks> listResponse = getLinks();
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().size()).isEqualTo(1);
        assertThat(listResponse.getBody().links().getFirst().url())
                .isEqualTo(URI.create("https://github.com/user/repo"));
    }

    @Test
    void addLinkToNonExistingChat() {
        registerChat();
        AddLink addLink = new AddLink(URI.create("https://github.com/user/repo"), List.of());

        ResponseEntity<Void> addResponse = restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", "999")
                .body(addLink)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                .toBodilessEntity();
        assertThat(addResponse.getStatusCode().is4xxClientError()).isTrue();

        ResponseEntity<ListLinks> listResponse = getLinks();
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().size()).isZero();
    }

    @Test
    void operationsAfterChatDeletion() {
        registerChat();

        ResponseEntity<Void> delChatResponse = deleteChat();
        assertThat(delChatResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        AddLink addLink = new AddLink(URI.create("https://github.com/user/repo"), List.of());

        ResponseEntity<Void> addResponse = restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", "1")
                .body(addLink)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                .toBodilessEntity();
        assertThat(addResponse.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void deleteNonExistingChat() {
        ResponseEntity<Void> deleteResponse = restClient
                .delete()
                .uri("/tg-chat/{chatId}", 1)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                .toBodilessEntity();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

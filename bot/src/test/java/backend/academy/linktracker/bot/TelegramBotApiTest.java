package backend.academy.linktracker.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TelegramBotApiTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @MockitoBean
    private BotRepository botRepository;

    @MockitoBean
    private TelegramBot telegramBot;

    private final long chatId = 123;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        when(botRepository.isPresent(chatId)).thenReturn(true);
    }

    @Test
    void validUpdate_shouldReturn200() {
        LinkUpdate linkUpdate =
                new LinkUpdate(chatId, "https://github.com/user/repo", "Изменение в репозитории", List.of(123L));

        ResponseEntity<Void> response =
                restClient.post().uri("/updates").body(linkUpdate).retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("requestProvider")
    void invalidUpdate_shouldNotReturn200(Map<String, Objects> body) {
        ResponseEntity<Void> response = restClient
                .post()
                .uri("/updates")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    static Stream<Arguments> requestProvider() {
        return Stream.of(
                Arguments.of(Map.of(
                        "id",
                        123,
                        "url",
                        "https://github.com/user/repo",
                        "description",
                        "",
                        "tgChatIds",
                        List.of(456L))),
                Arguments.of(Map.of(
                        "id",
                        "word",
                        "url",
                        "https://github.com/user/repo",
                        "description",
                        "",
                        "tgChatIds",
                        List.of(456L))),
                Arguments.of(Map.of("id", 123, "url", "qwerty", "description", "", "tgChatIds", List.of(456L))),
                Arguments.of(Map.of(
                        "id",
                        123,
                        "url",
                        "https://github.com/user/repo",
                        "description",
                        "",
                        "tgChatIds",
                        List.of("q", "w", "e"))));
    }
}

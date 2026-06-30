package backend.academy.linktracker.bot;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.bot.controller.BotController;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.dto.LinkUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BotController.class)
class TelegramBotApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BotRepository botRepository;

    @MockitoBean
    private TelegramBot telegramBot;

    private final long chatId = 123;

    @BeforeEach
    void setUp() {
        when(botRepository.isPresent(chatId)).thenReturn(true);
    }

    @Test
    void validUpdate_shouldReturn200() throws Exception {
        LinkUpdate linkUpdate =
                new LinkUpdate("https://github.com/user/repo", "Изменение в репозитории", List.of(123L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("requestProvider")
    void invalidUpdate_shouldNotReturn200(Map<String, Objects> body) throws Exception {
        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
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

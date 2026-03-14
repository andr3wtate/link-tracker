package backend.academy.linktracker.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.commands.Command;
import backend.academy.linktracker.bot.commands.CommandType;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.bot.repository.BotState;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramBotModuleTest {
    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotRepository botRepository;

    @Mock
    private Command trackCommand;

    @Captor
    private ArgumentCaptor<SendMessage> captor;

    private BotApplication botApplication;

    @BeforeEach
    void setUp() {
        when(trackCommand.getType()).thenReturn(CommandType.TRACK);
        when(trackCommand.validateArgs(anyLong(), anyList())).thenReturn(true);
        botApplication = new BotApplication(telegramBot, botRepository, List.of(trackCommand));
    }

    @ParameterizedTest()
    @MethodSource("provideLinksAndTags")
    void trackCommand_withValidLink_shouldSaveSubscriptionAndNotify(
            String link, String tags, List<String> expectedTags) {
        long chatId = 1;
        String commandText = "/track " + link;
        List<String> args = List.of("/track", link);

        Message commandMessage = mock(Message.class);
        when(commandMessage.text()).thenReturn(commandText);
        when(commandMessage.entities()).thenReturn(new MessageEntity[] {mock(MessageEntity.class)});
        when(commandMessage.entities()[0].type()).thenReturn(MessageEntity.Type.bot_command);

        when(botRepository.isPresent(chatId)).thenReturn(true);

        botApplication.processAwaitingCommandState(commandMessage, chatId);

        verify(botRepository).setState(chatId, BotState.AWAITING_TAGS);
        verify(botRepository).setArgs(chatId, args);
        verify(telegramBot).execute(captor.capture());

        SendMessage sent = captor.getValue();
        assertThat(sent.getParameters().get("chat_id")).isEqualTo(chatId);
        assertThat(sent.getParameters().get("text")).isEqualTo("Введите теги через запятую или -, если теги не нужны");

        Message tagsMessage = mock(Message.class);
        when(tagsMessage.text()).thenReturn(tags);

        when(botRepository.getArgs(chatId)).thenReturn(args);

        botApplication.processAwaitingTagsState(tagsMessage, chatId);

        verify(botRepository).setTags(chatId, expectedTags);
        verify(botRepository).setState(chatId, BotState.AWAITING_COMMAND);
    }

    static Stream<Arguments> provideLinksAndTags() {
        return Stream.of(
                Arguments.of("https://github.com/user/repo", "   abc ,    def  ", List.of("abc", "def")),
                Arguments.of("https://github.com/user/repo1", "-", List.of()),
                Arguments.of("https://stackoverflow.com/questions/1234567", "q,    w,   e", List.of("q", "w", "e")));
    }

    @ParameterizedTest
    @CsvSource({"tbank://github.com/user/repo", "abcde:123", "1"})
    void trackCommand_withInvalidLink_shouldNotifyThatIncorrect(String link) {
        long chatId = 1;
        String commandText = "/track " + link;

        Message message = mock(Message.class);
        when(message.text()).thenReturn(commandText);
        when(message.entities()).thenReturn(new MessageEntity[] {mock(MessageEntity.class)});
        when(message.entities()[0].type()).thenReturn(MessageEntity.Type.bot_command);

        when(botRepository.isPresent(chatId)).thenReturn(true);

        botApplication.processAwaitingCommandState(message, chatId);

        verify(botRepository, never()).setState(eq(chatId), any());
        verify(telegramBot).execute(captor.capture());

        SendMessage sent = captor.getValue();
        assertThat(sent.getParameters().get("text")).isEqualTo("Некорректная ссылка");
    }
}

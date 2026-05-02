package backend.academy.linktracker.bot.commandstest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.commands.TrackCommand;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.exceptions.ClientException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TrackCommandModuleTest {
    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotRepository repository;

    @Mock
    private BotClient botClient;

    @Captor
    private ArgumentCaptor<SendMessage> captor;

    @InjectMocks
    private TrackCommand trackCommand;

    @Test
    void trackCommand_withTrackingLink_shouldNotifyThatLinkIsAlreadyTracking() {
        long chatId = 1;
        List<String> args = List.of("/track", "https://github.com/user/repo");

        when(botClient.addLinkTracking(eq(chatId), any())).thenThrow(mock(ClientException.class));

        trackCommand.processCommand(1, args);

        verify(telegramBot).execute(captor.capture());

        SendMessage sent = captor.getValue();
        assertThat(sent.getParameters().get("chat_id")).isEqualTo(chatId);
        assertThat(sent.getParameters().get("text")).isEqualTo("Произошла ошибка: Ссылка уже отслеживается");
    }
}

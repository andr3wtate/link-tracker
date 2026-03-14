package backend.academy.linktracker.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.Link;
import backend.academy.linktracker.commondto.ListLinks;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ListCommandModuleTest {
    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotRepository repository;

    @Mock
    private BotClient botClient;

    @Captor
    private ArgumentCaptor<SendMessage> captor;

    @InjectMocks
    private ListCommand listCommand;

    @Test
    void listCommand_withActiveLinks_shouldSendActiveLinks() {
        long chatId = 1;
        List<String> args = List.of("/link");
        List<String> links = List.of("https://github.com/user/repo", "https://stackoverflow.com/questions/123");

        when(botClient.getTrackedLinks(chatId))
                .thenReturn(new ListLinks(
                        links.stream()
                                .map(l -> new Link(0, URI.create(l), List.of()))
                                .toList(),
                        2));

        when(repository.getTags(chatId)).thenReturn(List.of());

        listCommand.processCommand(chatId, args);

        verify(telegramBot).execute(captor.capture());

        SendMessage sent = captor.getValue();
        assertThat(sent.getParameters().get("text")).isEqualTo(links.stream().reduce("", (s1, s2) -> s1 + s2 + "\n"));
    }

    @Test
    void listCommand_withNoActiveLinks_shouldSendNotify() {
        long chatId = 1;
        List<String> args = List.of("/link");

        when(botClient.getTrackedLinks(chatId)).thenReturn(new ListLinks(List.of(), 0));

        when(repository.getTags(chatId)).thenReturn(List.of());

        listCommand.processCommand(chatId, args);

        verify(telegramBot).execute(captor.capture());

        SendMessage sent = captor.getValue();
        assertThat(sent.getParameters().get("text")).isEqualTo("Не найдена ни одна ссылка с такими тегами");
    }

    @Test
    void listCommand_withTagsAndActiveLinks_shouldSendActiveLinks() {
        long chatId = 1;
        List<String> args = List.of("/link");
        List<String> links = List.of("https://github.com/user/repo", "https://stackoverflow.com/questions/123");

        when(botClient.getTrackedLinks(chatId))
                .thenReturn(new ListLinks(
                        List.of(
                                new Link(0, URI.create(links.getFirst()), List.of("tag1", "tag2")),
                                new Link(0, URI.create(links.get(1)), List.of("tag1"))),
                        2));

        when(repository.getTags(chatId)).thenReturn(List.of("tag2"));

        listCommand.processCommand(chatId, args);

        verify(telegramBot).execute(captor.capture());

        SendMessage sent = captor.getValue();
        assertThat(sent.getParameters().get("text")).isEqualTo(links.getFirst() + "\n");
    }
}

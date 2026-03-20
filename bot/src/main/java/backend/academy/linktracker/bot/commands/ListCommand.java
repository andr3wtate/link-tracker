package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.exceptions.ClientException;
import backend.academy.linktracker.commondto.ListLinks;
import com.pengrad.telegrambot.TelegramBot;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListCommand extends BaseCommand {
    public ListCommand(TelegramBot telegramBot, BotRepository repository, BotClient botClient) {
        super(telegramBot, repository, botClient, 1);
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST;
    }

    @Override
    public void processCommand(long chatId, List<String> args) {
        ListLinks links;
        try {
            links = botClient.getTrackedLinks(chatId);
        } catch (ClientException e) {
            notifyError(chatId, e.getApiError());
            log.atWarn().addKeyValue("chatId", chatId).log("Error in bot client while getting tracked links");
            return;
        }
        List<String> tags = repository.getTags(chatId);
        List<String> ans = links.links().stream()
                .filter(l -> tags.isEmpty() || l.tags().stream().anyMatch(tags::contains))
                .map(l -> l.url().toString())
                .toList();
        if (ans.isEmpty()) {
            sendMessage(chatId, "Не найдена ни одна ссылка с такими тегами");
        } else {
            sendMessage(chatId, ans.stream().reduce("", (s1, s2) -> s1 + s2 + "\n"));
        }
    }
}

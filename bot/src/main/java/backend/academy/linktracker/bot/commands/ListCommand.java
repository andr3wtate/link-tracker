package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.commondto.ClientException;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.ListLinks;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
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
            // todo логи
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

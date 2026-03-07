package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.client.BotClientException;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.RemoveLink;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.List;

@Component
public class UntrackCommand extends BaseCommand {
    public UntrackCommand(TelegramBot telegramBot, BotRepository repository, BotClient botClient) {
        super(telegramBot, repository, botClient, 2);
    }

    @Override
    public CommandType getType() {
        return CommandType.UNTRACK;
    }

    @Override
    public void processCommand(long chatId, List<String> args) {
        try {
            botClient.removeLinkTracking(chatId, new RemoveLink(URI.create(args.get(1))));
        } catch (BotClientException e) {
            notifyError(chatId, e.getApiError());
            return;
        }
        sendMessage(chatId,  String.format("Ссылка %s больше не отслеживается", args.get(1)));
    }
}

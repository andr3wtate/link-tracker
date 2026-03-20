package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.AddLink;
import backend.academy.linktracker.commondto.exceptions.ClientException;
import com.pengrad.telegrambot.TelegramBot;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrackCommand extends BaseCommand {
    public TrackCommand(TelegramBot telegramBot, BotRepository repository, BotClient botClient) {
        super(telegramBot, repository, botClient, 2);
    }

    @Override
    public CommandType getType() {
        return CommandType.TRACK;
    }

    @Override
    public void processCommand(long chatId, List<String> args) {
        AddLink addLink = new AddLink(URI.create(args.get(1)), repository.getTags(chatId));
        try {
            botClient.addLinkTracking(chatId, addLink);
        } catch (ClientException e) {
            notifyError(chatId, "Ссылка уже отслеживается");
            log.atWarn().addKeyValue("chatId", chatId).log("Error in bot client while adding link tracking");
            return;
        }
        sendMessage(chatId, String.format("Ссылка %s успешно отслеживается", args.get(1)));
    }
}

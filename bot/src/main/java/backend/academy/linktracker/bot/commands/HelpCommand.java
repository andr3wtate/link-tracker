package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends BaseCommand {
    private static final CommandType type = CommandType.HELP;

    public HelpCommand(TelegramBot telegramBot, BotRepository repository, BotClient botClient) {
        super(telegramBot, repository, botClient, 1);
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public void processCommand(long chatId, List<String> args) {
        sendMessage(
                chatId,
                Arrays.stream(CommandType.values())
                        .filter(commandType -> commandType != CommandType.UNKNOWN)
                        .map(c -> c.getName() + " - " + c.getDescription())
                        .reduce("", (s1, s2) -> s1 + s2 + "\n"));
    }
}

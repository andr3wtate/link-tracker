package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;

public class HelpCommand extends BaseCommand {
    private static final CommandType type = CommandType.HELP;

    public HelpCommand(TelegramBot telegramBot, BotRepository repository, long chatId, long userId) {
        super(telegramBot, repository, chatId, userId);
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public void processCommand() {
        telegramBot.execute(new SendMessage(
                chatId,
                Arrays.stream(CommandType.values())
                        .map(c -> c.getName() + " - " + c.getDescription())
                        .reduce("", (s1, s2) -> s1 + s2 + "\n")));
    }
}

package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.CommandType;
import backend.academy.linktracker.bot.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;

public class HelpCommand extends BaseCommand implements Command {
    private static final CommandType type = CommandType.HELP;

    public HelpCommand(TelegramBot telegramBot, UserRepository repository, long chatId, long userId) {
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

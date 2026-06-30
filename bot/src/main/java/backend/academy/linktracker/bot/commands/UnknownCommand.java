package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UnknownCommand extends BaseCommand {
    private static final CommandType type = CommandType.UNKNOWN;

    public UnknownCommand(TelegramBot telegramBot, BotRepository repository, BotClient botClient) {
        super(telegramBot, repository, botClient, 1);
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public void processCommand(long chatId, List<String> args) {
        telegramBot.execute(new SendMessage(
                chatId, "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд."));
    }
}

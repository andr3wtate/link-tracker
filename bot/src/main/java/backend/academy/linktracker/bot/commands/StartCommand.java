package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartCommand extends BaseCommand {
    private static final CommandType type = CommandType.START;

    public StartCommand(TelegramBot telegramBot, BotRepository repository, long chatId, long userId) {
        super(telegramBot, repository, chatId, userId);
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public void processCommand() {
        if (repository.isPresent(userId)) {
            telegramBot.execute(
                    new SendMessage(chatId, "Бот уже запущен. Используйте /help, чтобы посмотреть доступные команды."));
        } else {
            repository.addChat(userId);
            log.atInfo().addKeyValue("userId", userId).log("New user added");
            telegramBot.execute(new SendMessage(
                    chatId, "Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды."));
        }
    }
}

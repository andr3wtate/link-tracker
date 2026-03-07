package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.client.BotClientException;
import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class StartCommand extends BaseCommand {
    private static final CommandType type = CommandType.START;

    public StartCommand(TelegramBot telegramBot, BotRepository repository, BotClient botClient) {
        super(telegramBot, repository, botClient, 1);
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public void processCommand(long chatId, List<String> args) {
        if (repository.isPresent(chatId)) {
            sendMessage(chatId, "Бот уже запущен. Используйте /help, чтобы посмотреть доступные команды.");
            return;
        }
        try {
            botClient.registerChat(chatId);
        } catch (BotClientException e) {
            notifyError(chatId, e.getApiError());
            sendMessage(chatId, String.format("Произошла ошибка: %s\n%s", e.getApiError().description(), e.getApiError().exceptionMessage()));
            return;
        }
        repository.addChat(chatId);
        log.atInfo().addKeyValue("chatId", chatId).log("New user added");
        telegramBot.execute(new SendMessage(
                chatId, "Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды."));
    }
}

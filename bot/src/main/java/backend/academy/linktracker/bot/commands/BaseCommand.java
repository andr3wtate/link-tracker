package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.dto.ApiError;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseCommand implements Command {
    protected final TelegramBot telegramBot;
    protected final BotRepository repository;
    protected final BotClient botClient;
    protected final int argsLength; // с учетом самой команды

    public boolean validateArgs(long chatId, List<String> args) {
        if (args.isEmpty()) {
            // unreachable
            telegramBot.execute(new SendMessage(chatId, "Передано 0 аргументов"));
            return false;
        }
        if (args.size() != argsLength) {
            notifyError(chatId, String.format("Команда %s принимает %d аргументов", args.getFirst(), argsLength - 1));
            return false;
        }
        return true;
    }

    protected void sendMessage(long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

    protected void notifyError(long chatId, ApiError error) {
        notifyError(chatId, error.description());
    }

    protected void notifyError(long chatId, String message) {
        sendMessage(chatId, String.format("Произошла ошибка: %s", message));
    }
}

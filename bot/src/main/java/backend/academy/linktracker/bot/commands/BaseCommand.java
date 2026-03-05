package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class BaseCommand implements Command {
    protected final TelegramBot telegramBot;
    protected final BotRepository repository;
    protected final long chatId;
    protected final long userId;
}

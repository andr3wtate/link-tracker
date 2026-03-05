package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.commands.Command;
import backend.academy.linktracker.bot.commands.CommandType;
import backend.academy.linktracker.bot.commands.HelpCommand;
import backend.academy.linktracker.bot.commands.StartCommand;
import backend.academy.linktracker.bot.repository.BotRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class BotApplication {
    private final TelegramBot telegramBot;
    private final BotRepository repository;

    BotApplication(TelegramBot telegramBot, BotRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @PostConstruct
    void init() {
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                Message message = update.message();
                if (message == null) {
                    return;
                }
                long chatId = message.chat().id();
                long userId = message.from().id();
                if (message.entities() != null
                        && Arrays.stream(message.entities())
                                .map(MessageEntity::type)
                                .toList()
                                .contains(MessageEntity.Type.bot_command)) {
                    CommandType commandType = CommandType.getCommand(message.text());
                    if (commandType == null) {
                        telegramBot.execute(
                                new SendMessage(
                                        chatId,
                                        "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд."));
                    } else {
                        Command command = getCommand(commandType, chatId, userId);
                        command.processCommand();
                    }
                } else {
                    telegramBot.execute(new SendMessage(chatId, "Команды начинаются с /"));
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private Command getCommand(CommandType commandType, long chatId, long userId) {
        return switch (commandType) {
            case HELP -> new HelpCommand(telegramBot, repository, chatId, userId);
            case START -> new StartCommand(telegramBot, repository, chatId, userId);
        };
    }

    static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}

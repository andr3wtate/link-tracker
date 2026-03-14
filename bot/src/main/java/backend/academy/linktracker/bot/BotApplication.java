package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.commands.Command;
import backend.academy.linktracker.bot.commands.CommandType;
import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.bot.repository.BotState;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class BotApplication {
    private final TelegramBot telegramBot;
    private final BotRepository repository;
    private final Map<CommandType, Command> getCommand;

    BotApplication(TelegramBot telegramBot, BotRepository repository, List<Command> commandList) {
        this.telegramBot = telegramBot;
        this.repository = repository;
        this.getCommand = commandList.stream()
                .map(c -> Map.entry(c.getType(), c))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
                if (!repository.isPresent(chatId) || repository.getState(chatId) == BotState.AWAITING_COMMAND) {
                    processAwaitingCommandState(message, chatId);
                } else {
                    processAwaitingTagsState(message, chatId);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    void processAwaitingCommandState(Message message, long chatId) {
        if (message.entities() != null
                && Arrays.stream(message.entities())
                        .map(MessageEntity::type)
                        .toList()
                        .contains(MessageEntity.Type.bot_command)) {
            List<String> args = Arrays.stream(message.text().split("\\s+")).toList();
            CommandType commandType = CommandType.getCommandType(args.getFirst());
            if (!repository.isPresent(chatId) && commandType != CommandType.START) {
                sendMessage(chatId, "Для начала использования бота напишите /start");
                return;
            }

            Command command = getCommand.get(commandType);
            if (!command.validateArgs(chatId, args)) {
                return;
            }

            if (commandType == CommandType.TRACK || commandType == CommandType.LIST) {
                if (commandType == CommandType.TRACK && !validateLink(args.get(1))) {
                    sendMessage(chatId, "Некорректная ссылка");
                    return;
                }
                sendMessage(chatId, "Введите теги через запятую или -, если теги не нужны");
                repository.setState(chatId, BotState.AWAITING_TAGS);
                repository.setArgs(chatId, args);
            } else {
                command.processCommand(chatId, args);
            }
        } else {
            sendMessage(chatId, "Команды начинаются с /");
        }
    }

    void processAwaitingTagsState(Message message, long chatId) {
        List<String> tags;
        if (message.text().equals("-")) {
            tags = List.of();
        } else {
            if (!isValidTags(message.text())) {
                sendMessage(chatId, "Теги должны быть словами, разделенными запятыми, введите их еще раз");
                return;
            }
            tags = Arrays.stream(message.text().trim().split("\\s*,\\s*")).toList();
        }
        repository.setTags(chatId, tags);
        List<String> args = repository.getArgs(chatId);
        CommandType commandType = CommandType.getCommandType(args.getFirst());
        getCommand.get(commandType).processCommand(chatId, args);
        repository.setState(chatId, BotState.AWAITING_COMMAND);
    }

    private void sendMessage(long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

    private boolean validateLink(String link) {
        try {
            URI.create(link).toURL();
            return true;
        } catch (IllegalArgumentException | MalformedURLException e) {
            return false;
        }
    }

    private boolean isValidTags(String input) {
        String trimmed = input.trim();
        if (trimmed.equals("-")) {
            return true;
        }
        String[] parts = trimmed.split("\\s*,\\s*");
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            for (char c : part.toCharArray()) {
                if (!Character.isLetter(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}

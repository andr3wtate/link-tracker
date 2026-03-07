package backend.academy.linktracker.bot.commands;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum CommandType {
    UNKNOWN("", ""),
    START("/start", "Запуск бота"),
    HELP("/help", "Доступные команды"),
    TRACK("/track", "Начать отслеживание ссылки"),
    UNTRACK("/untrack", "Прекратить отслеживание ссылки"),
    LIST("/list", "Вывести список отслеживаемых ссылок");

    private final String name;
    private final String description;

    private static final Map<String, CommandType> mapping =
            Arrays.stream(values()).collect(Collectors.toMap(CommandType::getName, Function.identity()));

    CommandType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static CommandType getCommandType(String name) {
        return mapping.getOrDefault(name, UNKNOWN);
    }
}

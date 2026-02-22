package backend.academy.linktracker.bot.commands;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum CommandType {
    START("/start", "Запуск бота"),
    HELP("/help", "Доступные команды");

    private final String name;
    private final String description;

    private static final Map<String, CommandType> mapping =
            Arrays.stream(CommandType.values()).collect(Collectors.toMap(CommandType::getName, Function.identity()));

    CommandType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static CommandType getCommand(String name) {
        return mapping.getOrDefault(name, null);
    }
}

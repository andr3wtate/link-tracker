package backend.academy.linktracker.bot.commands;

import lombok.Getter;

@Getter
public enum CommandType {
    START("/start", "Запуск бота"),
    HELP("/help", "Доступные команды");

    private final String name;
    private final String description;

    CommandType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

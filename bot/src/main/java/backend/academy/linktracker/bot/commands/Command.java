package backend.academy.linktracker.bot.commands;

import java.util.List;

public interface Command {
    CommandType getType();

    void processCommand(long chatId, List<String> args);

    boolean validateArgs(long chatId, List<String> args);
}

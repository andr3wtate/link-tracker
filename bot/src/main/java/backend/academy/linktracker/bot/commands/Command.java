package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.CommandType;

public interface Command {
    CommandType getType();

    void processCommand();
}

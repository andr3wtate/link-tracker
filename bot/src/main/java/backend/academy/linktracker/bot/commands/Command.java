package backend.academy.linktracker.bot.commands;

public interface Command {
    CommandType getType();

    void processCommand();
}

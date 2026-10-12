package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.util.CommandManager;

/**
 * Команда для вывода списка команд
 */
public class Help extends Command {
    private final CommandManager commandManager;

    public Help(CommandManager commandManager) {
        super("help", "вывести справку по доступным командам", null);
        this.commandManager = commandManager;
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (!argument.isEmpty() || element != null) {
            throw new IllegalArgumentException("Команда не принимает аргументы");
        }
        StringBuilder sb = new StringBuilder();
        commandManager.getCommands().forEach(command ->
                sb.append(command.getName()).append(": ").append(command.getDescription()).append("\n"));
        return new Response(true, sb.toString());
    }
}
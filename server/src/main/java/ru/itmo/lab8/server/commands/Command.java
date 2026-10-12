package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.CommandRequirement;
import ru.itmo.lab8.common.util.Response;

/**
 * Класс-предок всех классов команд
 */
public abstract class Command {
    private final String name;
    private final String description;
    private final CommandRequirement requirement;

    public Command(String name, String description, CommandRequirement requirement) {
        this.name = name;
        this.description = description;
        this.requirement = requirement;
    }

    /**
     * @param argument
     * @param element
     * @param username
     * @return ответ от сервера о выполнении команды
     * @throws Exception
     */
    public abstract Response execute(String argument, MusicBand element, String username) throws Exception;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CommandRequirement getRequirement() {
        return requirement;
    }
}
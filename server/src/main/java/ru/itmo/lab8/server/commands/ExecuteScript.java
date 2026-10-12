package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.CommandRequirement;
import ru.itmo.lab8.common.util.Response;

/**
 * Команда выполнения скрипта
 */
public class ExecuteScript extends Command {

    public ExecuteScript() {
        super("execute_script", "считать и выполнить скрипт из файла", CommandRequirement.RUN_SCRIPT);
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (argument.isEmpty()) {
            throw new IllegalArgumentException("Команде нужен аргумент");
        }
        return new Response(true, getRequirement().name());
    }
}
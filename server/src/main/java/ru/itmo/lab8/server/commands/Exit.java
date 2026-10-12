package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.CommandRequirement;
import ru.itmo.lab8.common.util.Response;

/**
 * Команда для завершения работы клиента
 */
public class Exit extends Command {

    public Exit() {
        super("exit", "завершить программу", CommandRequirement.EXIT);
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (!argument.isEmpty() || element != null) {
            throw new IllegalArgumentException("Команда не принимает аргументы");
        }
        return new Response(true, getRequirement().name());
    }
}
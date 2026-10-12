package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.database.DatabaseManager;

/**
 * Команда очистки коллекции
 */
public class Clear extends Command {
    private final DatabaseManager dbManager;

    public Clear(DatabaseManager dbManager) {
        super("clear", "очистить коллекцию", null);
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (!argument.isEmpty() || element != null) {
            throw new IllegalArgumentException("Команда не принимает аргументы");
        }
        dbManager.clearCollection(username);
        return new Response(true, "Ваши элементы удалены из коллекции");
    }
}
package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.CommandRequirement;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.database.DatabaseManager;

/**
 * Команда удаления из коллекции всех элементов, превышающих заданный
 */
public class RemoveGreater extends Command {
    private final DatabaseManager dbManager;

    public RemoveGreater(DatabaseManager dbManager) {
        super("remove_greater", "удалить элементы, превышающие заданный", CommandRequirement.ELEMENT);
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (!argument.isEmpty()) {
            throw new IllegalArgumentException("Команда не принимает аргументы");
        }
        if (element == null) {
            return new Response(true, getRequirement().name());
        }

        String removedCount = String.valueOf(dbManager.removeGreater(element, username));
        return new Response(true, removedCount + " элементов удалено");
    }
}
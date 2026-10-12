package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.database.DatabaseManager;

/**
 * Команда удаления из коллекции всех элементов, ключ которых превышает заданный
 */
public class RemoveGreaterKey extends Command {
    private final DatabaseManager dbManager;

    public RemoveGreaterKey(DatabaseManager dbManager) {
        super("remove_greater_key", "удалить элементы с ключом больше заданного", null);
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (argument.trim().isEmpty()) {
            throw new IllegalArgumentException("Команде нужен аргумент");
        }
        Integer key = Integer.valueOf(argument);
        String removedCount = String.valueOf(dbManager.removeGreaterKey(key, username));
        return new Response(true, removedCount + " элементов удалено");
    }
}
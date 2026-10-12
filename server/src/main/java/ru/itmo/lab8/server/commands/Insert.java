package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.exceptions.KeyException;
import ru.itmo.lab8.common.util.CommandRequirement;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.database.DatabaseManager;
import ru.itmo.lab8.server.util.CollectionManager;

/**
 * Команда добавления элемента в коллекцию
 */
public class Insert extends Command {
    private final DatabaseManager dbManager;
    private final CollectionManager collectionManager;

    public Insert(DatabaseManager dbManager, CollectionManager collectionManager) {
        super("insert", "добавить новый элемент с заданным ключом", CommandRequirement.ELEMENT);
        this.dbManager = dbManager;
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (argument.trim().isEmpty()) {
            throw new IllegalArgumentException("Команде нужен аргумент");
        }
        Integer key = Integer.valueOf(argument);

        if (collectionManager.containsKey(key)) {
            throw new KeyException("Элемент с таким ключом уже существует");
        }

        if (element == null) {
            return new Response(true, getRequirement().name());
        }

        dbManager.addMusicBand(key, element, username);
        return new Response(true, "Элемент с ключом " + key + " добавлен в коллекцию");
    }
}
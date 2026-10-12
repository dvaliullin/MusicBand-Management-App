package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.database.DatabaseManager;
import ru.itmo.lab8.server.util.CollectionManager;

/**
 * Команда удаления элемента из коллекции по его ключу
 */
public class RemoveKey extends Command {
    private final DatabaseManager dbManager;
    private final CollectionManager collectionManager;

    public RemoveKey(DatabaseManager dbManager, CollectionManager collectionManager) {
        super("remove_key", "удалить элемент из коллекции по его ключу", null);
        this.dbManager = dbManager;
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String argument, MusicBand element, String username) throws Exception {
        if (argument.trim().isEmpty()) {
            throw new IllegalArgumentException("Команде нужен аргумент");
        }
        Integer key = Integer.valueOf(argument);

        if (!collectionManager.containsKey(key)) {
            throw new IllegalArgumentException("Ключа с этим значением не существует");
        }

        if (!dbManager.confirmOwner(key, username)) {
            return new Response(false, "Нет прав на изменение этого элемента");
        }

        dbManager.removeMusicBand(key, username);
        return new Response(true, "Элемент удален");
    }
}
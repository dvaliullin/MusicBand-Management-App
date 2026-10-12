package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.CommandRequirement;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.database.DatabaseManager;
import ru.itmo.lab8.server.util.CollectionManager;

/**
 * Команда замены значения по ключу, если новое значение больше старого
 */
public class ReplaceIfGreater extends Command {
    private final DatabaseManager dbManager;
    private final CollectionManager collectionManager;

    public ReplaceIfGreater(DatabaseManager dbManager, CollectionManager collectionManager) {
        super("replace_if_greater", "заменить значение, если новое больше старого", CommandRequirement.ELEMENT);
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

        if (element == null) {
            return new Response(true, getRequirement().name());
        }

        MusicBand oldElement = collectionManager.getByKey(key);
        if (oldElement.compareTo(element) < 0) {
            dbManager.updateMusicBand(key, element, username);
            return new Response(true, "Значение заменено");
        } else {
            return new Response(true, "Значение не заменено (меньше старого)");
        }
    }
}
package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.exceptions.IllegalValueException;
import ru.itmo.lab8.common.util.CommandRequirement;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.database.DatabaseManager;
import ru.itmo.lab8.server.util.CollectionManager;

/**
 * Команда обновления значения элемента коллекции, id которого равен заданному
 */
public class Update extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager dbManager;

    public Update(CollectionManager collectionManager, DatabaseManager dbmanager) {
        super("update", "обновить значение элемента коллекции, id которого равен заданному", CommandRequirement.ELEMENT);
        this.collectionManager = collectionManager;
        this.dbManager = dbmanager;
    }

    /**
     * @param argument целое число
     */
    @Override
    public Response execute(String argument, MusicBand element, String username) throws NumberFormatException {
        if (argument.trim().isEmpty()) {
            throw new IllegalArgumentException("Команде нужен аргумент");
        }

        Integer id = Integer.valueOf(argument);

        if (!collectionManager.containsId(id)) {
            throw new IllegalValueException("Элемента с id = " + id + " не существует");
        }

        if (!dbManager.confirmOwner(collectionManager.getKeyById(id), username)) {
            return new Response(false, "Нет прав на изменение этого элемента");
        }

        if (element == null) {
            return new Response(true, getRequirement().name());
        }

        dbManager.updateMusicBandById(id, element, username);
        return new Response(true, "Элемент коллекции успешно обновлен");
    }
}

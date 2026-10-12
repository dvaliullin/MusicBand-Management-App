package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.util.CollectionManager;

/**
 * Команда вывода всех элементов коллекции
 */
public class Show extends Command {
    private final CollectionManager collectionManager;

    public Show(CollectionManager collectionManager) {
        super("show", "вывести все элементы коллекции", null);
        this.collectionManager = collectionManager;
    }

    /**
     * @param argument пустой
     */
    @Override
    public Response execute(String argument, MusicBand element, String username) {
        if (!argument.isEmpty() || element != null) {
            throw new IllegalArgumentException("Команда не принимает аргументы");
        }
        return new Response(true, collectionManager.toString(), collectionManager.getCollection());
    }
}


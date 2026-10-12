package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.util.CollectionManager;

/**
 * Команда для вывода информации о коллекции
 */
public class Info extends Command {
    private final CollectionManager collectionManager;

    public Info(CollectionManager collectionManager) {
        super("info", "вывести информацию о коллекции", null);
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
        return new Response(true, collectionManager.collectionInfo());
    }
}

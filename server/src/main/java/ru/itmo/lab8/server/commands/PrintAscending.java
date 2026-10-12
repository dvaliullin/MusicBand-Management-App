package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.util.CollectionManager;

import java.util.stream.Collectors;

/**
 * Команда вывода элементов коллекции в порядке возрастания
 */
public class PrintAscending extends Command {
    private final CollectionManager collectionManager;

    public PrintAscending(CollectionManager collectionManager) {
        super("print_ascending", "вывести элементы коллекции в порядке возрастания", null);
        this.collectionManager = collectionManager;
    }

    /**
     * @param argument пустой
     */
    @Override
    public Response execute(String argument, MusicBand element, String username) {
        if (!argument.isEmpty()) {
            throw new IllegalArgumentException("Команда не принимает аргументы");
        }
        return new Response(true, sortedCollectionToString());
    }

    public String sortedCollectionToString() {
        if (collectionManager.getSortedCollection().isEmpty()) {
            return "Коллекция пуста";
        }
        StringBuilder sb = new StringBuilder();
        collectionManager.getSortedCollection().forEach((key, value) -> {
            sb.append("Ключ: ").append(key).append("\n");
            sb.append(value.toString()).append("\n\n");
        });
        return sb.toString();
    }
}

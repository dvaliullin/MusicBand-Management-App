package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.util.CollectionManager;

/**
 * Команда подсчета элементов с числом участников группы больше заданного
 */
public class CountGreaterThanNumberOfParticipants extends Command {
    private final CollectionManager collectionManager;

    public CountGreaterThanNumberOfParticipants(CollectionManager collectionManager) {
        super("count_greater_than_number_of_participants", "вывести количество элементов, значение поля numberOfParticipants которых больше заданного", null);
        this.collectionManager = collectionManager;
    }

    /**
     * @param argument целое число
     */
    @Override
    public Response execute(String argument, MusicBand element, String username) throws NumberFormatException {
        if (argument.trim().isEmpty()) {
            throw new IllegalArgumentException("Команде нужен аргумент");
        }
        Integer number = Integer.valueOf(argument);
        return new Response(true, String.valueOf(collectionManager.countGreaterThanNumberOfParticipants(number)));
    }
}


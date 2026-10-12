package ru.itmo.lab8.server.commands;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.util.CollectionManager;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Команда вывода значения поля numberOfParticipants всех элементов в порядке убывания
 */
public class PrintFieldDescendingNumberOfParticipants extends Command {
    private final CollectionManager collectionManager;

    public PrintFieldDescendingNumberOfParticipants(CollectionManager collectionManager) {
        super("print_field_descending_number_of_participants", "вывести значения поля numberOfParticipants всех элементов в порядке убывания", null);
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
        ArrayList<Integer> descendingNumberOfParticipantsList = new ArrayList<>(collectionManager.getNumberOfParticipantsList());
        descendingNumberOfParticipantsList.sort(Comparator.reverseOrder());
        return new Response(true, descendingNumberOfParticipantsList.toString().replace("[","").replace("]",""));
    }
}


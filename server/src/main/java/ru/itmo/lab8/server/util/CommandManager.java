package ru.itmo.lab8.server.util;

import ru.itmo.lab8.server.commands.*;
import ru.itmo.lab8.server.database.DatabaseManager;

import java.util.ArrayList;

/**
 * Класс управления командами
 */
public class CommandManager {
    private final ArrayList<Command> commands = new ArrayList<>();

    /**
     * Инициализация команд
     * @param dbManager
     * @param collectionManager
     */
    public void initializeCommands(DatabaseManager dbManager, CollectionManager collectionManager) {
        addCommand(new Help(this));
        addCommand(new Info(collectionManager));
        addCommand(new Show(collectionManager));
        addCommand(new Insert(dbManager, collectionManager));
        addCommand(new Update(collectionManager, dbManager));
        addCommand(new RemoveKey(dbManager, collectionManager));
        addCommand(new Clear(dbManager));
        addCommand(new ExecuteScript());
        addCommand(new Exit());
        addCommand(new RemoveGreater(dbManager));
        addCommand(new ReplaceIfGreater(dbManager, collectionManager));
        addCommand(new RemoveGreaterKey(dbManager));
        addCommand(new CountGreaterThanNumberOfParticipants(collectionManager));
        addCommand(new PrintAscending(collectionManager));
        addCommand(new PrintFieldDescendingNumberOfParticipants(collectionManager));
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    /**
     * Добавить команду
     * @param command
     */
    public void addCommand(Command command) {
        commands.add(command);
    }

    /**
     * @param name
     * @return команду по ее названию
     */
    public Command getCommandByName(String name) {
        return commands.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * @param commandName
     * @return true, если есть команда с таким именем, иначе false
     */
    public boolean containsCommand(String commandName) {
        return getCommandByName(commandName) != null;
    }
}
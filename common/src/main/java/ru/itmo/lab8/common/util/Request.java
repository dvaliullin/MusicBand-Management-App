package ru.itmo.lab8.common.util;

import java.io.Serializable;

/**
 * Класс для запросов от клиента
 */
public class Request implements Serializable {
    private final String commandName;
    private final String commandArgument;
    private Serializable element;
    private String sessionToken;

    public Request(String commandName, String commandArgument, Serializable element) {
        this.commandName = commandName;
        this.commandArgument = commandArgument;
        this.element = element;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getCommandArgument() {
        return commandArgument;
    }

    public Serializable getElement() {
        return element;
    }

    public void setElement(Serializable element) {
        this.element = element;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public String toString() {
        return "запрос команды " + commandName + " с аргументом " + commandArgument;
    }
}
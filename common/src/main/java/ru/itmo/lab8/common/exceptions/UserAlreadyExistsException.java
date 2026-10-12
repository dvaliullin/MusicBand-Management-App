package ru.itmo.lab8.common.exceptions;

/**
 * Исключение, выбрасываемое при попытке зарегистрировать уже существующего пользователя
 */
public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String username) {
        super("Пользователь " + username + " уже существует");
    }
}
package ru.itmo.lab8.common.exceptions;

/**
 * Недопустимое значение
 */
public class IllegalValueException extends RuntimeException {
    private final String message;

    public IllegalValueException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

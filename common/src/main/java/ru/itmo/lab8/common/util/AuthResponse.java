package ru.itmo.lab8.common.util;

import java.io.Serializable;

/**
 * Класс для ответов сервера об аутентификации
 */
public class AuthResponse implements Serializable {
    private final boolean success;
    private final String message;
    private final String sessionToken;

    public AuthResponse(boolean success, String message, String sessionToken) {
        this.success = success;
        this.message = message;
        this.sessionToken = sessionToken;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
package ru.itmo.lab8.common.util;

import java.io.Serializable;

/**
 * Класс для запросов аутентификации
 */
public class AuthRequest implements Serializable {
    private final String username;
    private final String passwordHash;
    private final boolean isRegister;

    public AuthRequest(String username, String passwordHash, boolean isRegister) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.isRegister = isRegister;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isRegister() {
        return isRegister;
    }
}
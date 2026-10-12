package ru.itmo.lab8.common.util;

import java.io.Serializable;

/**
 * Класс для ответов от сервера
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final boolean success;
    private Serializable data;

    public Response(boolean success, String message) {
        this.message = message;
        this.success = success;
    }

    public Response(boolean success, String message, Serializable data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }

    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
    public Serializable getData() { return data; }
}
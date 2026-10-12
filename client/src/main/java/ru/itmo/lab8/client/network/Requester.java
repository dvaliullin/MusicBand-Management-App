package ru.itmo.lab8.client.network;

import ru.itmo.lab8.common.util.AuthRequest;
import ru.itmo.lab8.common.util.AuthResponse;
import ru.itmo.lab8.common.util.Request;
import ru.itmo.lab8.common.util.Response;

import java.io.IOException;

/**
 * Класс для отправки запросов серверу
 */
public class Requester {
    private final ConnectionManager connectionManager;
    private String sessionToken = null;

    public Requester(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * @param request
     * @return ответ от сервера на отправленный запрос аутентификации
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public AuthResponse sendAuthRequest(AuthRequest request) throws IOException, ClassNotFoundException {
        if (!connectionManager.connect()) {
            throw new IOException("Сервер недоступен");
        }

        try {
            connectionManager.writeObject(request);
            AuthResponse response = (AuthResponse) connectionManager.readObject();
            if (response.isSuccess() && response.getSessionToken() != null) {
                setSessionToken(response.getSessionToken());
            }
            return response;
        } finally {
            connectionManager.close();
        }
    }

    /**
     * @param request
     * @return ответ от сервера на отправленный запрос
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Response sendRequest(Request request) throws IOException, ClassNotFoundException {
        if (!connectionManager.connect()) {
            throw new IOException("Сервер недоступен");
        }

        try {
            request.setSessionToken(sessionToken);
            connectionManager.writeObject(request);
            return (Response) connectionManager.readObject();
        } finally {
            connectionManager.close();
        }
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
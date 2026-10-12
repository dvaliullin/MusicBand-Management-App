package ru.itmo.lab8.server.network;

import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.exceptions.UserAlreadyExistsException;
import ru.itmo.lab8.common.util.AuthRequest;
import ru.itmo.lab8.common.util.AuthResponse;
import ru.itmo.lab8.common.util.Request;
import ru.itmo.lab8.common.util.Response;
import ru.itmo.lab8.server.commands.Command;
import ru.itmo.lab8.server.database.UsersManager;
import ru.itmo.lab8.server.util.CommandManager;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.itmo.lab8.server.Server.logger;

/**
 * Класс для обработки запросов
 */
public class Handler {
    private final CommandManager commandManager;
    private final UsersManager usersManager;
    private final ExecutorService readPool = Executors.newCachedThreadPool();
    private final ExecutorService processPool = Executors.newCachedThreadPool();
    private final ExecutorService writePool = Executors.newFixedThreadPool(10);

    public Handler(CommandManager commandManager, UsersManager usersManager) {
        this.commandManager = commandManager;
        this.usersManager = usersManager;
    }

    /**
     * Обработка подключений
     *
     * @param selector
     * @param key
     * @throws IOException
     */
    public void handleAccept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        Receiver receiver = new Receiver(socketChannel);
        SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
        clientKey.attach(receiver);
        logger.info("Новое подключение от {}", socketChannel.getRemoteAddress());
    }

    /**
     * Чтение данных
     *
     * @param key
     */
    public void handleRead(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Receiver receiver = (Receiver) key.attachment();
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
        readPool.submit(() -> readAndProcess(key, socketChannel, receiver));
    }

    /**
     * Передача прочитанных данных на обработку
     *
     * @param key
     * @param socketChannel
     * @param receiver
     */
    private void readAndProcess(SelectionKey key, SocketChannel socketChannel, Receiver receiver) {
        try {
            Object received = receiver.receiveObject();

            if (received == null) {
                handleNullReceived(key, socketChannel, receiver);
                return;
            }

            processReceivedObject(received, receiver);

        } catch (ClassNotFoundException e) {
            logger.error("Ошибка десериализации");
            sendResponse(new Response(false, "Ошибка формата запроса"), receiver);
        } catch (SocketException e) {
            logger.error("Соединение прервано");
            closeConnection(key, socketChannel);
        } catch (IOException e) {
            logger.error("Ошибка чтения: {}", e.getMessage());
            closeConnection(key, socketChannel);
        }
    }

    /**
     * Обработка null-объекта
     *
     * @param key
     * @param socketChannel
     * @param receiver
     */
    private void handleNullReceived(SelectionKey key, SocketChannel socketChannel, Receiver receiver) {
        if (receiver.isEof()) {
            closeConnection(key, socketChannel);
        } else {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.selector().wakeup();
        }
    }

    /**
     * Обработка полученного объекта
     *
     * @param received
     * @param receiver
     */
    private void processReceivedObject(Object received, Receiver receiver) {
        if (received instanceof AuthRequest authRequest) {
            processAuth(authRequest, receiver);
        } else if (received instanceof Request request) {
            processRequest(request, receiver);
        }
    }

    /**
     * Обработка аутентификации
     *
     * @param authRequest
     * @param receiver
     */
    public void processAuth(AuthRequest authRequest, Receiver receiver) {
        processPool.submit(() -> {
            AuthResponse response = executeAuth(authRequest);
            sendResponse(response, receiver);
        });
    }

    /**
     * Выполнение аутентификации
     */
    private AuthResponse executeAuth(AuthRequest authRequest) {
        String username = authRequest.getUsername();
        String passwordHash = authRequest.getPasswordHash();

        boolean success;
        String message;
        String token = null;

        if (authRequest.isRegister()) {
            try {
                usersManager.registerUser(username, passwordHash);
                success = true;
                message = "Регистрация успешна";
                token = usersManager.authenticateUser(username, passwordHash);
            } catch (UserAlreadyExistsException | SQLException e) {
                success = false;
                message = "Ошибка регистрации: " + e.getMessage();
            }
        } else {
            token = usersManager.authenticateUser(username, passwordHash);
            success = token != null;
            message = success ? "Авторизация успешна" : "Неверный логин или пароль";
        }

        return new AuthResponse(success, message, token);
    }

    /**
     * Обработка запросов
     *
     * @param request
     * @param receiver
     */
    private void processRequest(Request request, Receiver receiver) {
        processPool.submit(() -> {
            String token = request.getSessionToken();

            if (!usersManager.validateToken(token)) {
                sendResponse(new Response(false, "Необходима авторизация. Используйте 'login' или 'register'"), receiver);
                return;
            }

            String username = usersManager.getUserByToken(token);
            Response response = executeCommand(request, username);
            sendResponse(response, receiver);
        });
    }

    /**
     * Выполнение команд
     */
    private Response executeCommand(Request request, String username) {
        String commandName = request.getCommandName();
        String argument = request.getCommandArgument();
        Serializable data = request.getElement();

        logger.info("Обработка команды: {}", commandName);

        try {
            if (!commandManager.containsCommand(commandName)) {
                return new Response(false, "Неизвестная команда");
            }

            Command command = commandManager.getCommandByName(commandName);
            return command.execute(argument, (MusicBand) data, username);

        } catch (NumberFormatException e) {
            return new Response(false, "Ошибка: Неправильный аргумент, введите целое число");
        } catch (Exception e) {
            logger.error("Ошибка выполнения команды: {}", e.getMessage());
            return new Response(false, "Ошибка: " + e.getMessage());
        }
    }

    /**
     * Отправить ответ
     *
     * @param response
     * @param receiver
     */
    private void sendResponse(Object response, Receiver receiver) {
        writePool.submit(() -> {
            try {
                receiver.sendObject(response);
            } catch (IOException e) {
                logger.error("Ошибка отправки ответа: {}", e.getMessage());
            }
        });
    }

    /**
     * Закрыть соединение
     *
     * @param key
     * @param socketChannel
     */
    private void closeConnection(SelectionKey key, SocketChannel socketChannel) {
        try {
            if (key != null) key.cancel();
            if (socketChannel != null && socketChannel.isOpen()) socketChannel.close();
        } catch (IOException e) {
            logger.error("Ошибка закрытия соединения: {}", e.getMessage());
        }
    }

    /**
     * Остановка пулов потоков
     */
    public void shutdown() {
        logger.info("Остановка пулов потоков...");
        readPool.shutdownNow();
        processPool.shutdownNow();
        writePool.shutdownNow();
    }
}
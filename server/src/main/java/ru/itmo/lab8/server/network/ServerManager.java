package ru.itmo.lab8.server.network;

import ru.itmo.lab8.common.util.Checker;
import ru.itmo.lab8.server.Server;
import ru.itmo.lab8.server.database.DatabaseManager;

import java.io.IOException;
import java.nio.channels.*;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * Класс управления сервером
 */
public class ServerManager {
    private final String host;
    private final String port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final Handler handler;
    private volatile boolean running = true;
    private final DatabaseManager dbManager;

    public ServerManager(String host, String port, Handler handler, DatabaseManager dbManager) {
        this.host = host;
        this.port = port;
        this.handler = handler;
        this.dbManager = dbManager;
    }

    /**
     * Запуск сервера
     */
    public void start() {
        setShutdownHook();
        try {
            initializeServer();
            Server.logger.info("Запуск сервера: хост {}, порт {}", host, port);
            run();
        } catch (UnresolvedAddressException e) {
            Server.logger.error("Ошибка запуска сервера: адрес недоступен");
        } catch (IOException e) {
            Server.logger.error("Ошибка запуска сервера: {}", e.getMessage());
        }
    }

    /**
     * Инициализация сервера
     * @throws IOException
     */
    public void initializeServer() throws IOException {
        setServerSocketChannel(ServerSocketChannel.open());
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(Checker.checkAddress(host, port));
        setSelector(Selector.open());
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Начало работы сервера
     */
    public void run() {
        Thread consoleThread = new Thread(this::consoleInput);
        consoleThread.setDaemon(true);
        consoleThread.start();
        while (running) {
            try {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        handler.handleAccept(selector, key);
                    } else if (key.isReadable()) {
                        handler.handleRead(key);
                    }
                }
            } catch (ClosedSelectorException e) {
                stop();
            } catch (IOException e) {
                Server.logger.error("Ошибка в работе сервера: {}", e.getMessage());
            }
        }
    }

    /**
     * Остановка сервера
     */
    public void stop() {
        setRunning(false);
        try {
            if (selector != null) {
                selector.close();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            if (handler != null) {
                handler.shutdown();
            }
            if (dbManager != null) {
                dbManager.close();
            }
        } catch (IOException e) {
            Server.logger.error("Ошибка остановки сервера: {}", e.getMessage());
        } catch (SQLException e) {
            Server.logger.error("Ошибка закрытия соединения с БД: {}", e.getMessage());
        }
    }

    /**
     * Ввод серверных команд
     */
    public void consoleInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                String input = scanner.nextLine().trim().toLowerCase();
                switch (input) {
                    case "":
                        continue;
                    case "save":
                        Server.logger.info("Данные сохранены в базе данных");
                        break;
                    case "exit":
                        Server.logger.info("Завершение работы сервера");
                        System.exit(0);
                    default:
                        Server.logger.error("Не удалось распознать команду (save или exit)");
                        break;
                }
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }

    /**
     * Обработка прерываний
     */
    public void setShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Server.logger.info("Получен сигнал завершения работы сервера...");
            try {
                dbManager.close();
                Server.logger.info("Соединение с БД закрыто");
            } catch (Exception e) {
                Server.logger.error("Ошибка закрытия соединения: {}", e.getMessage());
            }
            stop();
            Server.logger.info("Сервер завершил работу");
        }));
    }

    public void setServerSocketChannel(ServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
package ru.itmo.lab8.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.lab8.server.database.DatabaseManager;
import ru.itmo.lab8.server.database.SQLQueryManager;
import ru.itmo.lab8.server.database.UsersManager;
import ru.itmo.lab8.server.network.Handler;
import ru.itmo.lab8.server.network.ServerManager;
import ru.itmo.lab8.server.util.CollectionManager;
import ru.itmo.lab8.server.util.CommandManager;

/**
 * Главный класс сервера
 */
public class Server {
    public static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        String host = System.getenv("HOST");
        String port = System.getenv("PORT");
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUrl == null || dbUser == null || dbPassword == null) {
            logger.error("Не заданы переменные окружения для БД: DB_URL, DB_USER, DB_PASSWORD");
            System.exit(1);
        }

        try {
            CollectionManager collectionManager = new CollectionManager();
            DatabaseManager dbManager = new DatabaseManager(collectionManager, new SQLQueryManager());
            UsersManager usersManager = new UsersManager(dbManager);
            dbManager.connect(dbUrl, dbUser, dbPassword);

            CommandManager commandManager = new CommandManager();
            commandManager.initializeCommands(dbManager, collectionManager);

            Handler handler = new Handler(commandManager, usersManager);
            ServerManager serverManager = new ServerManager(host, port, handler, dbManager);

            serverManager.start();
        } catch (Exception e) {
            logger.error("Ошибка запуска сервера: {}", e.getMessage());
            System.exit(1);
        }
    }
}
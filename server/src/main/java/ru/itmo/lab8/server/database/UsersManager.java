package ru.itmo.lab8.server.database;

import ru.itmo.lab8.common.exceptions.UserAlreadyExistsException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Класс для управления многопользовательским взаимодействием с коллекцией
 */
public class UsersManager {
    private final DatabaseManager databaseManager;
    private final Map<String, String> sessions = new HashMap<>();

    public UsersManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * @param username
     * @param password
     * @return true, если пользователь успешно зарегистрирован, иначе false
     * @throws SQLException
     * @throws UserAlreadyExistsException
     */
    public boolean registerUser(String username, String password) throws SQLException, UserAlreadyExistsException {
        try {
            String salt = PasswordEncoder.generateSalt();
            String passwordHash = PasswordEncoder.hashPassword(password, salt);
            return insertUser(username, salt, passwordHash);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                DatabaseManager.logger.warn("Пользователь {} уже существует", username);
                throw new UserAlreadyExistsException(username);
            } else {
                DatabaseManager.logger.error("Ошибка регистрации: {}", e.getMessage());
                throw e;
            }
        }
    }

    /**
     * @param username
     * @param salt
     * @param passwordHash
     * @return true, если данные пользователя успешно сохранены в БД
     * @throws SQLException
     */
    private boolean insertUser(String username, String salt, String passwordHash) throws SQLException {
        String query = "INSERT INTO users (name, password, salt) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, salt);
            stmt.executeUpdate();
            DatabaseManager.logger.info("Зарегистрирован новый пользователь: {}", username);
            return true;
        }
    }

    /**
     * @param username
     * @param password
     * @return токен сессии, если пользователь авторизован
     */
    public String authenticateUser(String username, String password) {
        try {
            String query = "SELECT password, salt FROM users WHERE name = ?";
            try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String salt = rs.getString("salt");
                    String computedHash = PasswordEncoder.hashPassword(password,salt);

                    if (storedHash.equals(computedHash)) {
                        return getSessionToken(username);
                    }
                }
            }
        } catch (SQLException e) {
            DatabaseManager.logger.error("Ошибка аутентификации: {}", e.getMessage());
        }
        return null;
    }

    /**
     * @param username
     * @return токен пользователя
     */
    private String getSessionToken(String username) {
        String sessionToken = generateSessionToken();
        sessions.put(sessionToken, username);
        DatabaseManager.logger.info("Пользователь {} авторизован", username);
        return sessionToken;
    }

    /**
     * @param sessionToken
     * @return имя пользователя, по токену сессии
     */
    public String getUserByToken(String sessionToken) {
        return sessions.get(sessionToken);
    }

    /**
     * @param sessionToken
     * @return true, если токен существует, иначе false
     */
    public boolean validateToken(String sessionToken) {
        return sessionToken != null && sessions.containsKey(sessionToken);
    }

    /**
     * @return сгенерированный токен сессии
     */
    private String generateSessionToken() {
        return UUID.randomUUID().toString();
    }
}

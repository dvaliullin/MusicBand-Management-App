package ru.itmo.lab8.server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.lab8.server.util.CollectionManager;
import ru.itmo.lab8.common.data.Coordinates;
import ru.itmo.lab8.common.data.Label;
import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.data.MusicGenre;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Класс для управления базой данных
 */
public class DatabaseManager {
    public static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private Connection connection;
    private final ReadWriteLock collectionLock = new ReentrantReadWriteLock();
    private final CollectionManager collectionManager;
    private final SQLQueryManager queryManager;

    public DatabaseManager(CollectionManager collectionManager, SQLQueryManager queryManager) {
        this.collectionManager = collectionManager;
        this.queryManager = queryManager;
    }

    /**
     * Подключение к БД
     * @param url
     * @param user
     * @param password
     * @throws SQLException
     */
    public void connect(String url, String user, String password) throws SQLException {
        try {
            setConnection(DriverManager.getConnection(url, user, password));
            logger.info("Подключение к БД успешно установлено");
        } catch (SQLException e) {
            logger.error("Ошибка подключения: SQLState={}, ErrorCode={}", e.getSQLState(), e.getErrorCode());
            logger.error("Сообщение: {}", e.getMessage());
            throw e;
        }
        initializeDatabase();
        loadCollection();
        logger.info("Подключение к БД установлено");
    }

    /**
     * Инициализация БД
     * @throws SQLException
     */
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(queryManager.createGenreEnum());
            stmt.execute(queryManager.createUsersTable());
            stmt.execute(queryManager.createLabelTable());
            stmt.execute(queryManager.createBandsTable());
            logger.info("Таблицы инициализированы");
        }
    }

    /**
     * Загрузка коллекции из БД в память
     */
    private void loadCollection() {
        collectionLock.writeLock().lock();
        try {
            collectionManager.clear();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(queryManager.selectMusicBand())) {

                while (rs.next()) {
                    MusicBand band = extractMusicBandFromResultSet(rs);
                    int key = rs.getInt("key");
                    collectionManager.addElement(key, band);
                }
                logger.info("Загружено {} элементов в коллекцию", collectionManager.getCollection().size());
            }
        } catch (SQLException e) {
            logger.error("Ошибка загрузки коллекции: {}", e.getMessage());
        } finally {
            collectionLock.writeLock().unlock();
        }
    }

    /**
     * Извлечение элементов коллекции из результата выполненного запроса
     * @param rs
     * @return
     * @throws SQLException
     */
    private MusicBand extractMusicBandFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int x = rs.getInt("x");
        Integer xValue = rs.wasNull() ? null : x;
        Long y = rs.getLong("y");
        Coordinates coordinates = new Coordinates(xValue != null ? xValue : 0, y);

        ZonedDateTime creationDate = rs.getTimestamp("creation_date")
                .toLocalDateTime()
                .atZone(ZoneId.systemDefault());

        Integer numberOfParticipants = rs.getObject("number_of_participants") != null ?
                rs.getInt("number_of_participants") : null;

        String genreStr = rs.getString("genre");
        MusicGenre genre = genreStr != null ? MusicGenre.valueOf(genreStr) : null;

        int labelId = rs.getInt("label_id");
        Label label = null;
        if (!rs.wasNull() && labelId > 0 && rs.getString("label_name") != null) {
            label = new Label(rs.getString("label_name"), rs.getLong("label_bands"));
        }

        MusicBand band = new MusicBand(id, name, coordinates, numberOfParticipants, genre, label);
        band.setCreationDate(creationDate);
        band.setOwner(rs.getString("owner_name"));
        return band;
    }

    /**
     * @param username
     * @return id пользователя с заданным именем
     * @throws SQLException
     */
    public int getUserIdByUsername(String username) throws SQLException {
        String query = "SELECT id FROM users WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Пользователь не найден");
    }

    /**
     * @param label
     * @return id лейбла
     * @throws SQLException
     */
    private int getOrCreateLabelId(Label label) throws SQLException {
        if (label == null) {
            return 0;
        }

        int labelId = getLabelId(label);
        if (labelId > 0) {
            return labelId;
        }

        return createLabel(label);
    }

    /**
     * @param label
     * @return id лейбла
     * @throws SQLException
     */
    private int getLabelId(Label label) throws SQLException {
        String findQuery = "SELECT id FROM label WHERE name = ? AND bands = ?";
        try (PreparedStatement stmt = connection.prepareStatement(findQuery)) {
            stmt.setString(1, label.getName());
            stmt.setLong(2, label.getBands());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return 0;
    }

    /**
     * @param label
     * @return id созданного лейбла
     * @throws SQLException
     */
    private int createLabel(Label label) throws SQLException {
        String insertQuery = "INSERT INTO label (name, bands) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setString(1, label.getName());
            stmt.setLong(2, label.getBands());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Добавление элемента в таблицу
     * @param key
     * @param band
     * @param username
     * @throws SQLException
     */
    public void addMusicBand(int key, MusicBand band, String username) throws SQLException {
        collectionLock.writeLock().lock();
        try {
            int userId = getUserIdByUsername(username);
            int labelId = getOrCreateLabelId(band.getLabel());
            int newId = getIdOfAdded(key, band, labelId, userId);
            band.setId(newId);
            band.setOwner(username);

            collectionManager.addElement(key, band);
            logger.info("Добавлен элемент с ключом {} пользователем {}", key, username);

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new SQLException("Элемент с таким ключом уже существует");
            }
            throw e;
        } finally {
            collectionLock.writeLock().unlock();
        }
    }

    /**
     * @param key
     * @param band
     * @param labelId
     * @param userId
     * @return id добавленного элемента
     * @throws SQLException
     */
    private int getIdOfAdded(int key, MusicBand band, int labelId, int userId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(queryManager.insertIntoMusicBand())) {
            stmt.setInt(1, key);
            stmt.setString(2, band.getName());
            stmt.setObject(3, band.getCoordinates().getX());
            stmt.setLong(4, band.getCoordinates().getY());
            stmt.setTimestamp(5, Timestamp.from(band.getCreationDate().toInstant()));
            stmt.setObject(6, band.getNumberOfParticipants());
            stmt.setString(7, band.getGenre() != null ? band.getGenre().name() : null);
            stmt.setInt(8, labelId > 0 ? labelId : null);
            stmt.setInt(9, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Не удалось получить сгенерированный ID при записи в базу данных");
    }

    /**
     * Обновление элемента
     * @param key
     * @param band
     * @param username
     */
    public void updateMusicBand(int key, MusicBand band, String username) {
        collectionLock.writeLock().lock();
        try {

            int labelId = getOrCreateLabelId(band.getLabel());

            try (PreparedStatement stmt = connection.prepareStatement(queryManager.updateMusicBand())) {
                stmt.setString(1, band.getName());
                stmt.setObject(2, band.getCoordinates().getX());
                stmt.setLong(3, band.getCoordinates().getY());
                stmt.setObject(4, band.getNumberOfParticipants());
                stmt.setString(5, band.getGenre() != null ? band.getGenre().name() : null);
                stmt.setInt(6, labelId > 0 ? labelId : null);
                stmt.setInt(7, key);
                stmt.setString(8, username);
            }

            band.setOwner(username);
            collectionManager.addElement(key, band);
        } catch (SQLException e) {
            logger.error("Ошибка обновления элемента: {}", e.getMessage());
        } finally {
            collectionLock.writeLock().unlock();
        }
    }

    /**
     * Удаление элемента
     * @param key
     * @param username
     * @throws SQLException
     */
    public void removeMusicBand(int key, String username) throws SQLException {
        collectionLock.writeLock().lock();
        try {
            String owner = getBandOwner(key);
            if (owner == null) {
                throw new SQLException("Такого элемента не существует");
            }
            if (!owner.equals(username)) {
                throw new SQLException("Нет прав на удаление этого элемента");
            }
            removeByKey(key, username);
            collectionManager.remove(key);
        } finally {
            collectionLock.writeLock().unlock();
        }
    }

    /**
     * Удаление элемента с заданным ключом
     * @param key
     * @param username
     * @throws SQLException
     */
    public void removeByKey(int key, String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(queryManager.deleteMusicBand())) {
            stmt.setInt(1, key);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }

    /**
     * Очистка таблицы
     * @param username
     * @throws SQLException
     */
    public void clearCollection(String username) throws SQLException {
        collectionLock.writeLock().lock();
        try {
            int userId = getUserIdByUsername(username);
            deleteByUserId(userId);
            loadCollection();
        } finally {
            collectionLock.writeLock().unlock();
        }
    }

    /**
     * Удаление элемента, принадлежащего пользователю с заданным id
     * @param userId
     * @throws SQLException
     */
    public void deleteByUserId(int userId) throws SQLException {
        String query = "DELETE FROM music_band WHERE owner_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Удаление элементов, больше заданного
     * @param band
     * @param username
     * @return число удаленных элементов
     * @throws SQLException
     */
    public int removeGreater(MusicBand band, String username) throws SQLException {
        collectionLock.writeLock().lock();
        try {
            int userId = getUserIdByUsername(username);

            try (PreparedStatement stmt = connection.prepareStatement(queryManager.deleteGreater())) {
                stmt.setInt(1, userId);
                stmt.setString(2, band.getName());
                stmt.setString(3, band.getName());
                stmt.setObject(4, band.getNumberOfParticipants());
                stmt.executeUpdate();
            }
            int oldCollectionSize = collectionManager.getCollectionSize();
            loadCollection();
            return oldCollectionSize - collectionManager.getCollectionSize();
        } finally {
            collectionLock.writeLock().unlock();
        }
    }

    /**
     * Удаление элементов с ключом, больше заданного
     * @param key
     * @param username
     * @return число удаленных элементов
     * @throws SQLException
     */
    public int removeGreaterKey(int key, String username) throws SQLException {
        collectionLock.writeLock().lock();
        try {
            int userId = getUserIdByUsername(username);
            String query = "DELETE FROM music_band WHERE owner_id = ? AND key > ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, key);
                stmt.executeUpdate();
            }
            int oldCollectionSize = collectionManager.getCollectionSize();
            loadCollection();
            return oldCollectionSize - collectionManager.getCollectionSize();
        } finally {
            collectionLock.writeLock().unlock();
        }
    }

    /**
     * @param key
     * @return имя владельца элемента
     * @throws SQLException
     */
    public String getBandOwner(int key) throws SQLException {
        String query = "SELECT u.name FROM music_band mb JOIN users u ON mb.owner_id = u.id WHERE mb.key = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
            return null;
        }
    }

    /**
     * Закрытие соединения с БД
     * @throws SQLException
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * @param id
     * @return ключ элемента с заданным id
     * @throws SQLException
     */
    public int getKeyById(int id) throws SQLException {
        String query = "SELECT key FROM music_band WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("key");
            }
        }
        throw new SQLException("Элемент с id " + id + " не найден");
    }

    /**
     * @param key
     * @param username
     * @return true, если владелец элемента подтвержден, иначе false
     */
    public boolean confirmOwner(int key, String username) {
        try {
            String owner = getBandOwner(key);
            if (!owner.equals(username)) {
                return false;
            }
        } catch (SQLException e) {
            logger.error("Ошибка подтверждения владельца элемента");
        }
        return true;
    }

    /**
     * Обновление элемента с заданным id
     * @param id
     * @param band
     * @param username
     */
    public void updateMusicBandById(int id, MusicBand band, String username) {
        try {
            int key = getKeyById(id);
            int labelId = getOrCreateLabelId(band.getLabel());
            boolean updated = isUpdated(id, band, labelId, username);

            if (updated) {
                band.setId(id);
                band.setOwner(username);
                collectionManager.addElement(key, band);
                logger.info("Обновлен элемент с id {} пользователем {}", id, username);
            }
        } catch (SQLException e) {
            logger.error("Ошибка обновления элемента: {}", e.getMessage());
        }
    }

    /**
     * @param id
     * @param band
     * @param labelId
     * @param username
     * @return true, если элемент успешно обновлен, иначе false
     * @throws SQLException
     */
    private boolean isUpdated(int id, MusicBand band, int labelId, String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(queryManager.updateById())) {
            stmt.setString(1, band.getName());
            stmt.setObject(2, band.getCoordinates().getX());
            stmt.setLong(3, band.getCoordinates().getY());
            stmt.setObject(4, band.getNumberOfParticipants());
            stmt.setString(5, band.getGenre() != null ? band.getGenre().name() : null);
            stmt.setInt(6, labelId > 0 ? labelId : null);
            stmt.setInt(7, id);
            stmt.setString(8, username);

            int updatedRows = stmt.executeUpdate();
            return updatedRows > 0;
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
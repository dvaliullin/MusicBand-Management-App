package ru.itmo.lab8.server.database;

/**
 * Класс управления SQL-запросами
 */
public class SQLQueryManager {

    /**
     * @return запрос на создание таблицы пользователей
     */
    public String createUsersTable() {
        return """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                name VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(128) NOT NULL,
                salt VARCHAR(64) NOT NULL
            )
        """;
    }

    /**
     * @return запрос на создание таблицы лейблов
     */
    public String createLabelTable() {
        return """
            CREATE TABLE IF NOT EXISTS label (
                id SERIAL PRIMARY KEY,
                name VARCHAR(50),
                bands BIGINT CHECK(bands >= 0)
            )
        """;
    }

    /**
     * @return запрос на создание таблицы музыкальных групп
     */
    public String createBandsTable() {
        return """
            CREATE TABLE IF NOT EXISTS music_band (
                id SERIAL PRIMARY KEY,
                key INTEGER UNIQUE NOT NULL,
                name VARCHAR(50) NOT NULL CHECK(length(name) > 0),
                x INTEGER,
                y BIGINT NOT NULL,
                creation_date TIMESTAMPTZ DEFAULT NOW() NOT NULL,
                number_of_participants INTEGER CHECK(number_of_participants > 0),
                genre musicgenre,
                label_id INTEGER REFERENCES label(id) ON DELETE CASCADE,
                owner_id INTEGER REFERENCES users(id) ON DELETE CASCADE
            )
        """;
    }

    /**
     * @return запрос на создание типа данных "жанр музыки"
     */
    public String createGenreEnum() {
        return """
            DO $$ BEGIN
                CREATE TYPE musicgenre AS ENUM (
                    'PSYCHEDELIC_CLOUD_RAP',
                    'POP',
                    'POST_ROCK'
                );
            EXCEPTION
                WHEN duplicate_object THEN null;
            END $$;
        """;
    }

    /**
     * @return запрос на получение элемента таблицы музыкальных групп
     */
    public String selectMusicBand() {
        return """
                SELECT mb.*, u.name as owner_name, l.name as label_name, l.bands as label_bands 
                FROM music_band mb 
                JOIN users u ON mb.owner_id = u.id
                LEFT JOIN label l ON mb.label_id = l.id
                ORDER BY mb.id
            """;
    }

    /**
     * @return запрос на добавление нового элемента в таблицу музыкальных групп
     */
    public String insertIntoMusicBand() {
        return """
                INSERT INTO music_band 
                (key, name, x, y, creation_date, number_of_participants, genre, label_id, owner_id)
                VALUES (?, ?, ?, ?, ?, ?, CAST(? AS musicgenre), ?, ?)
                RETURNING id
            """;
    }

    /**
     * @return запрос на обновление элемента в таблице музыкальных групп
     */
    public String updateMusicBand() {
        return """
                UPDATE music_band 
                SET name = ?, x = ?, y = ?, number_of_participants = ?, 
                    genre = CAST(? AS musicgenre), label_id = ?
                WHERE key = ? AND owner_id = (SELECT id FROM users WHERE name = ?)
            """;
    }

    /**
     * @return запрос на удаление элемента в таблице в музыкальных групп
     */
    public String deleteMusicBand() {
        return """
                DELETE FROM music_band 
                WHERE key = ? AND owner_id = (SELECT id FROM users WHERE name = ?)
            """;
    }

    /**
     * @return запрос на удаление элементов в таблице музыкальных групп, превышающих заданный
     */
    public String deleteGreater() {
        return """
                DELETE FROM music_band 
                WHERE owner_id = ? AND (
                    name > ? OR 
                    (name = ? AND number_of_participants > COALESCE(?, -1))
                )
            """;
    }

    /**
     * @return запрос на обновление элемента таблицы музыкальных групп с заданным id
     */
    public String updateById() {
        return """
            UPDATE music_band 
            SET name = ?, x = ?, y = ?, number_of_participants = ?, 
                genre = CAST(? AS musicgenre), label_id = ?
            WHERE id = ? AND owner_id = (SELECT id FROM users WHERE name = ?)
        """;
    }
}

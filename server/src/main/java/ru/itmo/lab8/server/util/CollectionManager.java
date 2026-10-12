package ru.itmo.lab8.server.util;

import ru.itmo.lab8.common.data.MusicBand;
import java.util.*;

/**
 * Класс для управления коллекцией
 */
public class CollectionManager {
    private TreeMap<Integer, MusicBand> collection = new TreeMap<>();
    private final Date initializationDate = new Date();

    /**
     * @return информация о коллекции
     */
    public String collectionInfo() {
        return "Тип: " + collection.getClass().getName() + "\n"
                + "Дата инициализации: " + initializationDate + "\n"
                + "Количество элементов: " + collection.size();
    }

    @Override
    public String toString() {
        if (collection.isEmpty()) {
            return "Коллекция пуста";
        }
        StringBuilder sb = new StringBuilder();
        collection.forEach((key, value) -> {
            sb.append("Ключ: ").append(key).append("\n");
            sb.append(value.toString()).append("\n\n");
        });
        return sb.toString();
    }

    public TreeMap<Integer, MusicBand> getCollection() {
        return collection;
    }

    /**
     * Добавляет элемент в коллекцию
     *
     * @param key
     * @param band
     */
    public void addElement(Integer key, MusicBand band) {
        collection.put(key, band);
    }

    /**
     * @param id
     * @return true, если найден заданный id
     */
    public boolean containsId(Integer id) {
        return collection.values().stream()
                .anyMatch(v -> v.getId().equals(id));
    }

    /**
     * @param key
     * @return элемент коллекции по ключу
     */
    public MusicBand getByKey(Integer key) {
        return collection.get(key);
    }

    /**
     * @param id
     * @return элемент коллекции по id
     */
    public MusicBand getById(Integer id) {
        return collection.values().stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .get();
    }

    /**
     * @param key
     * @return true, если есть элемент с заданным ключом
     */
    public boolean containsKey(Integer key) {
        return collection.containsKey(key);
    }

    /**
     * Удалить элемент коллекции с заданным ключом
     *
     * @param key
     */
    public void remove(Integer key) {
        collection.remove(key);
    }

    /**
     * Очистить коллекцию
     */
    public void clear() {
        collection.clear();
    }

    /**
     * @param number
     * @return число элементов с числом участников группы больше заданного
     */
    public long countGreaterThanNumberOfParticipants(Integer number) {
        return collection.values().stream()
                .filter(v -> v.getNumberOfParticipants() != null)
                .filter(v -> v.getNumberOfParticipants() > number)
                .count();
    }

    /**
     * @return отсортированную коллекцию
     */
    public TreeMap<Integer, MusicBand> getSortedCollection() {
        return collection.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(TreeMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll);
    }

    /**
     * @return список чисел участников группы всех элементов коллекции
     */
    public List<Integer> getNumberOfParticipantsList() {
        return collection.values().stream().map(MusicBand::getNumberOfParticipants)
                .filter(Objects::nonNull).toList();
    }

    /**
     * @param id
     * @return ключ элемента коллекции по его id
     */
    public Integer getKeyById(Integer id) {
        return collection.entrySet().stream()
                .filter(entry -> entry.getValue().equals(getById(id)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * @return размер коллекции
     */
    public int getCollectionSize() {
        return collection.size();
    }
}


package ru.itmo.lab8.common.data;

import java.io.Serializable;

/**
 * Лейбл
 */
public class Label implements Serializable {
    private final String name;
    private final long bands;

    public Label(String name, long bands) {
        this.name = name;
        this.bands = bands;
    }

    public String getName() {
        return name;
    }

    public long getBands() {
        return bands;
    }

    @Override
    public String toString() {
        return "{"
                + "name = '" + name + '\''
                + ", bands = '" + bands
                + '\''
                + '}';
    }
}

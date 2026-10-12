package ru.itmo.lab8.common.data;

import java.io.Serializable;

/**
 * Координаты
 */
public class Coordinates implements Serializable {
    private int x;
    private Long y; //Поле не может быть null

    public Coordinates(int x, Long y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public Long getY() {
        return y;
    }

    @Override
    public String toString() {
        return "{"
                + "x=" + x
                + ", y=" + y
                + '}';
    }
}

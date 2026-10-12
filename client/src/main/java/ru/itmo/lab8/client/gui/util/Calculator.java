package ru.itmo.lab8.client.gui.util;

import org.jxmapviewer.viewer.GeoPosition;
import ru.itmo.lab8.common.data.Coordinates;

import java.awt.*;
import java.util.Random;

/**
 * Класс для расчетов и вычислений
 */
public final class Calculator {
    /**
     * @param p1
     * @param p2
     * @return евклидово расстояние между двумя точками
     */
    public static double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * @param coordinates
     * @return нормализованные координаты
     */
    public static GeoPosition normalize(Coordinates coordinates) {
        double lat = coordinates.getY();
        double lon = coordinates.getX();
        lat = Math.max(-85, Math.min(85, lat));
        lon = lon % 360;
        if (lon > 180) lon -= 360;
        if (lon < -180) lon += 360;
        return new GeoPosition(lat, lon);
    }

    /**
     * @param time
     * @param hash
     * @return значение масштаба
     */
    public static double scale(long time, int hash) {
        return Math.sin(time * 0.004 + hash) * 0.15 + 1.0;
    }

    /**
     * @param numberOfParticipants
     * @return начальный радиус
     */
    public static int baseRadius(Integer numberOfParticipants) {
        int baseRadius = 15;
        if (numberOfParticipants != null) {
            baseRadius += Math.min(numberOfParticipants / 3, 20);
        }
        return baseRadius;
    }

    /**
     * @param random
     * @return случайный компонент цвета
     */
    public static int randomColorComponent(Random random) {
        return 60 + random.nextInt(150);
    }

    /**
     * @param h
     * @return толщину связующей балки для ноты
     */
    public static int beamThickness(int h) {
        return Math.max(3, h / 6);
    }

    /**
     * @param w
     * @return ширину ноты
     */
    public static float width(int w) {
        return Math.max(1.5f, w / 10.0f);
    }
}

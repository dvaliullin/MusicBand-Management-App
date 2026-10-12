package ru.itmo.lab8.client.gui.components.visualization;

import ru.itmo.lab8.client.gui.components.main.MainWindow;
import ru.itmo.lab8.client.gui.util.Calculator;
import ru.itmo.lab8.client.gui.util.LocalizationManager;
import ru.itmo.lab8.common.data.MusicBand;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Панель для визуализации объектов, просмотра карты и анимаций
 */
public class VisualizationPanel extends JPanel {
    private final MainWindow mainWindow;
    private final BandMapViewer bandMapViewer = new BandMapViewer();
    private Set<ColoredWaypoint> currentWaypoints = new HashSet<>();

    private final Map<Integer, Point> screenPoints = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> screenRadii = new ConcurrentHashMap<>();

    public VisualizationPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());

        initMap();

        new Timer(40, e -> bandMapViewer.repaint()).start();
    }

    /**
     * Инициализация карты мира
     */
    private void initMap() {
        System.setProperty("http.agent", "MusicBandApp/1.0 (ITMO Lab 8 Client; Swing Java Application)");

        bandMapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point mousePoint = e.getPoint();
                handleClickOnCurrentWaypoints(mousePoint, e);
            }
        });

        add(bandMapViewer, BorderLayout.CENTER);
    }

    /**
     * @param e
     * Показывает меню с информацией об объекте и возможными действиями с ним при нажатии на значок на карте
     */
    private void showMapEmptySpacePopupMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem(LocalizationManager.getString("cmd_add"));

        Point clickPoint = e.getPoint();
        GeoPosition geoPos = bandMapViewer.convertPointToGeoPosition(clickPoint);

        addItem.addActionListener(al -> {
            mainWindow.triggerInsertWithDefaultCoords(geoPos);
        });

        menu.add(addItem);
        menu.show(bandMapViewer, e.getX(), e.getY());
    }

    private void showBandPopup(MusicBand band) {
        SwingUtilities.invokeLater(() -> {
            String info = getBandPopupMenu(band);
            mainWindow.handleOptionTrigger(getOptionTrigger(info));
        });
    }

    /**
     * Показывает контекстное меню действий при клике правой кнопкой мыши на существующий маркер
     */
    private void showPopupMenu(MouseEvent e, Integer key) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem(LocalizationManager.getString("cmd_update"));
        JMenuItem deleteItem = new JMenuItem(LocalizationManager.getString("cmd_delete"));

        mainWindow.addEditItemTrigger(editItem);
        mainWindow.addDeleteItemTrigger(deleteItem);

        menu.add(editItem);
        menu.add(deleteItem);
        menu.addSeparator();

        MusicBand band = findBandInCurrentWaypoints(key);

        if (band != null) {
            JMenuItem infoItem = new JMenuItem(band.getName() + " (" + band.getOwner() + ")");
            infoItem.setEnabled(false);
            menu.add(infoItem);
        }

        menu.show(bandMapViewer, e.getX(), e.getY());
    }

    /**
     * @param elements
     * Установить элементы коллекции в виде значков на карту
     */
    public synchronized void setElements(Map<Integer, MusicBand> elements) {
        Set<ColoredWaypoint> waypoints = getWaypointsOfElements(elements);

        WaypointPainter<ColoredWaypoint> painter = new WaypointPainter<>();
        painter.setWaypoints(waypoints);
        painter.setRenderer(new ColoredWaypointRenderer(screenPoints, screenRadii, mainWindow));

        setCurrentWaypoints(waypoints);
        bandMapViewer.setOverlayPainter(painter);
    }

    /**
     * @param band
     * @return цвет значка по хэш-коду владельца элемента
     */
    private Color getColorByOwnerHashCode(MusicBand band) {
        String owner = band.getOwner() != null ? band.getOwner() : "";
        Random random = new Random(owner.hashCode());
        return new Color(
                Calculator.randomColorComponent(random),
                Calculator.randomColorComponent(random),
                Calculator.randomColorComponent(random)
        );
    }

    /**
     * Вложенный класс для цветных значков
     */
    private static class ColoredWaypoint extends DefaultWaypoint {
        private final Color color;
        private final MusicBand musicBand;
        private final int key;

        public ColoredWaypoint(GeoPosition position, Color color, MusicBand musicBand, int key) {
            super(position);
            this.color = color;
            this.musicBand = musicBand;
            this.key = key;
        }

        public Color getColor() {
            return color;
        }

        public MusicBand getMusicBand() {
            return musicBand;
        }

        public int getKey() {
            return key;
        }
    }

    /**
     * @param screenPoints
     * @param screenRadii
     * @param mainWindow
     * Вложенный класс для визуализации элементов на карте
     */
    private record ColoredWaypointRenderer(Map<Integer, Point> screenPoints, Map<Integer, Integer> screenRadii,
                                           MainWindow mainWindow) implements WaypointRenderer<ColoredWaypoint> {

        /**
         * @param g
         * @param map
         * @param wp
         * Изображение значков на карте
         */
        @Override
        public void paintWaypoint(Graphics2D g, JXMapViewer map, ColoredWaypoint wp) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
            int globalX = (int) point.getX();
            int globalY = (int) point.getY();

            int radius = calcRadius(wp);

            screenPoints.put(wp.getKey(), getScreenPoint(map, globalX, globalY));
            screenRadii.put(wp.getKey(), radius);

            if (mainWindow.getSelectedKey() != null && mainWindow.getSelectedKey().equals(wp.getKey())) {
                setSelectionFrame(g, globalX, globalY, radius);
            }

            setPulsatingArea(g, wp, radius, globalX, globalY);

            setMarkerBody(g, wp, globalX, globalY, radius);

            drawMusicNote(g, globalX, globalY, radius);
        }

        /**
         * @param g
         * @param cx
         * @param cy
         * @param radius
         * Изображение ноты
         */
        private void drawMusicNote(Graphics2D g, int cx, int cy, int radius) {
            int w = (int) (radius * 0.9);
            int h = (int) (radius * 0.9);

            g.setColor(Color.WHITE);

            // Левая головка ноты
            g.fillOval(cx - w / 3, cy + h / 6, w / 4, h / 5);
            // Правая головка ноты
            g.fillOval(cx + w / 8, cy + h / 14, w / 4, h / 5);

            g.setStroke(new BasicStroke(Calculator.width(w)));

            // Левый штиль
            g.drawLine(cx - w / 3 + w / 4, cy + h / 6 + h / 10, cx - w / 3 + w / 4, cy - h / 3);
            // Правый штиль
            g.drawLine(cx + w / 8 + w / 4, cy + h / 14 + h / 10, cx + w / 8 + w / 4, cy - h / 3 - h / 10);

            // Связующая балка
            Path2D beam = drawBeam(cx, cy, w, h);
            g.fill(beam);
        }

        /**
         * @param map
         * @param globalX
         * @param globalY
         * @return точку на карте по координатам
         */
        private Point getScreenPoint(JXMapViewer map, int globalX, int globalY) {
            Rectangle viewportBounds = map.getViewportBounds();
            int screenX = globalX - (int) viewportBounds.getX();
            int screenY = globalY - (int) viewportBounds.getY();
            return new Point(screenX, screenY);
        }

        /**
         * @param wp
         * @return радиус окружности для значка
         */
        private int calcRadius(ColoredWaypoint wp) {
            MusicBand band = wp.getMusicBand();
            int baseRadius = Calculator.baseRadius(band.getNumberOfParticipants());
            long time = System.currentTimeMillis();
            int hash = band.getOwner() != null ? band.getOwner().hashCode() : 0;
            double scale = Calculator.scale(time, hash);
            return (int) (baseRadius * scale);
        }

        /**
         * @param g
         * @param globalX
         * @param globalY
         * @param radius
         * Установить рамку выбора
         */
        private void setSelectionFrame(Graphics2D g, int globalX, int globalY, int radius) {
            g.setColor(new Color(255, 235, 59, 180));
            g.setStroke(new BasicStroke(3));
            g.drawOval(globalX - radius - 5, globalY - radius - 5, (radius + 5) * 2, (radius + 5) * 2);
        }

        /**
         * @param g
         * @param wp
         * @param radius
         * @param globalX
         * @param globalY
         * Установить область пульсации
         */
        private void setPulsatingArea(Graphics2D g, ColoredWaypoint wp, int radius, int globalX, int globalY) {
            g.setColor(new Color(wp.getColor().getRed(), wp.getColor().getGreen(), wp.getColor().getBlue(), 55));
            int waveRadius = (int) (radius * 1.35);
            g.fillOval(globalX - waveRadius, globalY - waveRadius, waveRadius * 2, waveRadius * 2);
        }

        /**
         * @param g
         * @param wp
         * @param globalX
         * @param globalY
         * @param radius
         * Установить тело маркера
         */
        private void setMarkerBody(Graphics2D g, ColoredWaypoint wp, int globalX, int globalY, int radius) {
            g.setColor(wp.getColor());
            g.fillOval(globalX - radius, globalY - radius, radius * 2, radius * 2);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(globalX - radius, globalY - radius, radius * 2, radius * 2);
        }

        /**
         * @param cx
         * @param cy
         * @param w
         * @param h
         * @return связующую балку для изображения ноты
         */
        private Path2D drawBeam(int cx, int cy, int w, int h) {
            Path2D beam = new Path2D.Double();
            int beamThickness = Calculator.beamThickness(h);
            int lx = cx - w / 3 + w / 4;
            int ly = cy - h / 3;
            int rx = cx + w / 8 + w / 4;
            int ry = cy - h / 3 - h / 10;

            beam.moveTo(lx, ly);
            beam.lineTo(rx, ry);
            beam.lineTo(rx, ry + beamThickness);
            beam.lineTo(lx, ly + beamThickness);
            beam.closePath();
            return beam;
        }
    }

    /**
     * @param pt
     * @param mousePoint
     * @param r
     * @return проверка нажатия на маркер
     */
    private boolean isClickedOnMarker(Point pt, Point mousePoint, Integer r) {
        if (pt != null && r != null) {
            double dist = Calculator.dist(mousePoint, pt);
            return dist <= r;
        }
        return false;
    }

    /**
     * @param e
     * @param cwp
     * Обработка нажатия на маркер
     */
    private void handleClickedOnMarker(MouseEvent e, ColoredWaypoint cwp) {
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            showPopupMenu(e, cwp.getKey());
        } else {
            mainWindow.changeSelection(cwp.getKey());
            showBandPopup(cwp.getMusicBand());
        }
    }

    /**
     * @param mousePoint
     * @param e
     * Обработка нажатия на значок
     */
    private void handleClickOnCurrentWaypoints(Point mousePoint, MouseEvent e) {
        boolean clickedOnMarker = false;
        for (ColoredWaypoint cwp : currentWaypoints) {
            Point pt = screenPoints.get(cwp.getKey());
            Integer r = screenRadii.get(cwp.getKey());

            clickedOnMarker = isClickedOnMarker(pt, mousePoint, r);

            if (clickedOnMarker) {
                handleClickedOnMarker(e, cwp);
                break;
            }
        }
        if (!clickedOnMarker && (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger())) {
            showMapEmptySpacePopupMenu(e);
        }
    }

    /**
     * @param band
     * @return меню с информацией об объекте
     */
    private String getBandPopupMenu(MusicBand band) {
        return String.format(
                LocalizationManager.getString("col_name") + ": %s\n" +
                        LocalizationManager.getString("col_x") + ": %d\n" +
                        LocalizationManager.getString("col_y") + ": %d\n" +
                        LocalizationManager.getString("col_owner") + ": %s",
                band.getName(),
                band.getCoordinates().getX(),
                band.getCoordinates().getY(),
                band.getOwner()
        );
    }

    /**
     * @param key
     * @return объект с выбранным значком на карте
     */
    private MusicBand findBandInCurrentWaypoints(Integer key) {
        return currentWaypoints.stream()
                .filter(wp -> wp.getKey() == key)
                .map(ColoredWaypoint::getMusicBand)
                .findFirst()
                .orElse(null);
    }

    /**
     * @param elements
     * @return коллекцию из значков элементов
     */
    private Set<ColoredWaypoint> getWaypointsOfElements(Map<Integer, MusicBand> elements) {
        return elements.entrySet().stream()
                .filter(entry -> entry.getValue().getCoordinates() != null)
                .map(entry -> {
                    GeoPosition pos = Calculator.normalize(entry.getValue().getCoordinates());
                    Color color = getColorByOwnerHashCode(entry.getValue());
                    return new ColoredWaypoint(pos, color, entry.getValue(), entry.getKey());
                })
                .collect(Collectors.toSet());
    }

    /**
     * @param info
     * @return триггер выбора действий с элементов
     */
    private int getOptionTrigger(String info) {
        String cancelText = LocalizationManager.getString("cancel_btn");
        String updateText = LocalizationManager.getString("cmd_update");
        String removeText = LocalizationManager.getString("cmd_delete");
        String[] options = {cancelText, updateText, removeText};

        return JOptionPane.showOptionDialog(
                this,
                info,
                LocalizationManager.getString("dialog_band_title"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                cancelText
        );
    }

    public void setCurrentWaypoints(Set<ColoredWaypoint> waypoints) {
        this.currentWaypoints = waypoints;
    }
}
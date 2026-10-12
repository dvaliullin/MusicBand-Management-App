package ru.itmo.lab8.client.gui.components.visualization;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.event.MouseInputListener;

/**
 * Класс для инициализации карты мира
 */
public class BandMapViewer extends JXMapViewer {
    public BandMapViewer() {
        TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
        TileFactory tileFactory = new DefaultTileFactory(info);
        setTileFactory(tileFactory);

        setZoom(8);
        setAddressLocation(new GeoPosition(59.9343, 30.3351));

        MouseInputListener mouseInputListener = new PanMouseInputListener(this);
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
        addMouseWheelListener(new ZoomMouseWheelListenerCenter(this));
    }
}

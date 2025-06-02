package map;


import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.GeoPosition;
import java.util.List;

public class HeatMap {
    List<HeatPoint> heatPoints;
    GeoPosition startPos;

    JXMapViewer heatMap;

    public HeatMap(GeoPosition startPos, List<HeatPoint> heatPoints, JXMapViewer syncMap) {
        this.heatPoints = heatPoints;
        this.startPos = startPos;
        this.heatMap = createHeatMap(syncMap);
    }

    private JXMapViewer createHeatMap(JXMapViewer syncMap) {
        JXMapViewer map = new JXMapViewer();
        TileUtil tileUtil = new TileUtil(heatPoints, null);
        map.setTileFactory(tileUtil.getTileFactory(1));
        map.setZoom(syncMap.getZoom());
        map.setAddressLocation(startPos);

        syncMap.addPropertyChangeListener("center", _ -> {
            if (!map.getAddressLocation().equals(syncMap.getAddressLocation())) {
                map.setAddressLocation(syncMap.getAddressLocation());
            }
        });
        map.addPropertyChangeListener("center", _ -> {
            if (!syncMap.getAddressLocation().equals(map.getAddressLocation())) {
                syncMap.setAddressLocation(map.getAddressLocation());
            }
        });

        syncMap.addPropertyChangeListener("zoom", _ -> {
            if (map.getZoom() != syncMap.getZoom()) {
                map.setZoom(syncMap.getZoom());
            }
        });
        map.addPropertyChangeListener("zoom", _ -> {
            if (syncMap.getZoom() != map.getZoom()) {
                syncMap.setZoom(map.getZoom());
            }
        });

        map.addMouseListener(new PanMouseInputListener(map));
        map.addMouseMotionListener(new PanMouseInputListener(map));
        map.addMouseListener(new CenterMapListener(map));
        map.addMouseWheelListener(new ZoomMouseWheelListenerCursor(map));
        map.addKeyListener(new PanKeyListener(map));

        final int initialZoom = map.getZoom();
        map.addPropertyChangeListener("zoom", _ -> {
            int zoom = map.getZoom();
            int maxZoom = map.getTileFactory().getInfo().getMaximumZoomLevel();
            int minZoom = Math.max(0, initialZoom - 5);
            if (zoom < minZoom) map.setZoom(minZoom);
            if (zoom > maxZoom) map.setZoom(maxZoom);
        });
        return map;
    }

    public JXMapViewer getHeatMap() {
        return heatMap;
    }

}

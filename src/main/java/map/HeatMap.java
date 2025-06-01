package map;


import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import java.util.List;

public class HeatMap {
    List<HeatPoint> heatPoints;
    GeoPosition startPos;

    public HeatMap(GeoPosition startPos, List<HeatPoint> heatPoints) {
        this.heatPoints = heatPoints;
        this.startPos = startPos;
    }

    public JXMapViewer getHeatMap() {
        JXMapViewer map = new JXMapViewer();
        TileUtil tileUtil = new TileUtil(heatPoints, new int[]{7, 13});
        map.setTileFactory(tileUtil.getTileFactory(1));
        map.setZoom(7);
        map.setAddressLocation(startPos);
        return map;
    }

}

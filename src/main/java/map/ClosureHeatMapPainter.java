package map;

import closureAnalysis.StopData;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static map.HeatMapPainter.constructImage;

public class ClosureHeatMapPainter implements Painter<JXMapViewer> {
    private final List<StopData> stopDataList;
    private final int radius;
    private final int blur;

    public ClosureHeatMapPainter(List<StopData> stopDataList) {
        this(stopDataList, 80, 60);
    }

    public ClosureHeatMapPainter(List<StopData> stopDataList, int radius, int blur) {
        this.stopDataList = stopDataList;
        this.radius = radius;
        this.blur = blur;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        if (stopDataList == null || stopDataList.isEmpty()) return;

        float[][] intensity = new float[w][h];
        float maxIntensity = 0f;

        for (StopData stop : stopDataList) {
            GeoPosition pos = new GeoPosition(stop.getStopLat(), stop.getStopLon());
            Point2D pt = map.getTileFactory().geoToPixel(pos, map.getZoom());
            int cx = (int) (pt.getX() - map.getViewportBounds().getX());
            int cy = (int) (pt.getY() - map.getViewportBounds().getY());

            float score = (float) stop.getScore();  // Already normalized [0–1] range is assumed
            int r = radius;
            int b = blur;
            int size = r + 2 * b;
            float sigma = (r + b) / 2f;
            float twoSigmaSq = 2 * sigma * sigma;

            for (int dx = -size / 2; dx <= size / 2; dx++) {
                int x = cx + dx;
                if (x < 0 || x >= w) continue;

                for (int dy = -size / 2; dy <= size / 2; dy++) {
                    int y = cy + dy;
                    if (y < 0 || y >= h) continue;

                    float distSq = dx * dx + dy * dy;
                    float kernel = (float) Math.exp(-distSq / twoSigmaSq);
                    intensity[x][y] += score * kernel;
                    maxIntensity = Math.max(maxIntensity, intensity[x][y]);
                }
            }
        }

        BufferedImage heatmap = constructImage(w, h, maxIntensity, intensity);
        g.drawImage(heatmap, 0, 0, null);
    }


}

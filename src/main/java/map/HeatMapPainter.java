package map;

import models.HeatPoint;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class HeatMapPainter implements Painter<JXMapViewer> {
    private final List<HeatPoint> heatPoints;
    private final int radius;
    private final int blur;

    public HeatMapPainter(List<HeatPoint> heatPoints) {
        this(heatPoints, 40, 30);
    }

    public HeatMapPainter(List<HeatPoint> heatPoints, int radius, int blur) {
        this.heatPoints = heatPoints;
        this.radius = radius;
        this.blur = blur;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        if (heatPoints == null || heatPoints.isEmpty()) return;
        // intensity map
        float[][] intensity = new float[w][h];
        float maxIntensity = 0f;
        for (HeatPoint hp : heatPoints) {
            GeoPosition pos = new GeoPosition(hp.latitude(), hp.longitude());
            Point2D pt = map.getTileFactory().geoToPixel(pos, map.getZoom());
            int cx = (int) (pt.getX() - map.getViewportBounds().getX());
            int cy = (int) (pt.getY() - map.getViewportBounds().getY());
            float pointIntensity = (float) Math.min(1.0, hp.time() / 60.0f); // normalize
            int r = radius;
            int b = blur;
            int size = r + 2 * b; // total size of the kernel
            float sigma = (r + b) / 2f; // standard deviation for Gaussian
            float twoSigmaSq = 2 * sigma * sigma;
            for (int dx = -size / 2; dx <= size / 2; dx++) {
                int x = cx + dx;  // center x coordinate
                if (x < 0 || x >= w) continue;
                for (int dy = -size / 2; dy <= size / 2; dy++) {
                    int y = cy + dy; // center y coordinate
                    if (y < 0 || y >= h) continue;
                    float distSq = dx * dx + dy * dy; // squared distance from center
                    float kernel = (float) Math.exp(-distSq / twoSigmaSq); // Gaussian kernel
                    intensity[x][y] += pointIntensity * kernel;
                    if (intensity[x][y] > maxIntensity) maxIntensity = intensity[x][y];
                }
            }
        }
        // coloring and rendering
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                float norm = maxIntensity == 0 ? 0 : intensity[x][y] / maxIntensity;
                if (norm < 0.01f) continue; // skip very low intensity
                Color color = getColorFromGradient(norm);
                int alpha = (int) (norm * 180); // control opacity
                int rgb = (color.getRGB() & 0x00ffffff) | (alpha << 24);
                out.setRGB(x, y, rgb);
            }
        }
        g.drawImage(out, 0, 0, null);
    }

    private Color getColorFromGradient(float value) {
        // Green -> Yellow -> Orange -> Red -> Purple
        float[] stops = {0f, 0.2f, 0.5f, 0.8f, 1f};
        Color[] colors = {
                new Color(0, 255, 0),
                new Color(255, 255, 0),
                new Color(255, 140, 0),
                new Color(255, 0, 0),
                new Color(128, 0, 128)
        };
        for (int i = 0; i < stops.length - 1; i++) {
            if (value <= stops[i + 1]) {
                float ratio = (value - stops[i]) / (stops[i + 1] - stops[i]);
                return blend(colors[i], colors[i + 1], ratio);
            }
        }
        return colors[colors.length - 1];
    }

    private Color blend(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }
}


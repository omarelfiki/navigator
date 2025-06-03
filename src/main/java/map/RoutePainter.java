package map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.List;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;
import router.Node;

public class RoutePainter implements Painter<JXMapViewer> {
    private final List<Node> path;

    public RoutePainter(List<Node> path) {
        this.path = path;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the route
        drawRoute(g, map);
        g.dispose();
    }

    private void drawRoute(Graphics2D g, JXMapViewer map) {
        int lastX = 0;
        int lastY = 0;
        boolean first = true;

        for (Node node : path) {
            GeoPosition position = new GeoPosition(node.getStop().getStopLat(), node.getStop().getStopLon());
            Point2D pt = map.getTileFactory().geoToPixel(position, map.getZoom());

            if (first) {
                first = false;
            } else {
                if ("WALK".equals(node.getMode())) {
                    g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0)); // Dashed line
                    g.setColor(Color.BLUE); // Color for walking
                } else if ("TRANSFER".equals(node.getMode())) {
                    g.setStroke(new BasicStroke(5)); // Solid line
                    g.setColor(Color.RED); // Color for transfers
                } else {
                    g.setStroke(new BasicStroke(5)); // Solid line
                    g.setColor(Color.RED); // Default color for other modes
                }
                g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
            }

            lastX = (int) pt.getX();
            lastY = (int) pt.getY();
        }
    }
}
package map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import router.Node;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class WayPoint {
    public static void addWaypoint(List<Node> path) {
        MapIntegration mapIntegration = MapProvider.getInstance();
        JXMapViewer map = mapIntegration.getMap();

        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        Set<Waypoint> waypoints = new HashSet<>();

        painters.add(new RoutePainter(path));

        if (!path.isEmpty()) {
            Node first = path.getFirst();
            Node last = path.getLast();
            waypoints.add(new DefaultWaypoint(
                    new GeoPosition(first.getStop().getStopLat(), first.getStop().getStopLon())
            ));
            if (path.size() > 1) {
                waypoints.add(new DefaultWaypoint(
                        new GeoPosition(last.getStop().getStopLat(), last.getStop().getStopLon())
                ));
            }
        }

        // Add waypoint painter for the first and last nodes
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        painters.add(waypointPainter);

        // Custom bullet painter for all other nodes
        painters.add((g, map1, _, _) -> {
            Rectangle rect = map1.getViewportBounds();
            g = (Graphics2D) g.create();
            g.translate(-rect.x, -rect.y);

            for (Node node : path) {
                if (node.equals(path.getLast()) || node.equals(path.getFirst())) {
                    continue;
                }
                GeoPosition pos = new GeoPosition(node.getStop().getStopLat(), node.getStop().getStopLon());
                Point2D pt = map1.getTileFactory().geoToPixel(pos, map1.getZoom());

                g.setColor(Color.WHITE);
                g.fillOval((int) pt.getX() - 6, (int) pt.getY() - 6, 12, 12);

                g.setStroke(new BasicStroke(3));
                g.setColor(Color.BLACK);
                g.drawOval((int) pt.getX() - 7, (int) pt.getY() - 7, 14, 14);
            }
            g.dispose();
        });

        // Combine all painters
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painters);
        map.setOverlayPainter(compoundPainter);

        // Zoom to fit the first and last markers
        Set<GeoPosition> geoPositions = new HashSet<>();
        for (Waypoint waypoint : waypoints) {
            if (waypoint instanceof DefaultWaypoint) {
                geoPositions.add(waypoint.getPosition());
            }
        }
        map.zoomToBestFit(geoPositions, 0.7);
    }

    public static void clearRoute() {
        MapIntegration mapIntegration = MapProvider.getInstance();
        JXMapViewer map = mapIntegration.getMap();
        map.setOverlayPainter(null);
    }

}
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

    for (int i = 0; i < path.size(); i++) {
        Node node = path.get(i);
        double[] coords = {node.getStop().getStopLat(), node.getStop().getStopLon()};
        GeoPosition position = new GeoPosition(coords[0], coords[1]);

        if (i == 0 || i == path.size() - 1) {
            waypoints.add(new DefaultWaypoint(position)); //default marker for start and end
        } else {
            painters.add((g, map1, _, _) -> {
                Point2D pt = map1.getTileFactory().geoToPixel(position, map1.getZoom());
                g.setColor(Color.YELLOW); // Ensure a visible color
                g.fillOval((int) pt.getX() - 5, (int) pt.getY() - 5, 30, 30);
            });
        }
    }

    // Add the line connecting all points
    painters.add(new RoutePainter(path));

    // Add waypoint painter for first and last nodes
    WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
    waypointPainter.setWaypoints(waypoints);
    painters.add(waypointPainter);

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

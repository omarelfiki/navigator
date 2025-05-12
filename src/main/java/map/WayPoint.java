package map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import util.Node;

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
    List<GeoPosition> track = new ArrayList<>();

    for (int i = 0; i < path.size(); i++) {
        Node node = path.get(i);
        double[] coords = {node.stop.stopLat, node.stop.stopLon};
        GeoPosition position = new GeoPosition(coords[0], coords[1]);
        track.add(position);

        if (i == 0 || i == path.size() - 1) {
            waypoints.add(new DefaultWaypoint(position)); //default marker for start and end
        } else {
            painters.add((g, map1, w, h) -> {
                Point2D pt = map1.getTileFactory().geoToPixel(position, map1.getZoom());
                g.setColor(Color.RED); // Ensure a visible color
                g.fillOval((int) pt.getX() - 5, (int) pt.getY() - 5, 20, 20);
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
            geoPositions.add(((DefaultWaypoint) waypoint).getPosition());
        }
    }
    map.zoomToBestFit(geoPositions, 0.7);
}
}

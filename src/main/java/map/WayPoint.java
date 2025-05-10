package map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import java.util.*;

public class WayPoint {
    public static void addWaypoint(double[] ocoords, double[] dcoords, JXMapViewer map) {
        GeoPosition op = new GeoPosition(ocoords[0], ocoords[1]);
        GeoPosition dp = new GeoPosition(dcoords[0], dcoords[1]);
        List<GeoPosition> track = Arrays.asList(op, dp);
        RoutePainter routePainter = new RoutePainter(track);

        map.zoomToBestFit(new HashSet<>(track), 0.7);
        Set<Waypoint> waypoints = new HashSet<>(Arrays.asList(
                new DefaultWaypoint(op),
                new DefaultWaypoint(dp)
        ));

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);

        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        map.setOverlayPainter(painter);
    }
}

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBService {

    private final DBaccess db;

    public DBService(DBaccess db) {
        this.db = db;
    }

    public List<Stop> getClosestStops(double lat, double lon, int limit) {
        List<Stop> stops = new ArrayList<>();
        String query = """
                SELECT stop_id, stop_name, stop_lat, stop_lon,
                SQRT(POW(stop_lat - ?, 2) + POW(stop_lon - ?, 2)) AS distance
                FROM stops
                ORDER BY distance ASC
                LIMIT ?
                """;
        try (PreparedStatement stmt = db.conn.prepareStatement(query)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stops.add(new Stop(
                        rs.getString("stop_id"),
                        rs.getString("stop_name"),
                        rs.getDouble("stop_lat"),
                        rs.getDouble("stop_lon")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stops;
    }

    public List<Trip> getTripsByStop(String stopId) {
        List<Trip> trips = new ArrayList<>();
        String query = """
                SELECT DISTINCT t.trip_id, t.route_id
                FROM trips t
                JOIN stop_times st ON t.trip_id = st.trip_id
                WHERE st.stop_id = ?
                """;
        try (PreparedStatement stmt = db.conn.prepareStatement(query)) {
            stmt.setString(1, stopId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String routeId = rs.getString("route_id");
                trips.add(new Trip(tripId, null, null, new Route(routeId, null, "", "", ""), "", "", "", false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trips;
    }

    public List<StopTime> getStopTimesByTripId(String tripId) {
        List<StopTime> stopTimes = new ArrayList<>();
        String query = """
                SELECT st.stop_id, s.stop_name, s.stop_lat, s.stop_lon,
                       st.departure_time, st.arrival_time, st.stop_sequence
                FROM stop_times st
                JOIN stops s ON st.stop_id = s.stop_id
                WHERE st.trip_id = ?
                ORDER BY st.stop_sequence ASC
                """;
        try (PreparedStatement stmt = db.conn.prepareStatement(query)) {
            stmt.setString(1, tripId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Stop stop = new Stop(
                        rs.getString("stop_id"),
                        rs.getString("stop_name"),
                        rs.getDouble("stop_lat"),
                        rs.getDouble("stop_lon")
                );
                StopTime stopTime = new StopTime(
                        stop, null,
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getInt("stop_sequence")
                );
                stopTimes.add(stopTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stopTimes;
    }
}

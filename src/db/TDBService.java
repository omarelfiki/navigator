package db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.*;

public class TDBService {

    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;

    public TDBService(String dbUrl, String dbUser, String dbPassword) {
        this.DB_URL = dbUrl;
        this.DB_USER = dbUser;
        this.DB_PASSWORD = dbPassword;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public Stop getStopById(String stopId) {
        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops WHERE stop_id = ?";
        return executeQueryObject(sql, new Object[]{stopId}, this::mapStop);
    }

    public List<Stop> getAllStops() {
        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops";
        return executeQueryList(sql, this::mapStop);
    }

    public List<StopTime> getStopTimesByTripId(String tripId) {
        String sql = "SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence FROM stop_times WHERE trip_id = ?";
        return executeQueryList(sql, new Object[]{tripId}, this::mapStopTime);
    }

    public List<StopTime> getStopTimesByStopId(String stopId) {
        String sql = "SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence FROM stop_times WHERE stop_id = ?";
        return executeQueryList(sql, new Object[]{stopId}, this::mapStopTime);
    }

    public List<StopTime> getFutureDepartures(String stopId, int afterTime) {
        String sql = "SELECT st.trip_id, st.stop_id, st.arrival_time, st.departure_time, st.stop_sequence " +
                "FROM stop_times st " +
                "JOIN trips tr ON st.trip_id = tr.trip_id " +
                "WHERE st.stop_id = ? AND TIME_TO_SEC(TIME(st.departure_time)) > ? * 60 " +
                "ORDER BY st.departure_time";
        return executeQueryList(sql, new Object[]{stopId, afterTime}, this::mapStopTime);
    }

    public Trip getTripById(String tripId) {
        String sql = "SELECT tr.trip_id, tr.route_id, tr.trip_headsign, rt.route_short_name, rt.route_long_name " +
                "FROM trips tr " +
                "JOIN routes rt ON tr.route_id = rt.route_id " +
                "WHERE tr.trip_id = ?";
        return executeQueryObject(sql, new Object[]{tripId}, this::mapTrip);
    }

    public Route getRouteById(String routeId) {
        String sql = "SELECT route_id, route_short_name, route_long_name FROM routes WHERE route_id = ?";
        return executeQueryObject(sql, new Object[]{routeId}, this::mapRoute);
    }

    private Stop mapStop(ResultSet rs) throws SQLException {
        return new Stop(
                rs.getString("stop_id"),
                rs.getString("stop_name"),
                rs.getDouble("stop_lat"),
                rs.getDouble("stop_lon")
        );
    }

    private StopTime mapStopTime(ResultSet rs) throws SQLException {
        Route route = new Route(
                rs.getString("route_id"),
                rs.getString("route_short_name"),
                rs.getString("route_long_name")
        );
        Stop stop = new Stop(
                rs.getString("stop_id"),
                rs.getString("stop_name"),
                rs.getDouble("stop_lat"),
                rs.getDouble("stop_lon")
        );
        Trip trip = new Trip(
                rs.getString("trip_id"),
                route,
                rs.getString("trip_headsign")
        );
        return new StopTime(
                stop,
                trip,
                rs.getString("arrival_time"),
                rs.getString("departure_time"),
                rs.getInt("stop_sequence")
        );
    }

    private Trip mapTrip(ResultSet rs) throws SQLException {
        Route route = new Route(
                rs.getString("route_id"),
                rs.getString("route_short_name"),
                rs.getString("route_long_name")
        );
        return new Trip(
                rs.getString("trip_id"),
                null,
                null,
                route,
                rs.getString("trip_headsign"),
                null,
                null,
                false
        );
    }

    private Route mapRoute(ResultSet rs) throws SQLException {
        return new Route(
                rs.getString("route_id"),
                rs.getString("route_short_name"),
                rs.getString("route_long_name")
        );
    }

    private <T> T executeQueryObject(String sql, Object[] p, ResultSetExtractor<T> extractor) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, p);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractor.extract(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error executing query: " + sql, e);
        }
    }

    private <T> List<T> executeQueryList(String sql, Object[] p, ResultSetExtractor<T> extractor) {
        List<T> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
             if (p != null) {
            for (int i = 0; i < p.length; i++) {
                stmt.setObject(i + 1, p[i]);
            }
        }
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            results.add(extractor.extract(rs));
        }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query: " + sql, e);
        }
        return results;
    }
    private <T> List<T> executeQueryList(String sql, ResultSetExtractor<T> extractor) {
        return executeQueryList(sql, null, extractor);
    }

    private void setParameters(PreparedStatement s, Object[] p) throws SQLException {
        if (p != null && p.length > 0) {
            for (int i = 0; i < p.length; i++) {
                s.setObject(i + 1, p[i]);
            }
        }
    }
}

@FunctionalInterface
interface ResultSetExtractor<T> {
    T extract(ResultSet rs) throws SQLException;
}
package db;

import closureAnalysis.StopFrequencyData;
import models.*;

import java.sql.*;
import java.util.*;
import java.util.List;

import static util.DebugUtil.sendError;

public class TDSImplement implements TransitDataService {
    private final DBAccess db;

    public TDSImplement() {
        this.db = DBAccessProvider.getInstance();
        if (db == null) {
            sendError("Database access instance is null.");
        }
    }

    @Override
    @SuppressWarnings("SqlResolve")
    public Stop getStop(String stopId) {
        if (db == null) {
            sendError("Database access instance is null.");
            return null;
        }
        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops WHERE stop_id = ?";
        try (
                PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, stopId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String stopName = rs.getString("stop_name");
                double stopLat = rs.getDouble("stop_lat");
                double stopLon = rs.getDouble("stop_lon");
                return new Stop(stopId, stopName, stopLat, stopLon);
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        sendError("Stop not found: " + stopId);
        return null;
    }

    @SuppressWarnings("SqlResolve")
    @Override
    public List<Stop> getAllStops() {
        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops";
        List<Stop> stops = new ArrayList<>();
        try (
                PreparedStatement ps = db.conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double stopLat = rs.getDouble("stop_lat");
                double stopLon = rs.getDouble("stop_lon");
                Stop stop = new Stop(stopId, stopName, stopLat, stopLon);
                stops.add(stop);
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return stops;
    }

    @SuppressWarnings("SqlResolve")
    public StopTime getNextStopTime(StopTime currentStopTime) {
        if (currentStopTime == null) {
            sendError("Provided StopTime is null.");
            return null;
        }
        if (db == null) {
            sendError("Database access instance is null.");
            return null;
        }
        String sql = "SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence " +
                "FROM stop_times " +
                "WHERE trip_id = ? AND stop_sequence = ?";
        try (PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, currentStopTime.trip().tripId());
            ps.setInt(2, currentStopTime.stopSequence() + 1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Trip trip = currentStopTime.trip();
                Stop stop = getStop(rs.getString("stop_id"));
                String arrivalTime = rs.getString("arrival_time");
                String departureTime = rs.getString("departure_time");
                int stopSequence = rs.getInt("stop_sequence");
                return new StopTime(stop, trip, departureTime, arrivalTime, stopSequence);
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("SqlResolve")
    public StopTime getCurrentStopTime(Trip trip, Stop stop, String departureTime) {
        if (db == null) {
            sendError("Database access instance is null.");
            return null;
        }
        String sql = """
                    SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence
                    FROM stop_times
                    WHERE trip_id = ? AND stop_id = ? AND departure_time >= ?
                    ORDER BY departure_time
                    LIMIT 1
                """;
        try (
                PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, trip.tripId());
            ps.setString(2, stop.getStopId());
            ps.setString(3, departureTime);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String arrivalTime = rs.getString("arrival_time");
                String departureTime1 = rs.getString("departure_time");
                int stopSequence = rs.getInt("stop_sequence");
                return new StopTime(stop, trip, departureTime1, arrivalTime, stopSequence);
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("SqlResolve")
    public List<Trip> getUpcomingDistinctRouteTrips(String stopId, String arrivalTime) {
        if (db == null) {
            sendError("Database access instance is null.");
            return null;
        }

        List<Trip> trips = new ArrayList<>();

        String sql = """
                    SELECT t.route_id, MIN(st.trip_id) AS trip_id  -- Select MIN(trip_id) as tie-breaker
                    FROM stop_times st
                    JOIN trips t ON st.trip_id = t.trip_id
                    WHERE st.stop_id = ?
                      AND TIME(st.arrival_time) > TIME(?)
                      AND TIME(st.arrival_time) <= ADDTIME(TIME(?), '00:15:00')
                    GROUP BY t.route_id
                """;

        try (PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, stopId);       // WHERE st.stop_id = ?
            ps.setString(2, arrivalTime);  // TIME window lower bound
            ps.setString(3, arrivalTime);  // TIME window upper bound

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                Trip trip = getTrip(tripId); // your method to fetch full Trip object
                if (trip != null) {
                    trips.add(trip);
                }
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }

        return trips;
    }

    @SuppressWarnings("SqlResolve")
    @Override
    public Trip getTrip(String tripId) {
        if (db == null) {
            sendError("Database access instance is null.");
            return null;
        }
        String sql = "SELECT trip_id, route_id, trip_headsign FROM trips WHERE trip_id = ?";
        try (
                PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, tripId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Route route = getRoute(rs.getString("route_id"));
                String headSign = rs.getString("trip_headsign");
                return new Trip(tripId, route, headSign);
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("SqlResolve")
    @Override
    public Route getRoute(String routeId) {
        if (db == null) {
            sendError("Database access instance is null.");
            return null;
        }
        String sql = "SELECT route_id, agency_id ,route_short_name, route_long_name FROM routes WHERE route_id = ?";
        try (
                PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, routeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String routeShortName = rs.getString("route_short_name");
                String routeLongName = rs.getString("route_long_name");
                String agencyId = rs.getString("agency_id");
                Agency agency = getAgency(agencyId);
                return new Route(routeId, agency, routeShortName, routeLongName);
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    @SuppressWarnings("SqlInjection")
    public ArrayList<Stop> getNearbyStops(double lat, double lon, double radiusMeters) {
        ArrayList<Stop> stopsWithinRadius = new ArrayList<>();
        DBAccess db = DBAccessProvider.getInstance();
        if (db == null) {
            sendError("Database access instance is null.");
            return stopsWithinRadius;
        }
        try (Statement stmt = db.conn.createStatement()) {
            stmt.execute("USE `" + db.dbName.replace("`", "``") + "`");
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }

        String procedureCall = "{CALL get_closest_stops(?, ?, ?)}";
        try (
                CallableStatement stmt = db.conn.prepareCall(procedureCall)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setDouble(3, radiusMeters);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String stopID = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double stopLat = rs.getDouble("stop_lat");
                double stopLon = rs.getDouble("stop_lon");
                stopsWithinRadius.add(new Stop(stopID, stopName, stopLat, stopLon));
            }
        } catch (
                SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return stopsWithinRadius;
    }

    public Agency getAgency(String agencyId) {
        if (db == null) {
            sendError("Database access instance is null.");
            return null;
        }
        String sql = "SELECT agency_id, agency_name FROM agency WHERE agency_id = ?";
        try (
                PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, agencyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Agency(agencyId, rs.getString("agency_name"));
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, StopFrequencyData> getStopFrequencyData() {
        Map<String, StopFrequencyData> dataMap = new HashMap<>();

        String sql = """
        SELECT stop_id,
               COUNT(DISTINCT t.route_id) AS route_count,
               COUNT(t.trip_id) AS trip_count
        FROM stop_times st
        JOIN trips t ON st.trip_id = t.trip_id
        GROUP BY stop_id
    """;

        try (PreparedStatement ps = db.conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                int routeCount = rs.getInt("route_count");
                int tripCount = rs.getInt("trip_count");

                dataMap.put(stopId, new StopFrequencyData(routeCount, tripCount));
            }

        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }

        return dataMap;
    }


    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT trip_id, route_id, trip_headsign FROM trips";
        try (
                PreparedStatement ps = db.conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                Route route = getRoute(rs.getString("route_id"));
                String headSign = rs.getString("trip_headsign");
                trips.add(new Trip(tripId, route, headSign));
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return trips;
    }

    public List<StopTime> getStopTimesForTrip(String tripId) {
        List<StopTime> stopTimes = new ArrayList<>();
        String sql = "SELECT stop_id, arrival_time, departure_time, stop_sequence FROM stop_times WHERE trip_id = ? ORDER BY stop_sequence";
        try (PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Stop stop = getStop(rs.getString("stop_id"));
                Trip trip = getTrip(tripId);
                String arrivalTime = rs.getString("arrival_time");
                String departureTime = rs.getString("departure_time");
                int stopSequence = rs.getInt("stop_sequence");
                stopTimes.add(new StopTime(stop, trip, departureTime, arrivalTime, stopSequence));
            }
        } catch (SQLException e) {
            sendError("SQL Error: " + e.getMessage());
        }
        return stopTimes;
    }

}

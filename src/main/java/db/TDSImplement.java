package db;

import models.*;

import java.sql.*;
import java.util.*;
import java.util.List;
public class TDSImplement implements TransitDataService
{
    private final DBaccess db;

    public TDSImplement() {
        this.db = DBaccessProvider.getInstance();
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return;
        }
        db.connect();
    }

    @Override
    public Stop getStop(String stopId) {
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return null;
        }
        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops WHERE stop_id = ?";
        try (
             PreparedStatement ps = db.conn.prepareStatement(sql)){
            ps.setString(1, stopId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Stop stop = new Stop();
                stop.stopId = rs.getString("stop_id");
                stop.stopName = rs.getString("stop_name");
                stop.stopLat = rs.getDouble("stop_lat");
                stop.stopLon = rs.getDouble("stop_lon");
                return stop;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getStop: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Stop> getAllStops() {
        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops";
        List<Stop> stops = new ArrayList<>();
        try (
             PreparedStatement ps = db.conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Stop stop = new Stop();
                stop.stopId = rs.getString("stop_id");
                stop.stopName = rs.getString("stop_name");
                stop.stopLat = rs.getDouble("stop_lat");
                stop.stopLon = rs.getDouble("stop_lon");
                stops.add(stop);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getAllStops: " + e.getMessage());
        }
        return stops;
    }

    @Override
    public List<StopTime> getStopTimesForTrip(String tripId) {
        return queryStopTimes("SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence FROM stop_times WHERE trip_id = ? ORDER BY stop_sequence", tripId);
    }

    @Override
    public List<StopTime> getStopTimesForStop(String stopId) {
        return queryStopTimes("SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence FROM stop_times WHERE stop_id = ? ORDER BY arrival_time", stopId);
    }

    public StopTime getNextStopTime(StopTime currentStopTime) {
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return null;
        }
        String sql = "SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence " +
                "FROM stop_times " +
                "WHERE trip_id = ? AND stop_sequence = ?";
        try (PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, currentStopTime.trip.tripId);
            ps.setInt(2, currentStopTime.stopSequence + 1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                StopTime next = new StopTime();
                next.trip = currentStopTime.trip;
                next.stop = getStop(rs.getString("stop_id")); // Assuming getStop is implemented
                next.arrivalTime = rs.getString("arrival_time");
                next.departureTime = rs.getString("departure_time");
                next.stopSequence = rs.getInt("stop_sequence");
                return next;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getNextStopTime: " + e.getMessage());
        }
        return null;
    }

    public StopTime getStopTime(Trip trip, Stop stop) {
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return null;
        }
        String sql = "SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence FROM stop_times WHERE trip_id = ? AND stop_id = ?";
        try (
             PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, trip.tripId);
            ps.setString(2, stop.stopId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                StopTime st = new StopTime();
                st.trip = trip;
                st.stop = stop;
                st.arrivalTime = rs.getString("arrival_time");
                st.departureTime = rs.getString("departure_time");
                st.stopSequence = rs.getInt("stop_sequence");
                return st;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getStopTime: " + e.getMessage());
        }
        return null;
    }


    public List<Trip> getUpcomingDistinctRouteTrips(String stopId, String arrivalTime) {
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return null;
        }
        List<Trip> trips = new ArrayList<>();

        String sql = """
        SELECT st.trip_id, MIN(st.arrival_time) AS earliest_time
        FROM stop_times st
        JOIN trips t ON st.trip_id = t.trip_id
        WHERE st.stop_id = ?
          AND TIME(st.arrival_time) > TIME(?)
          AND TIME(st.arrival_time) <= TIME_ADD(TIME(?), INTERVAL 30 MINUTE)
        GROUP BY t.route_id
        """;

        try (PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, stopId);
            ps.setString(2, arrivalTime);
            ps.setString(3, arrivalTime);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                Trip trip = getTrip(tripId); // Implement this to retrieve full Trip info
                trips.add(trip);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getUpcomingDistinctRouteTrips: " + e.getMessage());
        }

        return trips;
    }


    @Override
    public List<StopTime> getFutureDepartures(String stopId, Time afterTime) {
        String sql = "SELECT trip_id, stop_id, arrival_time, departure_time, stop_sequence FROM stop_times WHERE stop_id = ? AND departure_time > ? ORDER BY departure_time";
        List<StopTime> futureDepartures = new ArrayList<>();
        try (
             PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, stopId);
            ps.setTime(2,afterTime);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StopTime st = new StopTime();
                st.trip = getTrip(rs.getString("trip_id"));
                st.stop = getStop(rs.getString("stop_id"));
                st.arrivalTime = rs.getString("arrival_time");
                st.departureTime = rs.getString("departure_time");
                st.stopSequence = rs.getInt("stop_sequence");
                futureDepartures.add(st);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getFutureDepartures: " + e.getMessage());
        }
        return futureDepartures;
    }

    @Override
    public Trip getTrip(String tripId) {
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return null;
        }
        String sql = "SELECT trip_id, route_id, trip_headsign FROM trips WHERE trip_id = ?";
        try (
             PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, tripId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Trip trip = new Trip();
                trip.tripId = rs.getString("trip_id");
                trip.route = getRoute(rs.getString("route_id"));
                trip.headSign = rs.getString("trip_headsign");
                return trip;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getTrip: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Route getRoute(String routeId) {
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return null;
        }
        String sql = "SELECT route_id, route_short_name, route_long_name FROM routes WHERE route_id = ?";
        try (
             PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, routeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Route route = new Route();
                route.routeId = rs.getString("route_id");
                route.routeShortName = rs.getString("route_short_name");
                route.routeLongName = rs.getString("route_long_name");
                return route;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getRoute: " + e.getMessage());
        }
        return null;
    }

    private List<StopTime> queryStopTimes(String sql, String p) {
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return null;
        }
        List<StopTime> stopTimes = new ArrayList<>();
        try (
             PreparedStatement ps = db.conn.prepareStatement(sql)) {
            ps.setString(1, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StopTime st = new StopTime();
                st.trip = getTrip(rs.getString("trip_id"));
                st.stop = getStop(rs.getString("stop_id"));
                st.arrivalTime = rs.getString("arrival_time");
                st.departureTime = rs.getString("departure_time");
                st.stopSequence = rs.getInt("stop_sequence");
                stopTimes.add(st);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getStopTimesForStop: " + e.getMessage());
        }
        return stopTimes;
    }
}

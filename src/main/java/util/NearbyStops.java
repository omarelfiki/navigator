package util;

import db.DBaccess;
import db.DBaccessProvider;
import models.Stop;

import java.sql.*;
import java.util.ArrayList;

public class NearbyStops {
    public static ArrayList<Stop> getNearbyStops(double lat, double lon, double radiusMeters) {
        ArrayList<Stop> stopsWithinRadius = new ArrayList<>();
        DBaccess db = DBaccessProvider.getInstance();
        if (db == null) {
            System.err.println("Error: Database access instance is null.");
            return stopsWithinRadius;
        }
        String useDbQuery = "USE " + db.dbName;
        try (Statement stmt = db.conn.createStatement()) {
            stmt.execute(useDbQuery);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        String procedureCall = "{CALL get_closest_stops(?, ?, ?)}";
        try (CallableStatement stmt = db.conn.prepareCall(procedureCall)) {
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
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return stopsWithinRadius;
    }
}
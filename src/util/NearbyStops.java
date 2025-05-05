//
//import org.json.JSONObject;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;

import org.json.JSONObject;

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
        db.connect();
        String useDbQuery = "USE " + db.dbName;
        try (Statement stmt = db.conn.createStatement()) {
            stmt.execute(useDbQuery);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        String MSQLquery = "SELECT get_nearby_stops(?, ?, ?) AS nearby_stops;";
        try (PreparedStatement stmt = db.conn.prepareStatement(MSQLquery)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setDouble(3, radiusMeters);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String json = rs.getString("nearby_stops");
                JSONObject jsonObject = new JSONObject(json);

                String stopID = jsonObject.optString("stop_id", "N/A");
                String stopName = jsonObject.optString("stop_name", "N/A");
                double stopLat = jsonObject.optDouble("stop_lat", 0.0);
                double stopLon = jsonObject.optDouble("stop_lon", 0.0);

                stopsWithinRadius.add(new Stop(stopID, stopName, stopLat, stopLon));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return stopsWithinRadius;
    }
}

package db;

import models.Stop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static util.DebugUtli.getDebugMode;

public class AllStops {

    public static ArrayList<Stop> getAllStopsFromDatabase() {
        ArrayList<Stop> allStops = new ArrayList<>();
        DBaccess db = DBaccessProvider.getInstance();
        boolean isDebugMode = getDebugMode();

        if (db == null) {
            if (isDebugMode) System.err.println("Error: Database access instance is null.");
            return allStops;
        }

        String useDbQuery = "USE " + db.dbName;
        try (Statement stmt = db.conn.createStatement()) {
            stmt.execute(useDbQuery);
        } catch (SQLException e) {
            if (isDebugMode) System.err.println("SQL Error (use db): " + e.getMessage());
        }

        String query = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops";
        try (Statement stmt = db.conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String stopID = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double stopLat = rs.getDouble("stop_lat");
                double stopLon = rs.getDouble("stop_lon");

                allStops.add(new Stop(stopID, stopName, stopLat, stopLon));
            }

        } catch (SQLException e) {
            if (isDebugMode) System.err.println("SQL Error (getAllStops): " + e.getMessage());
        }

        return allStops;
    }

}

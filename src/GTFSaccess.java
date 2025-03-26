import java.sql.*;

public class GTFSaccess {
    String url;
    String user;
    String pass;
    Connection conn;

    public GTFSaccess(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public void connect() {
        try {
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connected to Roma GTFS Database");
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e);
        }
    }

    public void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("❌ Disconnected from Roma GTFS Database");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e);
        }
    }

    public void getTripData(String tripID) {
        String query = "SELECT* FROM trips WHERE trip_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tripID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String routeID = rs.getString("route_id");
                String serviceID = rs.getString("service_id");
                String shapeID = rs.getString("shape_id");
                String headsign = rs.getString("trip_headsign");
                String directionID = rs.getString("direction_id");
                String shortname = rs.getString("trip_short_name");

                System.out.println("🚍 Trip ID: " + tripID + '\n' + "    HeadSign: " + headsign + '\n'
                        + "    Route ID: " + routeID + '\n' + "    Service ID: "
                        + serviceID + '\n' + "    Direction: " + directionID + '\n' + "    Shape ID: " + shapeID + '\n' + "    Short Name: " + shortname);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e);
        }
    }

    public Stop getClosestStops(double lat, double lon) {
        String query = "SELECT * FROM get_closest_stop(?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String stopID = rs.getString("stop_id");
                if (rs.wasNull()) stopID = "N/A";
                String stopName = rs.getString("stop_name");
                if (rs.wasNull()) stopName = "N/A";
                double stopLat = rs.getDouble("stop_lat");
                if (rs.wasNull()) stopLat = 0.0;
                double stopLon = rs.getDouble("stop_lon");
                if (rs.wasNull()) stopLon = 0.0;
                return new Stop(stopID, stopName, stopLat, stopLon);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e);
        }
        return null;
    }
}
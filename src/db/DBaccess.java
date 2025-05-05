
import org.json.JSONObject;
import java.sql.*;

public class DBaccess {
    public String dbName;
    public String connectionString;
    public Connection conn;


    public DBaccess(String host, String port, String user, String password, String dbName) {
        this.dbName = dbName;
        this.connectionString = "jdbc:mysql://" + user + ":" + password + "@" + host +":" + port + "/" + dbName + "?allowLoadLocalInfile=true&useCursorFetch=true";
    }

    public DBaccess(String connectionString) {
        this.dbName = connectionString.split("/")[3];
        this.connectionString = connectionString + "?allowLoadLocalInfile=true&useCursorFetch=true";
    }

    public void connect() {
        try {
            conn = DriverManager.getConnection(connectionString);
            String infileQuery = "SET GLOBAL local_infile = 1;";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(infileQuery);
            } catch (SQLException e) {
                System.out.println("SQL Error: " + e.getMessage());
            }
            System.out.println("✅ Connected to MySQL Server");
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public void getTripData(String tripID) {
        String query = "SELECT * FROM trips WHERE trip_id = ?";
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

                System.out.println("🚍 Trip ID: " + tripID + '\n' +
                        "    HeadSign: " + headsign + '\n' +
                        "    Route ID: " + routeID + '\n' +
                        "    Service ID: " + serviceID + '\n' +
                        "    Direction: " + directionID + '\n' +
                        "    Shape ID: " + shapeID + '\n' +
                        "    Short Name: " + shortname);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public Stop getClosestStops(double lat, double lon) {
        String useDbQuery = "USE " + dbName;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(useDbQuery);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        String MSQLquery = "SELECT get_closest_stop(?, ?) AS closest_stop;";
        try (PreparedStatement stmt = conn.prepareStatement(MSQLquery)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String json = rs.getString("closest_stop");
                JSONObject jsonObject = new JSONObject(json);

                String stopID = jsonObject.optString("stop_id", "N/A");
                String stopName = jsonObject.optString("stop_name", "N/A");
                double stopLat = jsonObject.optDouble("stop_lat", 0.0);
                double stopLon = jsonObject.optDouble("stop_lon", 0.0);


                return new Stop(stopID, stopName, stopLat, stopLon);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return null;
    }
}
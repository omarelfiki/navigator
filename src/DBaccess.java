import org.json.JSONObject;

import java.sql.*;

public class DBaccess {

    //mssql server connection
    private String server;
    private String database;
    private String user;
    private String password;

    //mysql server connection
    private String host;
    private String port;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    private final int type;
    public Connection conn;

    public DBaccess(String server, String database, String user, String password) {
        this.server = server;
        this.database = database;
        this.user = user;
        this.password = password;
        this.type = 1; // 1 for Azure SQL
    }

    public DBaccess(String host, String port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.dbName = database;
        this.dbUser = user;
        this.dbPassword = password;
        this.type = 2; // 2 for MySQL
    }

    public void connect() {
        switch (type) {
            case 1:
                try {
                    String connectionUrl = "jdbc:sqlserver://" + server + ":1433;"
                            + "database=" + database + ";"
                            + "user=" + user + ";"
                            + "password=" + password + ";"
                            + "encrypt=true;"
                            + "trustServerCertificate=false;"
                            + "loginTimeout=30;";
                    conn = DriverManager.getConnection(connectionUrl);
                    System.out.println("✅ Connected to Azure SQL GTFS Database");
                } catch (SQLException e) {
                    System.out.println("SQL Error: " + e.getMessage());
                }
            case 2:
                try {
                    String connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                    conn = DriverManager.getConnection(connectionUrl, dbUser, dbPassword);
                    System.out.println("✅ Connected to MySQL GTFS Database");
                } catch (SQLException e) {
                    System.out.println("SQL Error: " + e.getMessage());
                }
        }
    }

    public void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("❌ Disconnected from Azure SQL GTFS Database");
            }
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
        switch (type) {
            case 1: String Azquery = "SELECT * FROM dbo.get_closest_stop(?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(Azquery)) {
                stmt.setDouble(1, lat);
                stmt.setDouble(2, lon);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String stopID = rs.getString("stop_id");
                    String stopName = rs.getString("stop_name");
                    double stopLat = rs.getDouble("stop_lat");
                    double stopLon = rs.getDouble("stop_lon");

                    return new Stop(
                            stopID != null ? stopID : "N/A",
                            stopName != null ? stopName : "N/A",
                            stopLat,
                            stopLon
                    );
                }
            } catch (SQLException e) {
                System.out.println("SQL Error: " + e.getMessage());
            }

            case 2: String MSQLquery = "SELECT get_closest_stop(?, ?) AS closest_stop;";
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
        }
        return null;
    }
}
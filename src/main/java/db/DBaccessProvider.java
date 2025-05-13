package db;

import java.sql.SQLException;

import static util.DebugUtli.getDebugMode;

public class DBaccessProvider {
    private static DBaccess instance;


    private DBaccessProvider() {}

    public static synchronized DBaccess getInstance() {
        boolean isDebugMode = getDebugMode();
        if (instance == null) {
            // Fetch connection details from environment variables
            String connectionString = System.getenv("ROUTING_ENGINE_MYSQL_JDBC");
            if (connectionString == null || connectionString.isEmpty()) {
                if (isDebugMode) System.err.println("Error: Environment variable ROUTING_ENGINE_MYSQL_JDBC is not set.");
                return null;
            }
            instance = new DBaccess(connectionString);
            instance.connect();
            if (instance.conn == null) {
                if (isDebugMode) System.err.println("Error: Unable to establish a connection to the database.");
                return null;
            }
        } else if (instance.conn == null) {
            // Reconnect if the connection is closed
            instance.connect();
            if (instance.conn == null) {
                if (isDebugMode) System.err.println("Error: Unable to re-establish a connection to the database.");
                return null;
            }
        } else {
            try {
                if (instance.conn.isClosed()) {
                    instance.connect();
                }
            } catch (SQLException e) {
                if (isDebugMode) System.err.println("Error: Unable to check the connection status. " + e.getMessage());
            }
        }
        return instance;
    }
}
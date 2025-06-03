package db;

import java.sql.SQLException;

import static util.DebugUtil.getDebugMode;

public class DBAccessProvider {
    private static DBAccess instance;

    private DBAccessProvider() {}

    public static synchronized DBAccess getInstance() {
        boolean isDebugMode = getDebugMode();
        if (instance == null) {
            // Fetch connection details from environment variables
            String connectionString = System.getenv("ROUTING_ENGINE_MYSQL_JDBC");
            if (connectionString == null || connectionString.isEmpty()) {
                if (isDebugMode) System.err.println("Error: Environment variable ROUTING_ENGINE_MYSQL_JDBC is not set.");
                return null;
            }
            instance = new DBAccess(connectionString);
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
package db;

import java.sql.SQLException;

import static util.DebugUtil.sendError;

public class DBAccessProvider {
    private static DBAccess instance;

    public static synchronized DBAccess getInstance() {
        if (instance == null) {
            // Fetch connection details from environment variables
            String connectionString = System.getenv("ROUTING_ENGINE_MYSQL_JDBC");
            if (connectionString == null || connectionString.isEmpty()) {
                sendError("ROUTING_ENGINE_MYSQL_JDBC environment variable not set");
                return null;
            }
            instance = new DBAccess(connectionString);
            instance.connect();
            if (checkConnection()) {return null;}

        } else if (instance.conn == null) {
            // Reconnect if the connection is closed
            instance.connect();
            if (checkConnection()) {return null;}
        } else {
            try {
                if (instance.conn.isClosed()) {
                    instance.connect();
                    if (checkConnection()) {return null;}
                }
            } catch (SQLException e) {
                sendError("Error: Unable to check the connection status", e);
            }
        }
        return instance;
    }

    private static boolean checkConnection() {
        if (instance == null || instance.conn == null) {
            sendError("Unable to establish a connection to the database.");
            return true;
        }
        return false;
    }
}
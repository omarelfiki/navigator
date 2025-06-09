package db;

import java.sql.*;

import static util.DebugUtil.*;

public class DBAccess {
    public String dbName;
    public String connectionString;
    public Connection conn;

    @SuppressWarnings("unused")
    public DBAccess(String host, String port, String user, String password, String dbName) {
        this("jdbc:mysql://" + user + ":" + password + "@" + host + ":" + port + "/" + dbName);
    }

    public DBAccess(String connectionString) {
        this.connectionString = connectionString;
    }

    public void connect() {
        try {
            // Establish the connection
            conn = DriverManager.getConnection(connectionString);
            this.dbName = conn.getCatalog();
            conn.setClientInfo("allowLoadLocalInfile", "true");
            conn.setClientInfo("useCursorFetch", "true");


            String infileQuery = "SET GLOBAL local_infile = 1;";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(infileQuery);
            } catch (SQLException e) {
                sendError("Failed to set local_infile as true in server-side", e);
            }
            sendSuccess("Connected to MySQL Server");
        } catch (SQLException e) {
            sendError("Failed to connect to MySQL Server", e);
        }
    }
}
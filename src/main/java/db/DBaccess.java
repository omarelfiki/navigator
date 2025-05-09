package db;

import java.sql.*;

public class DBaccess {
    public String dbName;
    public String connectionString;
    public Connection conn;


    public DBaccess(String host, String port, String user, String password, String dbName) {
        this("jdbc:mysql://" + user + ":" + password + "@" + host + ":" + port + "/" + dbName);
    }

    public DBaccess(String connectionString) {
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
                System.err.println("SQL Error (Server-Side Initialization): " + e.getMessage());
            }
            System.err.println("Connected to MySQL Server");
        } catch (SQLException e) {
            System.err.println("SQL Error (Connection): " + e.getMessage());
        }
    }
}
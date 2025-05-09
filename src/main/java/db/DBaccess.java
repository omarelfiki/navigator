package db;

import java.sql.*;

public class DBaccess {
    public String dbName;
    public String connectionString;
    public Connection conn;


    public DBaccess(String host, String port, String user, String password, String dbName) {
        //this.dbName = dbName;
        this("jdbc:mysql://" + user + ":" + password + "@" + host + ":" + port + "/" + dbName + "?allowLoadLocalInfile=true&useCursorFetch=true");
    }

    public DBaccess(String connectionString) {
        this.connectionString = connectionString;
        //this.dbName = connectionString.split("/")[3];
        // Ensure the connection string includes the required parameter
        //this.connectionString = connectionString + "allowLoadLocalInfile=true&useCursorFetch=true";
    }

    public void connect() {
        try {
            // Establish the connection
            conn = DriverManager.getConnection(connectionString);
            this.dbName = conn.getCatalog();
            {
                final var props = conn.getClientInfo();
                props.setProperty("allowLoadLocalInfile", "true");
                props.setProperty("useCursorFetch", "true");
                conn.setClientInfo(props);
            }

            // Enable local_infile on the server side
            String infileQuery = "SET GLOBAL local_infile = 1;";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(infileQuery);
            } catch (SQLException e) {
                System.out.println("SQL Error (Server-Side Initialization): " + e.getMessage());
            }

            System.out.println("Connected to MySQL Server");
        } catch (SQLException e) {
            System.out.println("SQL Error (Connection): " + e.getMessage());
        }
    }
}
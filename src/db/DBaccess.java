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
}
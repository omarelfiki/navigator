package db;

public class DBaccessProvider {
    private static DBaccess instance;

    private DBaccessProvider() {}

    public static synchronized DBaccess getInstance() {
        if (instance == null) {
            // Fetch connection details from environment variables
            String connectionString = System.getenv("ROUTING_ENGINE_MYSQL_JDBC");
            if (connectionString == null || connectionString.isEmpty()) {
                System.err.println("Error: Environment variable ROUTING_ENGINE_MYSQL_JDBC is not set.");
                return null;
            }
            instance = new DBaccess(connectionString);
        }
        return instance;
    }
}
public class DBaccessProvider {
    private static DBaccess instance;

    private DBaccessProvider() {}

    public static synchronized DBaccess getInstance() {
        if (instance == null) {
            // Fetch connection details from environment variables
            String connectionString = System.getenv("ROUTING_ENGINE_MYSQL_JDBC");
            if (connectionString == null || connectionString.isEmpty()) {
                throw new IllegalStateException("Connection string is not set in environment variables.");
            }
            instance = new DBaccess(connectionString);
        }
        return instance;
    }
}
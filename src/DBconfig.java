import java.io.IOException;
import java.sql.SQLException;

public class DBconfig {
    private final DBaccess access;
    private final String dbName;

    public DBconfig(DBaccess access) {
        this.access = access;
        this.dbName = "gtfsbynavigator";
    }

    public void initializeDB() {
        try {
            System.out.println("Staring database initialization...");
            if (access.conn != null && !access.conn.isClosed()) {
                String checkDbQuery = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + dbName + "'";
                var resultSet = access.conn.createStatement().executeQuery(checkDbQuery);
                if (resultSet.next()) {
                    System.out.println("GTFS database found. Replacing database...");
                    String dropDbQuery = "DROP DATABASE " + dbName;
                    access.conn.createStatement().execute(dropDbQuery);
                }

                System.out.println("Creating new GTFS database...");
                String createDbQuery = "CREATE DATABASE IF NOT EXISTS " + dbName;
                access.conn.createStatement().execute(createDbQuery);
                System.out.println("GTFS database created successfully.");
                System.setProperty("DB_NAME", dbName);
                ConfigLoader.saveConfig("config.properties");

                System.out.println("Connecting to GTFS database...");
                String useDbQuery = "USE " + dbName;
                access.conn.createStatement().execute(useDbQuery);
                System.out.println("Connected to GTFS database.");

                System.out.println("Initializing tables...");
                initializeTables();

                System.out.println("Initializing triggers...");
                initializeTriggers();

                System.out.println("Loading GTFS data...");
                GTFSImporter importer = new GTFSImporter(access, System.getProperty("GTFS_DIR"));
                importer.importGTFS();
                System.out.println("GTFS data loaded successfully.");

                System.out.println("Database initialization completed.");
            } else {
                System.err.println("Stopping database initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeTriggers() {
        try {
            if (access.conn != null && !access.conn.isClosed()) {
                System.out.println("Accessing GTFS trigger SQL file");
                try {
                    String sqlFilePath = "src/resources/gtfs_triggers.sql";
                    String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFilePath)));
                    try (java.sql.Statement statement = access.conn.createStatement()) {
                        statement.execute(sql.trim());
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Error reading SQL file: " + e.getMessage());
                }
                System.out.println("GTFS data model trigger created successfully.");
            } else {
                System.err.println("Stopping trigger initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public void initializeTables() {
        try {
            if (access.conn != null && !access.conn.isClosed()) {
                System.out.println("Accessing GTFS schema SQL file");
                try {
                    String sqlFilePath = "src/resources/newschema.sql";
                    String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFilePath)));
                    try (java.sql.Statement statement = access.conn.createStatement()) {
                        for (String stmt : sql.split(";")) {
                            if (!stmt.trim().isEmpty()) {
                                statement.execute(stmt.trim());
                            }
                        }
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Error reading SQL file: " + e.getMessage());
                }
                System.out.println("GTFS data model table created successfully.");
            } else {
                System.err.println("Stopping table initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        if (!ConfigLoader.checkIfConfigExists("config.properties")) {
            ConfigLoader.createConfig("config.properties");
        } else {
            System.out.println("Config file found");
            ConfigLoader.loadConfig("config.properties");
        }
        DBaccess access = new DBaccess(System.getProperty("DB_HOST"), System.getProperty("DB_PORT"), System.getProperty("DB_USER"), System.getProperty("DB_PASSWORD"));
        access.connect();
        DBconfig config = new DBconfig(access);
        config.initializeDB();
    }
}

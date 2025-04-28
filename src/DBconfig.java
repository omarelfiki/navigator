import java.io.IOException;
import java.sql.SQLException;

public class DBconfig {
    private final DBaccess access;

    public DBconfig(DBaccess access) {
        this.access = access;
    }

    public void initializeDB() {
        try {
            System.out.println("Staring database initialization...");
            access.connect();
            if (access.conn != null && !access.conn.isClosed()) {

                System.out.println("Insuring clear database...");
                access.conn.createStatement().execute("DROP DATABASE IF EXISTS " + access.dbName);
                access.conn.createStatement().execute("CREATE DATABASE " + access.dbName);
                access.conn.createStatement().execute("USE " + access.dbName);

                System.out.println("Initializing tables...");
                initializeTables();

                System.out.println("Initializing triggers...");
                initializeTriggers();

                System.out.println("Loading GTFS data...");
                GTFSImporter importer = new GTFSImporter(access);
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
        DBaccess access = DBaccessProvider.getInstance();
        DBconfig config = new DBconfig(access);
        config.initializeDB();
    }
}

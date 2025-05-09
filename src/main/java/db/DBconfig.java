package db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class DBconfig {
    private final DBaccess access;

    private final String GTFS_FILE_PATH;

    public DBconfig(DBaccess access) {
        this.access = access;
        this.GTFS_FILE_PATH = System.getenv("GTFS_DIR");
    }

    public DBconfig(String filePath) {
        this.access = DBaccessProvider.getInstance();
        GTFS_FILE_PATH = filePath;
    }

    public void initializeDB() {
        access.connect();
        try {
            System.out.println("Starting database initialization...");
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
                GTFSImporter importer = new GTFSImporter(GTFS_FILE_PATH);
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
                   String sqlFilePath = Objects.requireNonNull(getClass().getClassLoader().getResource("gtfs_triggers.sql")).getPath();
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
                    String sqlFilePath = Objects.requireNonNull(getClass().getClassLoader().getResource("newschema.sql")).getPath();
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
}

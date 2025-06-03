package db;

import util.ZipExtractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.net.URISyntaxException;
import static util.DebugUtil.getDebugMode;


public class DBConfig {
    private final DBAccess access;

    private final String GTFS_PATH;

    private final boolean isDebugMode;

    public DBConfig(String filePath) {
        this.access = DBAccessProvider.getInstance();
        GTFS_PATH = filePath;
        isDebugMode = getDebugMode();
    }

    @SuppressWarnings("SqlInjection")
    public void initializeDB() {
        try {
            if (isDebugMode) System.err.println("Starting database initialization...");
            if (access.conn != null && !access.conn.isClosed()) {
                if (isDebugMode) System.err.println("Insuring clear database...");
                access.conn.createStatement().execute("DROP DATABASE IF EXISTS " + access.dbName);
                access.conn.createStatement().execute("CREATE DATABASE " + access.dbName);
                access.conn.createStatement().execute("USE " + access.dbName);

                if (isDebugMode) System.err.println("Initializing tables...");
                initializeTables();

                if (isDebugMode) System.err.println("Initializing triggers...");
                initializeTriggers();

                if (isDebugMode) System.err.println("Loading GTFS data from zip file: " + GTFS_PATH);
                String tempDir = System.getenv("ROUTING_ENGINE_STORAGE_DIRECTORY");
                if (tempDir == null || tempDir.isEmpty()) {
                    tempDir = System.getProperty("java.io.tmpdir");
                }
                ZipExtractor.extractZipToDirectory(GTFS_PATH, tempDir);
                GTFSImporter importer = new GTFSImporter(tempDir);
                importer.importGTFS();
                if (isDebugMode) System.err.println("GTFS data loaded successfully.");

                if (isDebugMode) System.err.println("Database initialization completed.");
            } else {
                if (isDebugMode) System.err.println("Stopping database initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Initialization Error: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeTriggers() {
        try {
            if (access.conn != null && !access.conn.isClosed()) {
                if (isDebugMode) System.err.println("Accessing GTFS trigger SQL file");
                try {
                    String sqlFilePath = java.nio.file.Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("gtfs_triggers.sql")).toURI()).toString();
                    String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFilePath)));

                    try (java.sql.Statement statement = access.conn.createStatement()) {
                        statement.execute(sql.trim());
                    }
                } catch (java.io.IOException e) {
                    System.err.println("Error reading SQL file: " + e.getMessage());
                }
                catch (URISyntaxException e) {
                    System.err.println("Error reading SQL path file: " + e.getMessage());
                }
                if (isDebugMode) System.err.println("GTFS data model trigger created successfully.");
            } else {
                if (isDebugMode) System.err.println("Stopping trigger initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Trigger Error: " + e.getMessage());
        }
    }

    public void initializeTables() {
        try {
            if (access.conn != null && !access.conn.isClosed()) {
                if (isDebugMode) System.err.println("Accessing GTFS schema SQL file");
                try {
                    String sqlFilePath = java.nio.file.Paths.get(
                            Objects.requireNonNull(getClass().getClassLoader().getResource("new_schema.sql")).toURI()
                    ).toString();
                    String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFilePath)));
                    try (java.sql.Statement statement = access.conn.createStatement()) {
                        for (String stmt : sql.split(";")) {
                            if (!stmt.trim().isEmpty()) {
                                statement.execute(stmt.trim());
                            }
                        }
                    }
                } catch (java.io.IOException e) {
                    System.err.println("Error reading SQL file: " + e.getMessage());
                } catch (URISyntaxException e) {
                    System.err.println("Error reading SQL pathfile: " + e.getMessage());
                }
                if (isDebugMode) System.err.println("GTFS data model table created successfully.");
            } else {
                if (isDebugMode) System.err.println("Stopping table initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Table Init Error: " + e.getMessage());
        }
    }
}

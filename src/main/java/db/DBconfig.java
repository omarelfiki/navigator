package db;

import util.ZipExtractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class DBconfig {
    private final DBaccess access;

    private final String GTFS_PATH;

    private final int filetype; // 0 for dir, 1 for zip

    private final boolean isDebugMode;

    public DBconfig(DBaccess access) {
        this.access = access;
        this.GTFS_PATH = System.getenv("GTFS_DIR");
        filetype = 0;
        isDebugMode = true;
    }

    public DBconfig(String filePath) {
        this.access = DBaccessProvider.getInstance();
        GTFS_PATH = filePath;
        filetype = 1;
        isDebugMode = false;
    }

    public void initializeDB() {
        access.connect();
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

                if (isDebugMode) System.err.println("Loading GTFS data...");
                switch (filetype) {
                    case 0 -> {
                        if (isDebugMode) System.err.println("Loading GTFS data from directory: " + GTFS_PATH);
                        GTFSImporter importer = new GTFSImporter(GTFS_PATH, isDebugMode);
                        importer.importGTFS();
                    }
                    case 1 -> {
                        if (isDebugMode) System.err.println("Loading GTFS data from zip file: " + GTFS_PATH);
                        String tempDir = System.getenv("ROUTING_ENGINE_STORAGE_DIRECTORY");
                        ZipExtractor.extractZipToDirectory(GTFS_PATH, tempDir);
                        GTFSImporter importer = new GTFSImporter(tempDir, isDebugMode);
                        importer.importGTFS();
                    }
                }
                if (isDebugMode) System.err.println("GTFS data loaded successfully.");

                if (isDebugMode) System.err.println("Database initialization completed.");
            } else {
                if (isDebugMode) System.err.println("Stopping database initialization: connection to the database is not established.");
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
                if (isDebugMode) System.err.println("Accessing GTFS trigger SQL file");
                try {
                   String sqlFilePath = Objects.requireNonNull(getClass().getClassLoader().getResource("gtfs_triggers.sql")).getPath();
                    String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFilePath)));
                    try (java.sql.Statement statement = access.conn.createStatement()) {
                        statement.execute(sql.trim());
                    }
                } catch (java.io.IOException e) {
                    System.err.println("Error reading SQL file: " + e.getMessage());
                }
                if (isDebugMode) System.err.println("GTFS data model trigger created successfully.");
            } else {
                if (isDebugMode) System.err.println("Stopping trigger initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public void initializeTables() {
        try {
            if (access.conn != null && !access.conn.isClosed()) {
                if (isDebugMode) System.err.println("Accessing GTFS schema SQL file");
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
                    System.err.println("Error reading SQL file: " + e.getMessage());
                }
                if (isDebugMode) System.err.println("GTFS data model table created successfully.");
            } else {
                if (isDebugMode) System.err.println("Stopping table initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }
}

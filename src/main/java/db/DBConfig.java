package db;

import util.ZipExtractor;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.net.URISyntaxException;

import static util.DebugUtil.*;


public class DBConfig {
    private final DBAccess access;

    private final String GTFS_PATH;

    public DBConfig(String filePath) {
        this.access = DBAccessProvider.getInstance();
        GTFS_PATH = filePath;
    }

    @SuppressWarnings("SqlInjection")
    public void initializeDB() {
        try {
            sendInfo("Starting database initialization...");
            if (access.conn != null && !access.conn.isClosed()) {
                sendInfo("Insuring clear database...");
                try (Statement stmt = access.conn.createStatement()) {
                    stmt.execute("DROP DATABASE IF EXISTS `" + access.dbName.replace("`", "``") + "`");
                    stmt.execute("CREATE DATABASE `" + access.dbName.replace("`", "``") + "`");
                    stmt.execute("USE `" + access.dbName.replace("`", "``") + "`");
                } catch (SQLException e) {
                    sendError("SQL Error (Disabling Foreign Key Checks): " , e);
                }
                sendInfo("Initializing tables...");
                initializeTables();

                sendInfo("Initializing triggers...");
                initializeTriggers();

                sendInfo("Loading GTFS data from zip file: " + GTFS_PATH);
                String tempDir = System.getenv("ROUTING_ENGINE_STORAGE_DIRECTORY");
                if (tempDir == null || tempDir.isEmpty()) {
                    tempDir = System.getProperty("java.io.tmpdir");
                }
                ZipExtractor.extractZipToDirectory(GTFS_PATH, tempDir);
                GTFSImporter importer = new GTFSImporter(tempDir);
                importer.importGTFS();
                sendSuccess("GTFS data loaded successfully.");

                sendSuccess("Database initialization completed successfully.");
            } else {
                sendError("Database connection is not established. Please check your connection settings.");
            }
        } catch (SQLException e) {
            sendError("FATAL SQL Initialization Error: ", e);
        } catch (IOException e) {
            sendError("FATAL IO Error during GTFS import: ", e);
            throw new RuntimeException();
        }
    }

    public void initializeTriggers() {
        try {
            if (access.conn != null && !access.conn.isClosed()) {
                sendInfo("Accessing GTFS trigger SQL file");
                try {
                    String sqlFilePath = java.nio.file.Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("gtfs_triggers.sql")).toURI()).toString();
                    String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFilePath)));

                    try (java.sql.Statement statement = access.conn.createStatement()) {
                        statement.execute(sql.trim());
                    }
                } catch (java.io.IOException e) {
                    sendError("Error reading SQL file", e);
                } catch (URISyntaxException e) {
                    sendError("Error reading SQL path", e);
                }
                sendSuccess("GTFS data model trigger created successfully.");
            } else {
                sendError("Stopping trigger initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            sendError("FATAL SQL Initialization Error: ", e);
        }
    }

    public void initializeTables() {
        try {
            if (access.conn != null && !access.conn.isClosed()) {
                sendInfo("Accessing GTFS schema SQL file");
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
                    sendError("Error reading SQL file", e);
                } catch (URISyntaxException e) {
                    sendError("Error reading SQL path", e);
                }
                sendSuccess("GTFS data model tables created successfully.");
            } else {
                sendError("Stopping table initialization: connection to the database is not established.");
            }
        } catch (SQLException e) {
            sendError("FATAL SQL Initialization Error: ", e);
        }
    }
}

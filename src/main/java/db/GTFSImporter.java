package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static util.DebugUtil.getDebugMode;

public class GTFSImporter {
    private final String GTFS_DIR;
    private final DBAccess access;
    private final boolean isDebugMode;

    public GTFSImporter(String GTFS_DIR) {
        this.access = DBAccessProvider.getInstance();
        this.GTFS_DIR = GTFS_DIR;
        this.isDebugMode = getDebugMode();

    }

    public void importGTFS() throws IOException, SQLException {
        try (Connection conn = access.conn) {

            List<String[]> gtfsFiles = List.of(
                    new String[]{"agency.txt", "agency"},
                    new String[]{"routes.txt", "routes"},
                    new String[]{"stops.txt", "stops"},
                    new String[]{"trips.txt", "trips"},
                    new String[]{"stop_times.txt", "stop_times"}
            );

            for (String[] file : gtfsFiles) {
                String filename = file[0];
                String tableName = file[1];
                Path filePath = Paths.get(GTFS_DIR, filename);

                if (Files.exists(filePath)) {
                    if (isDebugMode) System.err.println("Importing " + filename + " into " + tableName + "...");

                    switch (tableName) {
                        case "agency" -> importAgencyWithLoadData(filePath, conn);
                        case "routes" -> importRouteWithLoadData(filePath, conn);
                        case "stops" -> importStopsWithLoadData(filePath, conn);
                        case "trips" -> importTripsWithLoadData(filePath, conn);
                        case "stop_times" -> importStopTimesWithLoadData(filePath, conn);
                    }
                } else {
                    if (isDebugMode) System.err.println("File not found: " + filePath);
                }
            }
        }
        if (isDebugMode) System.err.println("GTFS import complete.");
    }

    @SuppressWarnings("SqlResolve")
    private void importAgencyWithLoadData(Path filePath, Connection conn) throws SQLException, IOException {
        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");
        String[] importantFields = {"agency_id", "agency_name"};

        Map<String, String> loadDataParts = prepareLoadDataParts(filePath, importantFields);
        String tempVariables = loadDataParts.get("tempVariables");
        String setBuilder = loadDataParts.get("setBuilder");

        try (Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS=0");

            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE agency " +
                    "FIELDS TERMINATED BY ',' " +
                    "OPTIONALLY ENCLOSED BY '\"' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 LINES " +
                    "(" + tempVariables + ") " +
                    setBuilder;

            int rows = stmt.executeUpdate(sql);
            if (isDebugMode) System.err.println("Inserted " + rows + " agencies from: " + filePath.getFileName());

            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    @SuppressWarnings("SqlResolve")
    private void importRouteWithLoadData(Path filePath, Connection conn) throws IOException, SQLException {

        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");

        String[] importantFields = {"route_id", "route_short_name", "route_long_name", "agency_id"};

        Map<String, String> loadDataParts = prepareLoadDataParts(filePath, importantFields);
        String tempVariables = loadDataParts.get("tempVariables");
        String setBuilder = loadDataParts.get("setBuilder");

        try (Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS=0");

            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE routes " +
                    "FIELDS TERMINATED BY ',' " +
                    "OPTIONALLY ENCLOSED BY '\"' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 LINES " +

                    "(" + tempVariables + ") " +

                    setBuilder;

            int rows = stmt.executeUpdate(sql);
            if (isDebugMode) System.err.println("Inserted " + rows + " routes from: " + filePath.getFileName());

            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    @SuppressWarnings("SqlResolve")
    private void importStopsWithLoadData(Path filePath, Connection conn) throws SQLException, IOException {
        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");
        String[] importantFields = {"stop_id", "stop_name", "stop_lat", "stop_lon", "stop_code"};

        Map<String, String> loadDataParts = prepareLoadDataParts(filePath, importantFields);
        String tempVariables = loadDataParts.get("tempVariables");
        String setBuilder = loadDataParts.get("setBuilder");

        try (Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS=0");

            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE stops " +
                    "FIELDS TERMINATED BY ',' " +
                    "OPTIONALLY ENCLOSED BY '\"' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 LINES " +
                    "(" + tempVariables + ") " +
                    setBuilder;

            int rows = stmt.executeUpdate(sql);
            if (isDebugMode) System.err.println("Inserted " + rows + " stops from: " + filePath.getFileName());

            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    @SuppressWarnings("SqlResolve")
    private void importTripsWithLoadData(Path filePath, Connection conn) throws SQLException, IOException {
        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");
        String[] importantFields = {"trip_id", "route_id", "service_id", "trip_short_name", "trip_headsign"};

        Map<String, String> loadDataParts = prepareLoadDataParts(filePath, importantFields);
        String tempVariables = loadDataParts.get("tempVariables");
        String setBuilder = loadDataParts.get("setBuilder");


        try (Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS=0");

            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE trips " +
                    "FIELDS TERMINATED BY ',' " +
                    "OPTIONALLY ENCLOSED BY '\"' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 LINES " +
                    "(" + tempVariables + ") " +
                    setBuilder;

            int rows = stmt.executeUpdate(sql);
            if (isDebugMode) System.err.println("Inserted " + rows + " trips from: " + filePath.getFileName());

            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    @SuppressWarnings("SqlResolve")
    private void importStopTimesWithLoadData(Path filePath, Connection conn) throws SQLException, IOException {
        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");

        String[] importantFields = {"trip_id", "arrival_time", "departure_time", "stop_id", "stop_sequence"};

        Map<String, String> loadDataPart = prepareLoadDataParts(filePath, importantFields);

        String tempVariables = loadDataPart.get("tempVariables");
        String setBuilder = loadDataPart.get("setBuilder");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");

            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE stop_times " +
                    "FIELDS TERMINATED BY ',' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 LINES " +
                    "(" + tempVariables + ") " +
                    setBuilder;

            int rows = stmt.executeUpdate(sql);
            if (isDebugMode) System.err.println("Inserted " + rows + " stop_times from: " + filePath.getFileName());

            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    // helper method to get build the tempVariables and Set statement for the loadData Methods
    private Map<String, String> prepareLoadDataParts(Path filePath, String[] importantFields) throws IOException {

        List<String> headers = readCsvHeaders(filePath);
        Map<String, String> loadDataParts = new HashMap<>();

        // building the tempVariables part
        StringBuilder tempVariables = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            tempVariables.append("@").append(headers.get(i));
            if (i < headers.size() - 1) {
                tempVariables.append(", ");
            }
        }
        // building the setBuilder part
        StringBuilder setBuilder = new StringBuilder("SET ");
        boolean putComma = true;
        for (String field : importantFields) {
            if (!putComma) {
                setBuilder.append(", ");
            }
            setBuilder.append(field).append(" = @").append(field);
            putComma = false;
        }
        loadDataParts.put("tempVariables", tempVariables.toString());
        loadDataParts.put("setBuilder", setBuilder.toString());
        return loadDataParts;
    }

    //Helper method to read the CSV headers
    private List<String> readCsvHeaders(Path filePath) throws IOException {
        List<String> headers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line = reader.readLine();
            if (line != null) {
                String[] headerArray = line.split(",");
                for (String header : headerArray) {
                    headers.add(header.trim().replace("\"", ""));
                }
            }
        }
        return headers;
    }
}

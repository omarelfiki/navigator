import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class GTFSImporter {
    private static String GTFS_DIR;

    private final DBaccess access;

    public GTFSImporter(DBaccess access) {
        this.access = access;
        GTFS_DIR = System.getenv("GTFS_DIR");
    }

    public void importGTFS() throws IOException, SQLException {
        try (Connection conn = access.conn) {
            insertUniqueServices(conn);

            List<String[]> gtfsFiles = List.of(
                    new String[]{"agency.txt", "agency"},
                    new String[]{"routes.txt", "routes"},
                    new String[]{"stops.txt", "stops"},
                    new String[]{"calendar.txt", "calendar"},
                    new String[]{"calendar_dates.txt", "calendar_dates"},
                    new String[]{"shapes.txt", "shapes"},
                    new String[]{"trips.txt", "trips"},
                    new String[]{"stop_times.txt", "stop_times"}
            );

            for (String[] file : gtfsFiles) {
                String filename = file[0];
                String tableName = file[1];
                Path filePath = Paths.get(GTFS_DIR, filename);

                if (Files.exists(filePath)) {
                    System.out.println("Importing " + filename + " into " + tableName + "...");

                    List<CSVRecord> records = readCSV(filePath);

                    if ("calendar".equals(tableName)) {
                        records.forEach(record -> {
                            record.toMap().put("start_date", formatDate(record.get("start_date")));
                            record.toMap().put("end_date", formatDate(record.get("end_date")));
                        });

                    }
                    if ("calendar_dates".equals(tableName)) {
                        records.forEach(record -> record.toMap().put("date", formatDate(record.get("date"))));
                    }
                    if ("stop_times".equals(tableName)) {
                        importStopTimesWithLoadData(filePath, conn);
                        continue;
                    }
                    if ("shapes".equals(tableName)) {
                        insertShapeIndex(records, conn);
                        importShapesWithLoadData(filePath, conn);
                        continue;
                    }
                    if ("trips".equals(tableName)) {
                        importTripsWithLoadData(filePath, conn);
                        continue;
                    }
                    uploadToTable(records, tableName, conn);
                } else {
                    System.out.println("⚠️ File not found: " + filePath);
                }
            }
        }
        System.out.println("✅ GTFS import complete.");
    }

    private static void insertUniqueServices(Connection conn) throws IOException, SQLException {

        Set<String> serviceIds = new HashSet<>();

        Path calendarPath = Paths.get(GTFS_DIR, "calendar.txt");
        if (Files.exists(calendarPath)) {
            readCSV(calendarPath).forEach(record -> serviceIds.add(record.get("service_id")));
        }

        Path calendarDatesPath = Paths.get(GTFS_DIR, "calendar_dates.txt");
        if (Files.exists(calendarDatesPath)) {
            readCSV(calendarDatesPath).forEach(record -> serviceIds.add(record.get("service_id")));
        }

        int insertedCount = 0;
        String sql = "INSERT IGNORE INTO service (service_id) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String serviceId : serviceIds) {
                stmt.setString(1, serviceId);
                stmt.executeUpdate();
                insertedCount++;
            }
        }
        System.out.println("✅ Inserted " + insertedCount + " unique service_id(s) into service table.");
    }


    // This method imports trip records from a txt file into the 'trips' table using LOAD DATA LOCAL INFILE.
    //It handles column mismatches, empty integer values, and temporarily disables foreign key checks
    //to avoid constraint violations during bulk insert.
    private static void importTripsWithLoadData(Path filePath, Connection conn) throws SQLException {
        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");

        try (Statement stmt = conn.createStatement()) {
            // disable the foreign key constrains
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE trips " +
                    "FIELDS TERMINATED BY ',' " +
                    "LINES TERMINATED BY '\\n' " +
                    // ignore the first row
                    "IGNORE 1 LINES " +
                    // instead of inserting directly to the tables, first capture them in temporary variables, because some fields are empty
                    "(@route_id, @service_id, @trip_id, @trip_headsign, @trip_short_name, @direction_id, @block_id, @shape_id, @wheelchair_accessible, @exceptional) " +
                    // now inserting them into the real table
                    "SET " +
                    "trip_id = @trip_id, " +
                    "route_id = @route_id, " +
                    "service_id = @service_id, " +
                    "shape_id = @shape_id, " +
                    "trip_short_name = @trip_short_name, " +
                    "trip_headsign = @trip_headsign, " +
                    // special cases for empty field, if the fild is a number insert it, else insert as NULL
                    "direction_id = IF(@direction_id REGEXP '^[0-9]+$', @direction_id, NULL), " +
                    // special cases for empty field, if the fild is a number insert it, else insert as NULL
                    "block_id = IF(@block_id REGEXP '^[0-9]+$', @direction_id, NULL), " +
                    // special cases for empty field, if the fild is a number insert it, else insert as NULL
                    "wheelchair_accessible = IF(@wheelchair_accessible REGEXP '^[0-9]+$', @wheelchair_accessible, NULL), " +
                    // special cases for empty field, if the fild is a number insert it, else insert as NULL
                    "exceptional = IF(@exceptional REGEXP '^[0-9]+$', @exceptional, NULL)";

            int rows = stmt.executeUpdate(sql);
            System.out.println("✅ Inserted " + rows + " trips from: " + filePath.getFileName());
            // finally enable the foreign key, to insure the relation integrity
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    //  this method works same as importTripsWithLoadData
    private static void importStopTimesWithLoadData(Path filePath, Connection conn) throws SQLException {
        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");

            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE stop_times " +
                    "FIELDS TERMINATED BY ',' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 LINES " +
                    "(@trip_id, @arrival_time, @departure_time, @stop_id, @stop_sequence, @stop_headsign, @pickup_type, @drop_off_type, @shape_dist_traveled, @timepoint) " +
                    "SET " +
                    "trip_id = @trip_id, " +
                    "arrival_time = @arrival_time, " +
                    "departure_time = @departure_time, " +
                    "stop_id = @stop_id, " +
                    "stop_sequence = IF(@stop_sequence REGEXP '^[0-9]+$', @stop_sequence, NULL), " +
                    "stop_headsign = NULLIF(@stop_headsign, ''), " +
                    "pickup_type = IF(@pickup_type REGEXP '^[0-9]+$', @pickup_type, NULL), " +
                    "drop_off_type = IF(@drop_off_type REGEXP '^[0-9]+$', @drop_off_type, NULL), " +
                    "shape_dist_traveled = IF(@shape_dist_traveled REGEXP '^[0-9]+(\\.[0-9]+)?$', @shape_dist_traveled, NULL), " +
                    "timepoint = IF(@timepoint REGEXP '^[0-9]+$', @timepoint, NULL)";

            int rows = stmt.executeUpdate(sql);
            System.out.println("✅ Inserted " + rows + " stop_times from: " + filePath.getFileName());

            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    private static void insertShapeIndex(List<CSVRecord> shapeRecords, Connection conn) throws SQLException {
        Set<String> shapeIds = shapeRecords.stream()
                .map(record -> record.get("shape_id"))
                .collect(Collectors.toSet());

        int insertedCount = 0;
        String sql = "INSERT IGNORE INTO shape_index (shape_id) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String shapeId : shapeIds) {
                stmt.setString(1, shapeId);
                stmt.executeUpdate();
                insertedCount++;
            }
        }
        System.out.println("✅ Inserted " + insertedCount + " unique shape_id(s) into shape_index table.");
    }
    // sane as importTimesWithLoadData
    private static void importShapesWithLoadData(Path filePath, Connection conn) throws SQLException {
        String absolutePath = filePath.toAbsolutePath().toString().replace("\\", "/");
        try(Statement stmt = conn.createStatement()) {
            /*stmt.execute("SET FOREIGN_KEY_CHECKS=0");*/
            String sql = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                    "INTO TABLE shapes " +
                    "FIELDS TERMINATED BY ',' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 LINES " +
                    "(@shape_id, @shape_pt_lat, @shape_pt_lon, @shape_pt_sequence, @shape_dist_traveled) " +
                    "SET " +
                    "shape_id = @shape_id, " +
                    "shape_pt_lat = IF(@shape_pt_lat REGEXP '^-?[0-9]+(\\.[0-9]+)?$', @shape_pt_lat, NULL), " +
                    "shape_pt_lon = IF(@shape_pt_lon REGEXP '^-?[0-9]+(\\.[0-9]+)?$', @shape_pt_lon, NULL), " +
                    "shape_pt_sequence = IF(@shape_pt_sequence REGEXP '^[0-9]+$', @shape_pt_sequence, NULL), " +
                    "shape_dist_traveled = IF(@shape_dist_traveled REGEXP '^-?[0-9]+(\\.[0-9]+)?$', @shape_dist_traveled, NULL)";


            int rows = stmt.executeUpdate(sql);
            System.out.println("✅ Inserted " + rows + " shapes from: " + filePath.getFileName());

            /*stmt.execute("SET FOREIGN_KEY_CHECKS=1");*/

        }
    }

//    private static void importStopTimesWithChunking(Path filePath, Connection conn) throws IOException, SQLException {
//        try (Reader reader = Files.newBufferedReader(filePath);
//             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
//
//            String sql = "INSERT INTO stop_times (trip_id, stop_id, stop_sequence) VALUES (?, ?, ?)";
//            List<CSVRecord> batch = new ArrayList<>();
//            int batchSize = 1000;
//
//            for (CSVRecord record : parser) {
//                batch.add(record);
//
//                if (batch.size() == batchSize) {
//                    insertBatch(batch, sql, conn);
//                    batch.clear();
//                }
//            }
//
//            if (!batch.isEmpty()) {
//                insertBatch(batch, sql, conn);
//            }
//        }
//        System.out.println("✅ Imported stop_times with chunking.");
//    }
//
//    // method to insert trips with chunking using the uinvocty parser,
//    private static void importTripsWithChunking(Path filePath, Connection conn) throws IOException, SQLException {
//        CsvParserSettings settings = new CsvParserSettings();
//
//        String sql = String.format("INSERT INTO trips (route_id,trip_id,Service_id,shape_id) VALUES (?,?,?,?)");
//        settings.setHeaderExtractionEnabled(true);
//        settings.setIgnoreLeadingWhitespaces(true);
//        settings.setIgnoreTrailingWhitespaces(true);
//        settings.setSkipEmptyLines(true);
//
//        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
//            CsvParser parser = new CsvParser(settings);
//            parser.beginParsing(reader);
//
//            String[] headers = parser.getContext().headers();
//            final int batchSize = 10000;
//            List<Map<String, String>> batch = new ArrayList<>();
//            String[] row;
//
//            while ((row = parser.parseNext()) != null) {
//                Map<String, String> rowMap = new HashMap<>();
//                for (int i = 0; i < headers.length && i < row.length; i++) {
//                    rowMap.put(headers[i], row[i]);
//                }
//                batch.add(rowMap);
//                if (batch.size() >= batchSize) {
//                    insertTripsBatch(batch, sql, conn);
//                    batch.clear();
//                }
//            }
//
//            if (!batch.isEmpty()) {
//                insertTripsBatch(batch, sql, conn);
//            }
//
//            parser.stopParsing();
//            System.out.println("✅ Imported trips with chunking.");
//        }
//    }

    // method for batching, will be used for trips
    private static void insertTripsBatch(List<Map<String, String>> batch, String sql, Connection conn) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map<String, String> row : batch) {
                stmt.setString(1, row.get("route_id"));
                stmt.setString(2, row.get("trip_id"));
                stmt.setString(3, row.get("service_id"));
                stmt.setString(4, row.get("shape_id"));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void insertBatch(List<CSVRecord> batch, String sql, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CSVRecord record : batch) {
                stmt.setString(1, record.get("trip_id"));
                stmt.setString(2, record.get("stop_id"));

                String stopSequence = record.get("stop_sequence");
                if (stopSequence == null || stopSequence.isEmpty()) {
                    stmt.setNull(3, Types.INTEGER); // Set NULL for missing stop_sequence
                } else {
                    stmt.setInt(3, Integer.parseInt(stopSequence));
                }

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void uploadToTable(List<CSVRecord> records, String tableName, Connection conn) throws SQLException {
        if (records.isEmpty()) return;

        String columns = String.join(", ", records.getFirst().toMap().keySet());
        String placeholders = records.getFirst().toMap().keySet().stream()
                .map(_ -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int insertedCount = 0;
            for (CSVRecord record : records) {
                int index = 1;
                for (Map.Entry<String, String> entry : record.toMap().entrySet()) {
                    String columnName = entry.getKey();
                    String value = entry.getValue();

                    // Handle empty strings for integer fields
                    if (value.isEmpty() && isIntegerColumn(columnName, tableName)) {
                        stmt.setObject(index++, null);
                    } else {
                        stmt.setString(index++, value);
                    }
                    insertedCount++;// Set NULL for empty strings
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
            System.out.println("✅ Inserted " + insertedCount + " rows into " + tableName + " table.");
        }
    }

    // Helper method to check if a column is an integer column
    private static boolean isIntegerColumn(String columnName, String tableName) {
        // Define integer columns for each table
        Map<String, Set<String>> integerColumns = Map.of(
                "stops", Set.of("wheelchair_boarding", "location_type"),
                "trips", Set.of("route_id", "service_id", "wheelchair_accessible", "exceptional"),
                "shapes", Set.of("shape_dist_traveled"),
                "stop_times", Set.of("stop_sequence")
                // Add other tables and their integer columns here as needed
        );

        return integerColumns.getOrDefault(tableName, Collections.emptySet()).contains(columnName);
    }

    private static List<CSVRecord> readCSV(Path filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath.toFile())) {
            return CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader).getRecords();
        }
    }

    private static String formatDate(String date) {
        return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
    }
}
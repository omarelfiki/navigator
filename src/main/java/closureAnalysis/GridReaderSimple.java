package closureAnalysis;

import db.TDSImplement;
import models.Stop;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GridReaderSimple {
    private static final Path CSV = Path.of("src/main/resources/GrigliaPop2021_Ind_ITA_CSV.txt");

    private static final double R    = 6_371_007.181;
    private static final double LAT0 = Math.toRadians(52);
    private static final double LON0 = Math.toRadians(10);
    private static final double FE   = 4_321_000;
    private static final double FN   = 3_210_000;
    private static final Pattern ID_PATTERN = Pattern.compile(".*N(\\d{4,7})E(\\d{4,7})");
    private static final double ROME_NORTH = 41.998617;
    private static final double ROME_SOUTH = 41.784336;
    private static final double ROME_WEST  = 12.350826;
    private static final double ROME_EAST  = 12.643411;

    //txt -> Tile
    public static List<Tile> read() throws IOException {
        List<Tile> tiles = new ArrayList<>(350_000);
        try (BufferedReader br = Files.newBufferedReader(CSV, StandardCharsets.UTF_8)) {
            String header = br.readLine();

            if (header == null)
                throw new IOException("Empty file: " + CSV);

            if (!header.isEmpty() && header.charAt(0) == '\uFEFF')
                header = header.substring(1);

            final String delimiter = header.indexOf(';') >= 0 ? ";" : ",";

            String[] h = header.split(delimiter, -1);

            int idIx  = indexOf(h, "GRD_ID");
            int popIx = indexOf(h, "Pop_Tot");
            if (idIx < 0)
                throw new IOException("GRD_ID column missing");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] f = line.split(delimiter, -1);
                if (f.length <= idIx) continue;

                String id = f[idIx].trim();
                int pop   = (popIx >= 0 && popIx < f.length) ? parseInt(f[popIx]) : 0;

                Matcher m = ID_PATTERN.matcher(id);
                if (!m.matches()) continue;        // skip malformed IDs

                int nMeters = Integer.parseInt(m.group(1));
                int eMeters = Integer.parseInt(m.group(2));

                double minX = eMeters;
                double minY = nMeters;
                double maxX = minX + 1_000;
                double maxY = minY + 1_000;

                double cx = (minX + maxX) * 0.5;
                double cy = (minY + maxY) * 0.5;

                double[] ll = inverse(cx, cy);

                tiles.add(new Tile(id, pop,
                        minX, minY, maxX, maxY,
                        ll[1], ll[0]));
            }
        }
        return tiles;
    }

    //Geometry helpers
    private static double[] inverse(double x, double y) {
        double xp = x - FE;
        double yp = y - FN;
        double rho  = Math.hypot(xp, yp);
        if (rho == 0) return new double[]{ Math.toDegrees(LON0), Math.toDegrees(LAT0) };

        double c     = 2 * Math.asin(rho / (2 * R));
        double sinC  = Math.sin(c);
        double cosC  = Math.cos(c);

        double lat = Math.asin(cosC * Math.sin(LAT0) + (yp * sinC * Math.cos(LAT0) / rho));

        double lon = LON0 + Math.atan2(xp * sinC, rho * Math.cos(LAT0) * cosC - yp * Math.sin(LAT0) * sinC);

        return new double[]{ Math.toDegrees(lon), Math.toDegrees(lat) };
    }

    private static int indexOf(String[] hdr, String name) {
        for (int i = 0; i < hdr.length; i++)
            if (hdr[i].equalsIgnoreCase(name)) return i;
        return -1;
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    //3. Tile -> PopulationTile
    public static List<PopulationTile> toPopulationTiles(List<Tile> tiles) {

        final double METRES_PER_DEG_LAT = 111_320.0;
        final double HALF = 500.0; // half-cell (1 km / 2)

        List<PopulationTile> out = new ArrayList<>(tiles.size());

        for (Tile t : tiles) {

            double centreLatDeg = t.getCentreLat();
            double centreLonDeg = t.getCentreLon();
            double phiRad       = Math.toRadians(centreLatDeg);

            double dLat = HALF / METRES_PER_DEG_LAT;
            double dLon = HALF / (METRES_PER_DEG_LAT * Math.cos(phiRad));

            double northLat = centreLatDeg + dLat;
            double southLat = centreLatDeg - dLat;
            double westLon  = centreLonDeg - dLon;
            double eastLon  = centreLonDeg + dLon;

            out.add(new PopulationTile(
                    northLat, westLon, southLat, eastLon,
                    t.getPopulation()));
        }
        return out;
    }

    //Populate with stops
    public static List<PopulationTile> buildTilesWithStops() throws IOException {

        List<Tile> allTiles   = read();
        //make tiles into population tiles :)
        List<PopulationTile> ptiles = toPopulationTiles(allTiles);
        List<Stop> stops = new TDSImplement().getAllStops();
        System.out.printf("Total stops loaded: %,d%n", stops.size());

        int unmatched = 0;
        for (Stop s : stops) {
            boolean found = false;
            for (PopulationTile t : ptiles) {
                if (t.contains(s.getStopLat(), s.getStopLon())) {
                    t.addStop(s);
                    found = true;
                    break;
                }
            }
            if (!found) unmatched++;
        }
        if (unmatched > 0)
            System.err.printf("WARNING – %d stop(s) did not fall inside any tile!%n", unmatched);
        // keep only tiles with at least one stop
        return ptiles.stream()
                .filter(t -> !t.stopsList.isEmpty())
                .toList();
    }
    // Scoring / ranking
    public static List<PopulationTile> scoreAndRankTiles(List<PopulationTile> tilesWithStops) {
        int minPop   = Integer.MAX_VALUE, maxPop   = Integer.MIN_VALUE;
        int minStops = Integer.MAX_VALUE, maxStops = Integer.MIN_VALUE;

        for (PopulationTile t : tilesWithStops) {
            int pop   = t.getPopulation();
            int stops = t.stopsList.size();
            if (pop   < minPop)   minPop   = pop;
            if (pop   > maxPop)   maxPop   = pop;
            if (stops < minStops) minStops = stops;
            if (stops > maxStops) maxStops = stops;
        }
        maxPop = Math.max(1, maxPop); // just to avoid division by zero(not going to happen, but you never know)
        for (PopulationTile t : tilesWithStops) {
            t.popScore=(t.getPopulation())   / (double) (maxPop);
        }
        return tilesWithStops;
    }
}

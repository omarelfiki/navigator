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

    private GridReaderSimple() {/* utility class */}

    private static final Path CSV = Path.of("src/main/resources/GrigliaPop2021_Ind_ITA_CSV.txt");

    private static final double R = 6_371_007.181;
    private static final double LAT0 = Math.toRadians(52);
    private static final double LON0 = Math.toRadians(10);
    private static final double FE = 4_321_000;
    private static final double FN = 3_210_000;

    private static final Pattern ID_PATTERN = Pattern.compile(".*N(\\d{4,7})E(\\d{4,7})");

    public static List<Tile> read() throws IOException {
        List<Tile> tiles = new ArrayList<>(350_000);

        try (BufferedReader br = Files.newBufferedReader(CSV, StandardCharsets.UTF_8)) {
            String header = br.readLine();
            if (header == null) throw new IOException("Empty file: " + CSV);
            if (!header.isEmpty() && header.charAt(0) == '\uFEFF') header = header.substring(1);

            final String delimiter = header.contains(";") ? ";" : ",";
            String[] h = header.split(delimiter, -1);

            int idIx = indexOf(h, "GRD_ID");
            int popIx = indexOf(h, "Pop_Tot");
            if (idIx < 0) throw new IOException("'GRD_ID' column missing in " + CSV);
            if (popIx < 0) throw new IOException("'Pop_Tot' column missing in " + CSV);

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;

                String[] f = line.split(delimiter, -1);
                if (f.length <= Math.max(idIx, popIx)) continue;

                int pop;
                try {
                    pop = parsePopulation(f[popIx]);
                } catch (NumberFormatException ex) {
                    throw new IOException("Bad population '" + f[popIx] + "' in " + CSV + " line " + lineNumber, ex);
                }
                if (pop <= 0) continue;

                String id = f[idIx].trim();
                Matcher m = ID_PATTERN.matcher(id);
                if (!m.matches()) continue;

                int nMeters = Integer.parseInt(m.group(1));
                int eMeters = Integer.parseInt(m.group(2));

                double minX = eMeters;
                double minY = nMeters;
                double maxX = minX + 1_000;
                double maxY = minY + 1_000;

                double cx = (minX + maxX) * 0.5;
                double cy = (minY + maxY) * 0.5;
                double[] ll = inverse(cx, cy);

                tiles.add(new Tile(id, pop, minX, minY, maxX, maxY, ll[1], ll[0]));
            }
        }
        return tiles;
    }

    public static List<PopulationTile> toPopulationTiles(List<Tile> tiles) {
        final double METRES_PER_DEG_LAT = 111_320.0;
        final double HALF = 500.0;

        List<PopulationTile> out = new ArrayList<>(tiles.size());
        for (Tile t : tiles) {
            double centreLatDeg = t.getCentreLat();
            double centreLonDeg = t.getCentreLon();
            double phiRad = Math.toRadians(centreLatDeg);

            double dLat = HALF / METRES_PER_DEG_LAT;
            double dLon = HALF / (METRES_PER_DEG_LAT * Math.cos(phiRad));

            double northLat = centreLatDeg + dLat;
            double southLat = centreLatDeg - dLat;
            double westLon = centreLonDeg - dLon;
            double eastLon = centreLonDeg + dLon;

            out.add(new PopulationTile(northLat, westLon, southLat, eastLon, t.getPopulation()));
        }
        return out;
    }

    public static List<PopulationTile> buildTilesWithStops() throws IOException {
        List<PopulationTile> ptiles = toPopulationTiles(read());
        List<Stop> stops = new TDSImplement().getAllStops();
        System.out.printf("Total stops loaded: %,d%n", stops.size());

        List<Stop> unmatched = new ArrayList<>();
        for (Stop s : stops) {
            boolean found = false;
            for (PopulationTile t : ptiles) {
                if (t.contains(s.getStopLat(), s.getStopLon())) {
                    t.addStop(s);
                    found = true;
                    break;
                }
            }
            if (!found) {
                unmatched.add(s);
            }
        }

        if (!unmatched.isEmpty()) {
            System.err.printf("WARNING – %d stop(s) did not fall inside any tile!%n", unmatched.size());
            unmatched.forEach(s -> System.out.printf("Unmatched Stop: ID=%s, lat=%.6f, lon=%.6f%n",
                    s.getStopId(), s.getStopLat(), s.getStopLon()));
        }

        return ptiles.stream().filter(t -> !t.stopsList.isEmpty()).toList();
    }

    public static List<PopulationTile> scoreAndRankTiles(List<PopulationTile> tilesWithStops) {
        int maxPop = tilesWithStops.stream().mapToInt(PopulationTile::getPopulation).max().orElse(1);

        for (PopulationTile t : tilesWithStops) {
            t.popScore = t.getPopulation() / (double) maxPop;
        }
        return tilesWithStops;
    }

    private static double[] inverse(double x, double y) {
        double xp = x - FE;
        double yp = y - FN;
        double rho = Math.hypot(xp, yp);
        if (rho == 0) return new double[]{Math.toDegrees(LON0), Math.toDegrees(LAT0)};

        double c = 2 * Math.asin(rho / (2 * R));
        double sinC = Math.sin(c);
        double cosC = Math.cos(c);

        double lat = Math.asin(cosC * Math.sin(LAT0) + (yp * sinC * Math.cos(LAT0) / rho));
        double lon = LON0 + Math.atan2(xp * sinC, rho * Math.cos(LAT0) * cosC - yp * Math.sin(LAT0) * sinC);

        return new double[]{Math.toDegrees(lon), Math.toDegrees(lat)};
    }

    private static int indexOf(String[] hdr, String name) {
        for (int i = 0; i < hdr.length; i++)
            if (hdr[i].equalsIgnoreCase(name)) return i;
        return -1;
    }

    private static int parsePopulation(String raw) throws NumberFormatException {
        String cleaned = raw.strip()
                .replace('\u00A0', ' ')
                .replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) throw new NumberFormatException("No digits in '" + raw + "'");
        return Integer.parseInt(cleaned);
    }
}

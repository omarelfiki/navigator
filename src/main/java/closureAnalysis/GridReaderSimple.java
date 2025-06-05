package closureAnalysis;

import db.TDSImplement;
import models.Stop;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class GridReaderSimple {
    private static final Path CSV = Path.of("src/main/resources/GrigliaPop2021_Ind_ITA_CSV.txt");
    private static final double R    = 6_371_007.181;
    private static final double LAT0 = Math.toRadians(52.0);
    private static final double LON0 = Math.toRadians(10.0);
    private static final double FE   = 4_321_000.0;
    private static final double FN   = 3_210_000.0;
    private static final Pattern ID_PATTERN = Pattern.compile("N(\\d{4,7}).*E(\\d{4,7})|E(\\d{4,7}).*N(\\d{4,7})");

    public static List<Tile> read() throws IOException {

        List<Tile> tiles = new ArrayList<>(350_000);

        try (BufferedReader br = Files.newBufferedReader(CSV)) {

            /* header — remove BOM if present */
            String header = br.readLine();
            if (header == null) throw new IOException("Empty file: " + CSV);
            if (header.charAt(0) == '\uFEFF') header = header.substring(1);

            String[] h = header.split(";", -1);
            int idIx  = indexOf(h, "GRD_ID");
            int popIx = indexOf(h, "Pop_Tot");
            if (idIx < 0) throw new IOException("GRD_ID column missing");

            String line;
            while ((line = br.readLine()) != null) {

                String[] f = line.split(";", -1);
                if (f.length <= idIx) continue;

                String id = f[idIx].trim();
                int pop   = (popIx >= 0 && popIx < f.length) ? parseInt(f[popIx]) : 0;

                Matcher m = ID_PATTERN.matcher(id);
                if (!m.find()) continue;                      // skip malformed

                int nMeters = m.group(1) != null ? Integer.parseInt(m.group(1))
                        : Integer.parseInt(m.group(4));
                int eMeters = m.group(2) != null ? Integer.parseInt(m.group(2))
                        : Integer.parseInt(m.group(3));

                /* IDs already include FE/FN — use as-is */
                double minX = eMeters;
                double minY = nMeters;
                double maxX = minX + 1_000.0;
                double maxY = minY + 1_000.0;

                /* WGS-84 centre */
                double cx = (minX + maxX) / 2.0;
                double cy = (minY + maxY) / 2.0;
                double[] ll = inverse(cx, cy);   // [lon°, lat°]

                tiles.add(new Tile(id, pop,
                        minX, minY, maxX, maxY,
                        ll[1], ll[0]));
            }
        }
        return tiles;
    }

    /* spherical LAEA inverse — subtract FE/FN once */
    private static double[] inverse(double x, double y) {

        double xp = x - FE;
        double yp = y - FN;

        double rho = Math.hypot(xp, yp);
        if (rho == 0)
            return new double[]{ Math.toDegrees(LON0), Math.toDegrees(LAT0) };

        double c = 2 * Math.asin(rho / (2 * R));
        double sinC = Math.sin(c), cosC = Math.cos(c);

        double lat = Math.asin(cosC * Math.sin(LAT0) +
                yp * sinC * Math.cos(LAT0) / rho);
        double lon = LON0 + Math.atan2(xp * sinC,
                rho * Math.cos(LAT0) * cosC - yp * Math.sin(LAT0) * sinC);

        return new double[]{ Math.toDegrees(lon), Math.toDegrees(lat) };
    }

    /* helpers */
    private static int indexOf(String[] hdr, String name){
        for (int i = 0; i < hdr.length; i++)
            if (hdr[i].equalsIgnoreCase(name)) return i;
        return -1;
    }
    private static int parseInt(String s){
        try { return Integer.parseInt(s.trim()); }
        catch(NumberFormatException e){ return 0; }
    }

    public static List<Tile> filterByBoundingBox(List<Tile> tiles, double latTopLeft,double lonTopLeft,double latBottomRight,double lonBottomRight) {
        List<Tile> filteredTiles = new ArrayList<>();
        for(Tile t : tiles) {
            if (t.getCentreLat()> latTopLeft && t.getCentreLat() < latBottomRight && t.getCentreLon()> lonTopLeft && t.getCentreLon() < lonBottomRight) {
                filteredTiles.add(t);
                System.out.printf("Tile %s is within bounding box%n", t.id());
            } else {
                System.out.printf("Tile %s is outside bounding box%n", t.id());
            }
        }
        return filteredTiles;
    }
    public static PopulationTile borders(Tile t) {
        double centreLatDeg = t.getCentreLat();
        double centreLonDeg = t.getCentreLon();
        double R = 6_371_000.0;
        double HALF = 500.0;
        double φ = Math.toRadians(centreLatDeg);

        double dLat = HALF / R;                 // radians
        double dLon = HALF / (R * Math.cos(φ));

        double northLat = centreLatDeg + Math.toDegrees(dLat);
        double southLat = centreLatDeg - Math.toDegrees(dLat);
        double eastLon = centreLonDeg + Math.toDegrees(dLon);
        double westLon = centreLonDeg - Math.toDegrees(dLon);
        PopulationTile p = new PopulationTile(northLat, westLon, southLat, eastLon, t.getPopulation());
        return p;
    }



    /* simple check */
    public static void main(String[] args) throws IOException {
        List<Tile> tiles = filterByBoundingBox(read(),35.508021, 12.597250,41.794962, 12.662777);
        //List<Stop> allStops = new TDSImplement().getAllStops();
        List<PopulationTile> populationTiles = new ArrayList<>();
        for(Tile t:tiles){
            populationTiles.add(borders(t));
        }
//        for (PopulationTile p : populationTiles) {
//            for (Stop stop : allStops) {
//                    p.addStop(stop);
//            }
//        }


        System.out.printf("First tile");
    }
}

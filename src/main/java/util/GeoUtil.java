package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

import static util.DebugUtli.getDebugMode;

public class GeoUtil {
    private static final String API_KEY = "AIzaSyDWUFIdOzWZeq2BsFfTMMif-VdY2YSqmKg";

    public static double[] getCoordinatesFromAddress(String address) {
        boolean isDebugMode = getDebugMode();
        try {
            if(!NetworkUtil.isNetworkAvailable()) {
                if (isDebugMode) System.err.println("Geocode Error: Network is not available.");
                return null;
            }

            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            JSONArray results = getGeo(encodedAddress);
            if (results.isEmpty()) return null;

            JSONObject location = results.getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location");

            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");

            return new double[]{lat, lng};

        } catch (Exception e) {
            System.err.println("Geocode Error: " + e);
            return null;
        }
    }

    private static JSONArray getGeo(String encodedAddress) throws IOException {
        String urlStr = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                encodedAddress, API_KEY
        );

        URI uri = URI.create(urlStr);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject json = new JSONObject(response.toString());
        return json.getJSONArray("results");
    }
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // Convert to meters
    }

    public static double[] parseCoords(String coords) {
        boolean isDebugMode = getDebugMode();
        String[] parts = coords.split(",");
        if (parts.length != 2) {
            if (isDebugMode) System.out.println("Invalid coordinates format");
            return null;
        }
        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            return new double[]{lat, lng};
        } catch (NumberFormatException e) {
            if (isDebugMode) System.out.println("Invalid coordinates format");
            return null;
        }
    }
}
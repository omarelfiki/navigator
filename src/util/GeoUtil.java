
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

public class GeoUtil {
    private static final String API_KEY = "AIzaSyDWUFIdOzWZeq2BsFfTMMif-VdY2YSqmKg";

    public static double[] getCoordinatesFromAddress(String address) {
        try {

            if(!NetworkUtil.isNetworkAvailable()) {
                System.out.println("Geocode Error: Network is not available.");
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
}
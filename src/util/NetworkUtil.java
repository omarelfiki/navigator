import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class NetworkUtil {
    public static boolean isNetworkAvailable() {
        try {
            URI uri = new URI("https://www.google.com");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000); // Timeout in milliseconds
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception e) {
            return false;
        }
    }
}
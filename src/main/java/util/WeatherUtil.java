package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.json.JSONObject;

import static util.DebugUtli.getDebugMode;

public class WeatherUtil {
    private static final String API_KEY = "d398b5c0a8091474dacc1edbab7b736e";
    private static final String BASE_URL = "https://api.openweathermap.org/data/3.0/onecall";

    @SuppressWarnings("deprecation")
    public static Object[] getWeather(String lat, String lon) {
        boolean isDebugMode = getDebugMode();
        try {
            String urlString = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&units=metric" + "&appid=" + API_KEY;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject current = jsonResponse.getJSONObject("current");
                JSONObject weather = current.getJSONArray("weather").getJSONObject(0);
                double temperature = current.getDouble("temp");
                String icon = weather.getString("icon");

                return new Object[]{temperature, icon};
            } else {
                if (isDebugMode) System.out.println("Error: Unable to fetch weather data. Response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            if (isDebugMode) System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public static Task<Void> createWeatherTask(double lat, double lon, Text temperatureLabel, ImageView weatherIcon) {
        return new Task<>() {
            @Override
            protected Void call() {
                try {
                    Object[] weatherData = WeatherUtil.getWeather(String.valueOf(lat), String.valueOf(lon));
                    if (weatherData != null) {
                        Double temp = (Double) weatherData[0];
                        String iconCode = (String) weatherData[1];
                        Platform.runLater(() -> {
                            temperatureLabel.setText(temp.intValue() + "°C");
                            weatherIcon.setImage(new Image("https://openweathermap.org/img/wn/" + iconCode + "@2x.png"));
                        });
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> temperatureLabel.setText("Error"));
                }
                return null;
            }
        };
    }
}
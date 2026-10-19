package com.tourmate.bd.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Weather API Client for TourMate BD (Topic 11: Networking, HTTP Requests, JSON Parsing & Background Operations).
 * Fetches real-time weather information using Open-Meteo REST API asynchronously off the UI thread.
 */
public class WeatherApiClient {

    public static class WeatherInfo {
        public double temperature;
        public double windSpeed;
        public int weatherCode;
        public String condition;
        public String conditionBn;
        public String emoji;

        public WeatherInfo(double temperature, double windSpeed, int weatherCode) {
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.weatherCode = weatherCode;
            resolveCondition();
        }

        private void resolveCondition() {
            if (weatherCode == 0) {
                condition = "Clear Sky";
                conditionBn = "পরিষ্কার আকাশ";
                emoji = "☀️";
            } else if (weatherCode >= 1 && weatherCode <= 3) {
                condition = "Partly Cloudy";
                conditionBn = "আংশিক মেঘলা";
                emoji = "⛅";
            } else if (weatherCode >= 51 && weatherCode <= 67) {
                condition = "Rainy";
                conditionBn = "বৃষ্টিপাত";
                emoji = "🌧️";
            } else if (weatherCode >= 80 && weatherCode <= 82) {
                condition = "Rain Showers";
                conditionBn = "ঝিরিঝিরি বৃষ্টি";
                emoji = "🌦️";
            } else if (weatherCode >= 95) {
                condition = "Thunderstorm";
                conditionBn = "বজ্রবৃষ্টি";
                emoji = "⛈️";
            } else {
                condition = "Fair Weather";
                conditionBn = "অনুকূল আবহাওয়া";
                emoji = "🌤️";
            }
        }
    }

    public interface WeatherCallback {
        void onSuccess(WeatherInfo info);
        void onError(String errorMessage);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static WeatherApiClient instance;

    public static synchronized WeatherApiClient getInstance() {
        if (instance == null) {
            instance = new WeatherApiClient();
        }
        return instance;
    }

    /**
     * Asynchronously fetches current weather for given latitude & longitude in the background.
     */
    public void fetchWeather(double latitude, double longitude, WeatherCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                // Open-Meteo free open-source weather API
                String urlString = String.format(
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current_weather=true",
                        latitude, longitude
                );

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // JSON Parsing
                    JSONObject root = new JSONObject(response.toString());
                    if (root.has("current_weather")) {
                        JSONObject currentWeather = root.getJSONObject("current_weather");
                        double temp = currentWeather.getDouble("temperature");
                        double wind = currentWeather.getDouble("windspeed");
                        int code = currentWeather.getInt("weathercode");

                        WeatherInfo weatherInfo = new WeatherInfo(temp, wind, code);

                        // Post result back safely to the UI / Main Thread
                        mainHandler.post(() -> callback.onSuccess(weatherInfo));
                        return;
                    }
                }

                mainHandler.post(() -> callback.onError("Server returned code: " + responseCode));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (Exception ignored) {}
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}

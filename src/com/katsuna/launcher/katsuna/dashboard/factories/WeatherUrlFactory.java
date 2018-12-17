package com.katsuna.launcher.katsuna.dashboard.factories;

public class WeatherUrlFactory {

    private final static String apiKey = "0d341e1a903638b5b5a062d66ed0c550";
    private final static String baseUrl = "http://api.openweathermap.org/data/2.5/";

    public static String getDayForecastUrl(String latitude, String longitude, String language) {
        return baseUrl + "weather?lat=" + latitude + "&lon=" + longitude + "&lang=" + language +
            "&appid=" + apiKey;
    }

    public static String getShortTermUrl(String latitude, String longitude, String language) {
        return baseUrl + "forecast?lat=" + latitude + "&lon=" + longitude + "&lang=" +
            language + "&mode=json&appid=" + apiKey;
    }

    public static String getLongTermUrl(String latitude, String longitude, String language) {
        return baseUrl + "forecast/daily?lat=" + latitude + "&lon=" + longitude + "&lang=" +
            language + "&mode=json&appid=" + apiKey;
    }
}

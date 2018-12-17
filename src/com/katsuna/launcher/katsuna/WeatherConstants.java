package com.katsuna.launcher.katsuna;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class WeatherConstants {

    public static final String CURRENT_WEATHER_INTENT = "CURRENT_WEATHER_INTENT";
    public static final String SHORT_TERM_WEATHER_INTENT = "SHORT_TERM_WEATHER_INTENT";
    public static final String LONG_TERM_WEATHER_INTENT = "LONG_TERM_WEATHER_INTENT";
    public static final String DOWNLOAD_RESPONSE = "DOWNLOAD_RESPONSE";
    public static final String[] LOCATION_PERMISSIONS = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    public static final long LONG_NOT_SET = 0;

    // TEMP UNIT prefs
    public static final String TEMP_UNIT = "TEMP_UNIT";
    public static final String TEMP_UNIT_C = "\u2103";
    public static final String TEMP_UNIT_F = "\u2109";

}

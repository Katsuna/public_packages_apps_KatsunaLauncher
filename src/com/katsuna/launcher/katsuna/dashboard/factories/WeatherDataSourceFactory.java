package com.katsuna.launcher.katsuna.dashboard.factories;

import android.content.Context;

import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherPreferenceDataSource;

public class WeatherDataSourceFactory {

    public static WeatherDataSource getDataSource(Context context) {
        return new WeatherPreferenceDataSource(context);
    }
}

package com.katsuna.launcher.katsuna.dashboard.utils;

import android.location.Location;

public interface IWeatherSync {

    void sync(Location location, WeatherSyncCallback callback);

    interface WeatherSyncCallback {
        void onSuccess();
        void onError();
    }

}

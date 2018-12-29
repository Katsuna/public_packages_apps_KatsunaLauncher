package com.katsuna.launcher.katsuna.dashboard.utils;

import android.location.Location;
import android.os.AsyncTask;

import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.tasks.LongTermWeatherTask;
import com.katsuna.launcher.katsuna.dashboard.tasks.ShortTermWeatherTask;
import com.katsuna.launcher.katsuna.dashboard.tasks.WeatherTask;

import timber.log.Timber;

public class WeatherSync implements IWeatherSync {

    private final WeatherDataSource mWeatherDataSource;
    private WeatherSyncCallback mCallback;

    public WeatherSync(WeatherDataSource weatherDataSource) {
        mWeatherDataSource = weatherDataSource;
    }

    @Override
    public void sync(Location location, WeatherSyncCallback callback) {
        mCallback = callback;
        locationCurrentWeather(location);
    }

    private void locationCurrentWeather(Location location) {
        Timber.d("loading current weather for location %s", location);

        new WeatherTask(null, location, mWeatherDataSource,
            (response) -> {
                if (response.allGood()) {
                    Timber.d("loading short term");
                    loadShortTermWeather(location);
                } else {
                    mCallback.onError();
                    Timber.e("Problem problems: %s", response.problems());
                }
            }
        ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadShortTermWeather(Location location) {
        Timber.d("loading short term weather for location %s", location);

        new ShortTermWeatherTask(null, location, mWeatherDataSource, (response) -> {
            if (response.allGood()) {
                Timber.d("loading long term");
                loadLongTermWeather(location);
            } else {
                mCallback.onError();
                Timber.e("Problem problems: %s", response.problems());
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadLongTermWeather(Location location) {
        Timber.d("loading long term weather for location %s", location);

        new LongTermWeatherTask(null, location, mWeatherDataSource, (response) -> {
            if (response.allGood()) {
                mCallback.onSuccess();
            } else {
                mCallback.onError();
                Timber.e("Problem problems: %s", response.problems());
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


}

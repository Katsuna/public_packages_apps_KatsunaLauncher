package com.katsuna.launcher.katsuna.dashboard.tasks;

import android.content.Context;
import android.location.Location;

import com.katsuna.launcher.katsuna.WeatherConstants;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.factories.WeatherUrlFactory;

public class ShortTermWeatherTask extends WeatherTaskBase {

    public ShortTermWeatherTask(Context context, Location location, WeatherDataSource dataSource,
                                IAsyncTaskFinished asyncTaskFinished) {
        super(context, location, dataSource, asyncTaskFinished);
        TAG = WeatherTask.class.getSimpleName();
        mUrl = WeatherUrlFactory.getShortTermUrl(mLatitude, mLongitude, mLanguage);
        mIntentAction = WeatherConstants.SHORT_TERM_WEATHER_INTENT;
    }

    @Override
    void saveResponse(String response) {
        mDataSource.saveShortTerm(response);
    }
}
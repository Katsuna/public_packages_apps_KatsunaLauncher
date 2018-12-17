package com.katsuna.launcher.katsuna.dashboard.tasks;

import android.content.Context;
import android.location.Location;

import com.katsuna.launcher.katsuna.WeatherConstants;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.factories.WeatherUrlFactory;

public class WeatherTask extends WeatherTaskBase {

    public WeatherTask(Context context, Location location, WeatherDataSource dataSource,
                       IAsyncTaskFinished asyncTaskFinished) {
        super(context, location, dataSource, asyncTaskFinished);
        TAG = WeatherTask.class.getSimpleName();
        mUrl = WeatherUrlFactory.getDayForecastUrl(mLatitude, mLongitude, mLanguage);
        mIntentAction = WeatherConstants.CURRENT_WEATHER_INTENT;
    }

    @Override
    void saveResponse(String response) {
        mDataSource.saveToday(response);
    }
}

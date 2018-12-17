package com.katsuna.launcher.katsuna.dashboard.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.katsuna.launcher.katsuna.dashboard.domain.Weather;

import java.util.List;

public interface WeatherDataSource {

    @Nullable
    Weather getToday();

    @Nullable
    List<Weather> getShortTerm();

    @Nullable
    List<Weather> getLongTerm();

    long getLastUpdate();

    void saveToday(@NonNull String weather);

    void saveShortTerm(@NonNull String weather);

    void saveLongTerm(@NonNull String weather);

    void saveLastUpdateTime();
}

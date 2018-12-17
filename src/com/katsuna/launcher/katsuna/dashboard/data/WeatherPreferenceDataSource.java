package com.katsuna.launcher.katsuna.dashboard.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.katsuna.launcher.katsuna.dashboard.domain.Weather;
import com.katsuna.launcher.katsuna.dashboard.parsers.WeatherParser;
import com.katsuna.launcher.katsuna.dashboard.utils.PrefUtils;

import java.util.List;

import static com.katsuna.launcher.katsuna.WeatherConstants.LONG_NOT_SET;

public class WeatherPreferenceDataSource implements WeatherDataSource {

    private static final String TODAY = "lastToday";
    private static final String SHORT_TERM = "lastShortterm";
    private static final String LONG_TERM = "lastLongterm";
    private static final String LAST_UPDATE = "lastUpdate";
    private final SharedPreferences mSharedPrefs;
    private final Context mContext;

    public WeatherPreferenceDataSource(Context context) {
        mContext = context;
        mSharedPrefs = PrefUtils.getSharedPreferences(context);
    }

    @Nullable
    @Override
    public Weather getToday() {
        String json = mSharedPrefs.getString(TODAY, null);
        return json == null ? null : WeatherParser.parse(json, mContext);
    }

    @Nullable
    @Override
    public List<Weather> getShortTerm() {
        String json = mSharedPrefs.getString(SHORT_TERM, null);
        return json == null ? null : WeatherParser.parseShortTermJson(json, mContext);
    }

    @Nullable
    @Override
    public List<Weather> getLongTerm() {
        String json = mSharedPrefs.getString(LONG_TERM, null);
        return json == null ? null : WeatherParser.parseLongTermJson(json, mContext);
    }

    @Override
    public long getLastUpdate() {
        return mSharedPrefs.getLong(LAST_UPDATE, LONG_NOT_SET);
    }

    @Override
    public void saveToday(@NonNull String weather) {
        saveWeather(TODAY, weather);
    }

    @Override
    public void saveShortTerm(@NonNull String weather) {
        saveWeather(SHORT_TERM, weather);
    }

    @Override
    public void saveLongTerm(@NonNull String weather) {
        saveWeather(LONG_TERM, weather);
    }

    @Override
    public void saveLastUpdateTime() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putLong(LAST_UPDATE, System.currentTimeMillis());
        editor.apply();
    }

    private void saveWeather(@NonNull String key, @NonNull String weather) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(key, weather);
        editor.apply();
    }
}

package com.katsuna.launcher.katsuna.dashboard.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.dashboard.domain.Weather;
import com.katsuna.launcher.katsuna.dashboard.domain.WindDirection;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static com.katsuna.launcher.katsuna.WeatherConstants.TEMP_UNIT;
import static com.katsuna.launcher.katsuna.WeatherConstants.TEMP_UNIT_C;

public class WeatherUtils {

    private static final Map<String, Integer> speedUnits = new HashMap<>(3);
    private static final Map<String, Integer> pressUnits = new HashMap<>(3);

    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }

    public static String getWindDirectionString(Context context, Weather weather) {
        if (!weather.isWindDirectionAvailable()) return "";

        WindDirection windDirection = weather.getWindDirection();
        if (windDirection != null) {
            SharedPreferences sp = PrefUtils.getSharedPreferences(context);
            String pref = sp.getString("windDirectionFormat", null);
            if ("arrow".equals(pref)) {
                return weather.getWindDirection(8).getArrow(context);
            } else if ("abbr".equals(pref)) {
                return weather.getWindDirection().getLocalizedString(context);
            }
        }

        return "";
    }

    public static String localize(Context context, String preferenceKey, String defaultValueKey) {
        SharedPreferences sp = PrefUtils.getSharedPreferences(context);
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    public static String getTempUnit(Context context) {
        SharedPreferences sp = PrefUtils.getSharedPreferences(context);
        return sp.getString(TEMP_UNIT, TEMP_UNIT_C);
    }

    public static String getDay(Context context, Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String day = "";
        switch (dayOfWeek) {
            case 1:
                day = context.getString(R.string.sunday);
                break;
            case 2:
                day = context.getString(R.string.monday);
                break;
            case 3:
                day = context.getString(R.string.tuesday);
                break;
            case 4:
                day = context.getString(R.string.wednesday);
                break;
            case 5:
                day = context.getString(R.string.thursday);
                break;
            case 6:
                day = context.getString(R.string.friday);
                break;
            case 7:
                day = context.getString(R.string.saturday);
                break;
        }
        return day;
    }
}

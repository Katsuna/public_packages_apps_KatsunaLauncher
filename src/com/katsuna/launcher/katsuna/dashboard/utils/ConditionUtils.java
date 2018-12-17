package com.katsuna.launcher.katsuna.dashboard.utils;

import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.dashboard.domain.WeatherCondition;

public class ConditionUtils {

    public static int getDrawableId(WeatherCondition condition, int hourOfDay) {
        int id = 0;
        switch (condition) {
            case THUNDER:
                id = R.drawable.ic_weathericonsthunderstorm;
                break;
            case DRIZZLE:
                id = R.drawable.ic_weathericonslightrain;
                break;
            case RAINY:
                id = R.drawable.ic_weathericonsheavyrain;
                break;
            case SNOWY:
                id = R.drawable.ic_weathericonsheavysnow;
                break;
            case FOGGY:
                id = R.drawable.ic_weathericonsfog;
                break;
            case CLOUDY:
                id = R.drawable.ic_weathericonsclouds;
                break;
            case SUNNY:
                if (hourOfDay >= 7 && hourOfDay < 20) {
                    id = R.drawable.ic_weathericonssun;
                } else {
                    id = R.drawable.ic_weathericonsnight;
                }
                break;
        }

        return id;

    }
}

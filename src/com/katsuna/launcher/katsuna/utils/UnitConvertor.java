package com.katsuna.launcher.katsuna.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.katsuna.launcher.katsuna.dashboard.utils.PrefUtils;

import static com.katsuna.launcher.katsuna.WeatherConstants.TEMP_UNIT;
import static com.katsuna.launcher.katsuna.WeatherConstants.TEMP_UNIT_C;
import static com.katsuna.launcher.katsuna.WeatherConstants.TEMP_UNIT_F;

public class UnitConvertor {
    public static float convertTemperature(Context context, float temperature) {
        SharedPreferences sp = PrefUtils.getSharedPreferences(context);
        String pref = sp.getString(TEMP_UNIT, TEMP_UNIT_C);
        if (pref == null) {
            return temperature;
        }

        switch (pref) {
            case TEMP_UNIT_C:
                return UnitConvertor.kelvinToCelsius(temperature);
            case TEMP_UNIT_F:
                return UnitConvertor.kelvinToFahrenheit(temperature);
            default:
                return temperature;
        }
    }

    private static float kelvinToCelsius(float kelvinTemp) {
        return kelvinTemp - 273.15f;
    }

    private static float kelvinToFahrenheit(float kelvinTemp) {
        return (((9 * kelvinToCelsius(kelvinTemp)) / 5) + 32);
    }

    public static float convertPressure(Context context, float pressure) {
        SharedPreferences sp = PrefUtils.getSharedPreferences(context);
        String pref = sp.getString("pressureUnit", "hPa");
        if (pref == null) {
            return pressure;
        }

        switch (pref) {
            case "kPa":
                return pressure / 10;
            case "mm Hg":
                return (float) (pressure * 0.750061561303);
            case "in Hg":
                return (float) (pressure * 0.0295299830714);
            default:
                return pressure;
        }
    }

    public static double convertWind(Context context, double wind) {
        SharedPreferences sp = PrefUtils.getSharedPreferences(context);
        String pref = sp.getString("speedUnit", "m/s");
        if (pref == null) {
            return wind;
        }

        switch (pref) {
            case "kph":
                return wind * 3.59999999712;
            case "mph":
                return wind * 2.23693629205;
            case "bft":
                if (wind < 0.3) {
                    return 0; // Calm
                } else if (wind < 1.5) {
                    return 1; // Light air
                } else if (wind < 3.3) {
                    return 2; // Light breeze
                } else if (wind < 5.5) {
                    return 3; // Gentle breeze
                } else if (wind < 7.9) {
                    return 4; // Moderate breeze
                } else if (wind < 10.7) {
                    return 5; // Fresh breeze
                } else if (wind < 13.8) {
                    return 6; // Strong breeze
                } else if (wind < 17.1) {
                    return 7; // High wind
                } else if (wind < 20.7) {
                    return 8; // Gale
                } else if (wind < 24.4) {
                    return 9; // Strong gale
                } else if (wind < 28.4) {
                    return 10; // Storm
                } else if (wind < 32.6) {
                    return 11; // Violent storm
                } else {
                    return 12; // Hurricane
                }
            default:
                return wind;
        }
    }

    public static String getBeaufortName(int wind) {
        if (wind == 0) {
            return "Calm";
        } else if (wind == 1) {
            return "Light air";
        } else if (wind == 2) {
            return "Light breeze";
        } else if (wind == 3) {
            return "Gentle breeze";
        } else if (wind == 4) {
            return "Moderate breeze";
        } else if (wind == 5) {
            return "Fresh breeze";
        } else if (wind == 6) {
            return "Strong breeze";
        } else if (wind == 7) {
            return "High wind";
        } else if (wind == 8) {
            return "Gale";
        } else if (wind == 9) {
            return "Strong gale";
        } else if (wind == 10) {
            return "Storm";
        } else if (wind == 11) {
            return "Violent storm";
        } else {
            return "Hurricane";
        }
    }
}

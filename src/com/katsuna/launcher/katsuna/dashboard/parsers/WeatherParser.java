package com.katsuna.launcher.katsuna.dashboard.parsers;

import android.content.Context;
import android.text.TextUtils;

import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.domain.Weather;
import com.katsuna.launcher.katsuna.dashboard.domain.WeatherCondition;
import com.katsuna.launcher.katsuna.dashboard.factories.WeatherDataSourceFactory;
import com.katsuna.launcher.katsuna.dashboard.utils.WeatherUtils;
import com.katsuna.launcher.katsuna.utils.UnitConvertor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class WeatherParser {
    private static final String JSON_NAME = "name";
    private static final String JSON_SYS = "sys";
    private static final String JSON_MAIN = "main";
    private static final String JSON_TEMP = "temp";
    private static final String JSON_PRESSURE = "pressure";
    private static final String JSON_HUMIDITY = "humidity";
    private static final String JSON_COUNTRY = "country";
    private static final String JSON_SUNRISE = "sunrise";
    private static final String JSON_SUNSET = "sunset";
    private static final String JSON_WIND = "wind";
    private static final String JSON_DEG = "deg";
    private static final String JSON_SPEED = "speed";
    private static final String JSON_WEATHER = "weather";
    private static final String JSON_ID = "id";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_LIST = "list";
    private static final String JSON_DT = "dt";
    private static final String JSON_RAIN = "rain";
    private static final String JSON_SNOW = "snow";
    private static final String JSON_DAY = "day";

    private static WeatherCondition getWeatherCondition(int actualId) {
        WeatherCondition output = null;
        if (actualId == 800) {
            output = WeatherCondition.SUNNY;
        } else {
            int id = actualId / 100;
            switch (id) {
                case 2:
                    output = WeatherCondition.THUNDER;
                    break;
                case 3:
                    output = WeatherCondition.DRIZZLE;
                    break;
                case 5:
                    output = WeatherCondition.RAINY;
                    break;
                case 6:
                    output = WeatherCondition.SNOWY;
                    break;
                case 7:
                    output = WeatherCondition.FOGGY;
                    break;
                case 8:
                    output = WeatherCondition.CLOUDY;
                    break;
            }
        }
        return output;
    }

    public static Weather parse(String result, Context context) {
        try {
            JSONObject json = new JSONObject(result);

            Weather weather = new Weather();

            // City
            weather.setCity(json.optString(JSON_NAME));

            JSONObject jsonMain = json.optJSONObject(JSON_MAIN);
            if (jsonMain != null) {
                // Temperature
                String temperature = jsonMain.optString(JSON_TEMP);
                if (!TextUtils.isEmpty(temperature)) {
                    setTemperature(context, temperature, weather);
                }

                // Pressure
                double pressureDbl = jsonMain.optDouble(JSON_PRESSURE);
                if (pressureDbl != Double.NaN) {
                    double pressure = UnitConvertor.convertPressure(context, (float) pressureDbl);
                    String pressureDesc = String.format("%s: %s %s",
                        context.getString(R.string.pressure),
                        new DecimalFormat("#.0").format(pressure),
                        WeatherUtils.localize(context, "pressureUnit", "hPa"));
                    weather.setPressure(pressureDesc);
                }

                // Humidity
                weather.setHumidity(jsonMain.optString(JSON_HUMIDITY));
            }

            JSONObject jsonSys = json.optJSONObject(JSON_SYS);
            if (jsonSys != null) {
                weather.setCountry(jsonSys.optString(JSON_COUNTRY));
                weather.setSunrise(jsonSys.optString(JSON_SUNRISE));
                weather.setSunset(jsonSys.optString(JSON_SUNSET));
            }

            // Wind
            JSONObject jsonWind = json.optJSONObject(JSON_WIND);
            if (jsonWind != null) {
                double deg = jsonWind.optDouble(JSON_DEG);
                if (deg != Double.NaN) {
                    weather.setWindDirectionDegree(deg);
                }

                double wind = jsonWind.optDouble(JSON_SPEED);
                wind = UnitConvertor.convertWind(context, wind);

                String windDescription = String.format("%s: %s %s %s",
                    context.getString(R.string.wind),
                    new DecimalFormat("#.0").format(wind),
                    WeatherUtils.localize(context, "speedUnit", "m/s"),
                    WeatherUtils.getWindDirectionString(context, weather));
                weather.setWind(windDescription);
            }

            JSONArray jsonArrayWeather = json.optJSONArray(JSON_WEATHER);
            if (jsonArrayWeather != null) {
                JSONObject jsonWeather = jsonArrayWeather.optJSONObject(0);
                if (jsonWeather != null) {
                    // Condition
                    int conditionId = jsonWeather.optInt(JSON_ID);
                    if (conditionId != 0) {
                        weather.setCondition(getWeatherCondition(conditionId));
                    }

                    String description = jsonWeather.optString(JSON_DESCRIPTION);
                    if (!TextUtils.isEmpty(description)) {
                        description = description.substring(0, 1).toUpperCase() +
                            description.substring(1).toLowerCase();
                        weather.setDescription(description);
                    }
                }
            }

            // LastUpdate
            WeatherDataSource dataSource = WeatherDataSourceFactory.getDataSource(context);
            long lastUpdateTimeInMillis = dataSource.getLastUpdate();
            String lastUpdate;
            if (lastUpdateTimeInMillis < 0) {
                // No time
                lastUpdate = "";
            } else {
                lastUpdate = R.string.last_update +
                    WeatherUtils.formatTimeWithDayIfNotToday(context, lastUpdateTimeInMillis);
            }
            weather.setLastUpdated(lastUpdate);

            return weather;
        } catch (JSONException e) {
            Timber.e(e, "JSONException Data %s", result);
            return new Weather();
        }
    }

    private static void setTemperature(Context context, String temp, Weather weather) {
        float tempKelvin = Float.parseFloat(temp);
        float temperature = UnitConvertor.convertTemperature(context, tempKelvin);
        temperature = Math.round(temperature);
        weather.setTemperature(String.valueOf(temperature));

        // TemperatureUnit
        String tempUnit = WeatherUtils.getTempUnit(context);
        weather.setTemperatureUnit(String.valueOf(tempUnit));
    }

    public static List<Weather> parseShortTermJson(String result, Context context) {
        ArrayList<Weather> forecast = new ArrayList<>();

        try {
            JSONObject reader = new JSONObject(result);

            JSONArray list = reader.getJSONArray(JSON_LIST);
            for (int i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject jsonItem = list.getJSONObject(i);

                // Date
                weather.setDate(jsonItem.getString(JSON_DT));

                JSONObject jsonMain = jsonItem.optJSONObject(JSON_MAIN);
                if (jsonMain != null) {
                    // Temperature
                    String temperature = jsonMain.optString(JSON_TEMP);
                    if (!TextUtils.isEmpty(temperature)) {
                        setTemperature(context, temperature, weather);
                    }
                    weather.setPressure(jsonMain.optString(JSON_PRESSURE));
                    weather.setHumidity(jsonMain.optString(JSON_HUMIDITY));
                }

                // Wind
                JSONObject windObj = jsonItem.optJSONObject(JSON_WIND);
                if (windObj != null) {
                    weather.setWind(windObj.optString(JSON_SPEED));
                    weather.setWindDirectionDegree(windObj.optDouble(JSON_DEG));
                }

                JSONArray jsonArrayWeather = jsonItem.optJSONArray(JSON_WEATHER);
                if (jsonArrayWeather != null) {
                    JSONObject jsonWeather = jsonArrayWeather.optJSONObject(0);
                    if (jsonWeather != null) {
                        weather.setDescription(jsonWeather.optString(JSON_DESCRIPTION));
                        final String idString = jsonWeather.getString(JSON_ID);
                        weather.setId(idString);
                        weather.setCondition(getWeatherCondition(Integer.parseInt(idString)));
                    }
                }

                // Rain
                JSONObject rainObj = jsonItem.optJSONObject(JSON_RAIN);
                String rain;
                if (rainObj != null) {
                    rain = WeatherParser.getRainString(rainObj);
                } else {
                    JSONObject snowObj = jsonItem.optJSONObject(JSON_SNOW);
                    if (snowObj != null) {
                        rain = WeatherParser.getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                forecast.add(weather);
            }
        } catch (JSONException e) {
            Timber.e(e, "JSONException Data");
        }
        return forecast;
    }

    public static List<Weather> parseLongTermJson(String result, Context context) {
        ArrayList<Weather> forecast = new ArrayList<>();

        try {
            JSONObject jsonRoot = new JSONObject(result);

            JSONArray list = jsonRoot.getJSONArray(JSON_LIST);
            for (int i = 0; i < list.length(); i++) {
                JSONObject jsonItem = list.getJSONObject(i);

                Weather weather = new Weather();

                // Date
                weather.setDate(jsonItem.optString(JSON_DT));

                // Temperature
                JSONObject jsonTemp = jsonItem.optJSONObject(JSON_TEMP);
                if (jsonTemp != null) {
                    String temperature = jsonTemp.optString(JSON_DAY);
                    if (!TextUtils.isEmpty(temperature)) {
                        setTemperature(context, temperature, weather);
                    }
                }

                JSONArray jsonArrayWeather = jsonItem.optJSONArray(JSON_WEATHER);
                if (jsonArrayWeather != null) {
                    JSONObject jsonWeather = jsonArrayWeather.optJSONObject(0);
                    if (jsonWeather != null) {
                        weather.setDescription(jsonWeather.optString(JSON_DESCRIPTION));
                        final String idString = jsonWeather.getString(JSON_ID);
                        weather.setId(idString);
                        weather.setCondition(getWeatherCondition(Integer.parseInt(idString)));
                    }
                }

                // Wind
                JSONObject windObj = jsonItem.optJSONObject(JSON_WIND);
                if (windObj != null) {
                    weather.setWind(windObj.optString(JSON_SPEED));
                    weather.setWindDirectionDegree(windObj.optDouble(JSON_DEG));
                }

                weather.setPressure(jsonItem.optString(JSON_PRESSURE));
                weather.setHumidity(jsonItem.optString(JSON_HUMIDITY));

                JSONObject rainObj = jsonItem.optJSONObject(JSON_RAIN);
                String rain;
                if (rainObj != null) {
                    rain = WeatherParser.getRainString(rainObj);
                } else {
                    JSONObject snowObj = jsonItem.optJSONObject(JSON_SNOW);
                    if (snowObj != null) {
                        rain = WeatherParser.getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                forecast.add(weather);
            }
        } catch (JSONException e) {
            Timber.e(e, "JSONException Data");
        }
        return forecast;
    }

    private static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }
}
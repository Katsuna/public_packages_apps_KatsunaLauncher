package com.katsuna.launcher.katsuna.dashboard;

import com.katsuna.launcher.katsuna.BasePresenter;
import com.katsuna.launcher.katsuna.BaseView;
import com.katsuna.launcher.katsuna.dashboard.domain.Weather;

import java.util.List;

public interface DashboardContract {

    interface View extends BaseView<Presenter> {

        void showMissingLocationPermissions(boolean flag);

        void showSyncProblem();

        void showNoInternetConnection();

        void showNoGpsProviderEnabled();

        void showNoLocationFound();

        void showWeather(boolean flag);

        void showNoWeatherWarning(boolean flag);

        void showNoRecentWeatherWarning(boolean flag);

        void setCurrentDate(String date);

        void setCurrentWeather(Weather weather);

        void setShortTermWeather(List<Weather> weatherList);

        void setLongTermWeather(List<Weather> weatherList);

        void showShortTermWeather();

        void showLongTermWeather();

        void showDate(boolean flag);

        void showExtendedSettings(boolean flag);

        void showExtendedCalendar(boolean flag);

        void showExtendedWeather(boolean flag);

        void showCalendarAppInstallationDialog();

        void setBrightnessLevel(int level);

        void setVolume(int level);

        void showBatteryLevel(int level);

        void showWifiStatus(boolean status);

        void showGpsStatus(boolean status);

        void showDndStatus(boolean status);

        void askPermissionToWriteSettings();
    }

    interface Presenter extends BasePresenter {

        void loadData();

        void selectLongTermWeather();

        void selectShortTermWeather();

        void sync();

        void openCalendarApp();

        void setBrightness(int level);

        void loadVolume();

        void loadWeather();

        void setVolume(int level);

        void setWifiStatus(boolean status);

        void setDndStatus(boolean status);

        void expandDashboardView(DashboardViewType dashboardViewType);

        void setGpsProvider();

        void launchSettings();
    }

}

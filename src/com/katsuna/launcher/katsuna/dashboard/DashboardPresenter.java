package com.katsuna.launcher.katsuna.dashboard;

import android.location.Location;
import android.support.annotation.NonNull;

import com.katsuna.commons.utils.KatsunaUtils;
import com.katsuna.launcher.katsuna.dashboard.data.LocationDataSource;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.domain.Weather;
import com.katsuna.launcher.katsuna.dashboard.utils.IDeviceUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.IPermissionUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.ISettingsController;
import com.katsuna.launcher.katsuna.dashboard.utils.IWeatherSync;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.katsuna.launcher.katsuna.WeatherConstants.LOCATION_PERMISSIONS;

public class DashboardPresenter implements DashboardContract.Presenter {

    private static final String TAG = DashboardPresenter.class.getSimpleName();

    @NonNull
    private final WeatherDataSource mWeatherDataSource;

    @NonNull
    private final DashboardContract.View mDashboardView;
    private final IPermissionUtils mPermissionUtils;
    private final LocationDataSource mLocationDataSource;
    private final IDeviceUtils mDeviceUtils;
    private final ISettingsController mSettingsController;
    private final IWeatherSync mWeatherSync;
    private Weather mTodayWeather;
    private List<Weather> mShortTermWeather;
    private List<Weather> mLongTermWeather;

    public DashboardPresenter(@NonNull WeatherDataSource weatherDataSource,
                              @NonNull DashboardContract.View weatherView,
                              @NonNull IPermissionUtils permissionUtils,
                              @NonNull LocationDataSource locationDataSource,
                              @NonNull IDeviceUtils deviceUtils,
                              @NonNull ISettingsController settingsController,
                              @NonNull IWeatherSync weatherSync) {
        mWeatherDataSource = weatherDataSource;
        mDashboardView = weatherView;
        mPermissionUtils = permissionUtils;
        mLocationDataSource = locationDataSource;
        mDeviceUtils = deviceUtils;
        mSettingsController = settingsController;
        mWeatherSync = weatherSync;

        mDashboardView.setPresenter(this);
    }

    @Override
    public void loadData() {
        loadSettings();
        loadWeather();
    }

    private void loadSettings() {
        // first write setting permissions must be granted
        if (mSettingsController.canModifySystemSetting()) {
            mDashboardView.setBrightnessLevel(mSettingsController.getBrightness());
            mDashboardView.showBatteryLevel(mSettingsController.getBatterLevel());
            mDashboardView.showWifiStatus(mSettingsController.isWifiEnabled());
            mDashboardView.showGpsStatus(mDeviceUtils.isLocationGpsProviderEnabled());
            mDashboardView.showDndStatus(mSettingsController.isDndModeOn());
        } else {
            mDashboardView.askPermissionToWriteSettings();
        }
    }

    @Override
    public void loadWeather() {
        // then location permissions must be granted
        boolean locationPermissionGranted = mPermissionUtils.hasPermissions(LOCATION_PERMISSIONS);
        if (!locationPermissionGranted) {
            mDashboardView.showMissingLocationPermissions(true);
            mDashboardView.showWeather(false);
            return;
        } else {
            mDashboardView.showMissingLocationPermissions(false);
        }

        mTodayWeather = mWeatherDataSource.getToday();
        mShortTermWeather = mWeatherDataSource.getShortTerm();
        mLongTermWeather = mWeatherDataSource.getLongTerm();

        boolean noWeatherData = (mTodayWeather == null);
        mDashboardView.showNoWeatherWarning(noWeatherData);

        if (!noWeatherData) {
            // show weather even if its old
            mDashboardView.setCurrentWeather(mTodayWeather);
            mDashboardView.setCurrentDate(calculateDate());
            if (mShortTermWeather != null) {
                mDashboardView.setShortTermWeather(mShortTermWeather);
            }
            if (mLongTermWeather != null) {
                mDashboardView.setLongTermWeather(mLongTermWeather);
            }

            mDashboardView.showWeather(true);

            // notify for old data
            long lastUpdateMillis = mWeatherDataSource.getLastUpdate();
            LocalDateTime lastUpdate = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastUpdateMillis),
                DateTimeUtils.toZoneId(TimeZone.getDefault()));
            LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);
            boolean noRecentWeather = lastUpdate.isBefore(threeHoursAgo);
            mDashboardView.showNoRecentWeatherWarning(noRecentWeather);
        }
    }

    @Override
    public void selectLongTermWeather() {
        if (mLongTermWeather != null) {
            mDashboardView.setLongTermWeather(mLongTermWeather);
            mDashboardView.showLongTermWeather();
        }
    }

    @Override
    public void selectShortTermWeather() {
        if (mShortTermWeather != null) {
            mDashboardView.setShortTermWeather(mShortTermWeather);
            mDashboardView.showShortTermWeather();
        }
    }

    @Override
    public void sync() {
        if (!mDeviceUtils.isNetworkConnected()) {
            mDashboardView.showNoInternetConnection();
            return;
        }

        if (!mDeviceUtils.hasALocationProviderEnabled()) {
            mDashboardView.showNoGpsProviderEnabled();
            return;
        }

        mLocationDataSource.getLocation(new LocationDataSource.GetLocationCallback() {
            @Override
            public void onLocationFound(@NonNull Location location) {
                mWeatherSync.sync(location, new IWeatherSync.WeatherSyncCallback() {
                    @Override
                    public void onSuccess() {
                        loadWeather();
                    }

                    @Override
                    public void onError() {
                        mDashboardView.showSyncProblem();
                    }
                });
            }

            @Override
            public void missingPermission() {
                mDashboardView.showMissingLocationPermissions(true);
            }

            @Override
            public void gpsSensorsTurnedOff() {
                mDashboardView.showNoGpsProviderEnabled();
            }

            @Override
            public void noLocationFound() {
                mDashboardView.showNoLocationFound();
            }
        });
    }

    @Override
    public void openCalendarApp() {
        String targetPackage = KatsunaUtils.KATSUNA_CALENDAR_PACKAGE;

        if (mDeviceUtils.isAppInstalled(targetPackage)) {
            mDeviceUtils.openApp(targetPackage);
        } else {
            mDashboardView.showCalendarAppInstallationDialog();
        }
    }

    @Override
    public void setBrightness(int level) {
        mSettingsController.setBrightness(level);
    }

    @Override
    public void loadVolume() {
        int volume = mSettingsController.getVolume();
        mDashboardView.setVolume(volume);
    }

    @Override
    public void setVolume(int level) {
        mSettingsController.setVolume(level);
    }

    @Override
    public void setWifiStatus(boolean status) {
        mSettingsController.setWifiEnabled(status);
    }

    @Override
    public void setDndStatus(boolean status) {
        mSettingsController.setDndMode(status);
    }

    @Override
    public void expandDashboardView(DashboardViewType dashboardViewType) {
        loadSettings();
        switch (dashboardViewType) {
            case SETTINGS:
                mDashboardView.showExtendedCalendar(false);
                expandFullWeather(false);
                mDashboardView.showExtendedSettings(true);
                break;
            case CALENDAR:
                mDashboardView.showExtendedSettings(false);
                expandFullWeather(false);
                mDashboardView.showExtendedCalendar(true);
                break;
            case WEATHER:
                mDashboardView.showExtendedSettings(false);
                mDashboardView.showExtendedCalendar(false);
                expandFullWeather(true);
                break;
        }
    }

    @Override
    public void setGpsProvider() {
        mDeviceUtils.setGpsProviderStatus();
    }

    @Override
    public void launchSettings() {
        mSettingsController.launchSettings();
    }

    private void expandFullWeather(boolean flag) {
        mDashboardView.showDate(flag);
        if (flag && mShortTermWeather != null) {
            mDashboardView.setShortTermWeather(mShortTermWeather);
            mDashboardView.showShortTermWeather();
        }
        mDashboardView.showExtendedWeather(flag);
    }

    @Override
    public void start() {
        // no op yet
    }

    private String calculateDate() {
        Format formatter = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        String date = formatter.format(new Date());
        String location = mTodayWeather == null ? "" : mTodayWeather.getCity();
        return String.format("%s | %s", location, date);
    }
}

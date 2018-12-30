package com.katsuna.launcher.katsuna.dashboard;

import android.location.Location;

import com.katsuna.launcher.katsuna.dashboard.data.LocationDataSource;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.domain.Weather;
import com.katsuna.launcher.katsuna.dashboard.utils.IDeviceUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.IPermissionUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.ISettingsController;
import com.katsuna.launcher.katsuna.dashboard.utils.IWeatherSync;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;

import static com.katsuna.commons.utils.KatsunaUtils.KATSUNA_CALENDAR_PACKAGE;
import static com.katsuna.launcher.katsuna.WeatherConstants.LOCATION_PERMISSIONS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DashboardPresenterTest {

    private DashboardPresenter mPresenter;

    @Mock
    private WeatherDataSource mWeatherDataSource;

    @Mock
    private DashboardContract.View mView;

    @Mock
    private IPermissionUtils mPermissionUtils;

    @Mock
    private LocationDataSource mLocationDatasource;

    @Mock
    private IDeviceUtils mDeviceUtils;

    @Mock
    private ISettingsController mSettingsController;

    @Mock
    private IWeatherSync mWeatherSync;

    @Captor
    private ArgumentCaptor<LocationDataSource.GetLocationCallback> mLocationCallback;

    @Captor
    private ArgumentCaptor<IWeatherSync.WeatherSyncCallback> mWeatherSyncCallback;

    @Before
    public void setupAlarmsPresenter() {

        MockitoAnnotations.initMocks(this);

        mPresenter = new DashboardPresenter(mWeatherDataSource, mView, mPermissionUtils,
            mLocationDatasource, mDeviceUtils, mSettingsController, mWeatherSync);

    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        mPresenter.start();

        // Then the presenter is set to the view
        verify(mView).setPresenter(mPresenter);
    }

    @Test
    public void loadDataWithNoPermissionToWriteSettings_AsksPermissionToWriteSetting() {
        // When there is no permission granted to write settings
        when(mSettingsController.canModifySystemSetting()).thenReturn(false);

        // and we are loading data
        mPresenter.loadData();

        // then view should ask for corresponding permission
        verify(mView).askPermissionToWriteSettings();
    }

    @Test
    public void loadDataWithNoLocationPermissions_AsksLocationPermissions() {
        // When there are no permissions granted to access location
        when(mPermissionUtils.hasPermissions(LOCATION_PERMISSIONS)).thenReturn(false);

        // and we are loading data
        mPresenter.loadData();

        // then view should ask for corresponding permission
        verify(mView).showMissingLocationPermissions(true);
    }

    @Test
    public void loadDataWithPermissionToWriteSettings_ShowsAllSettings() {
        // when permission to write settings is granted
        when(mSettingsController.canModifySystemSetting()).thenReturn(true);

        // and we are loading data
        mPresenter.loadData();

        // then view should load settings
        verify(mView).setBrightnessLevel(mSettingsController.getBrightness());
        verify(mView).showBatteryLevel(mSettingsController.getBatterLevel());
        verify(mView).showWifiStatus(mSettingsController.isWifiEnabled());
        verify(mView).showGpsStatus(mDeviceUtils.hasALocationProviderEnabled());
        verify(mView).showDndStatus(mSettingsController.isDndModeOn());
    }

    @Test
    public void loadDataWithAllPermissionsGrantedButNoWeather_ShowsNoWeatherData() {
        allPermissionsGranted();

        // and no weather data
        when(mWeatherDataSource.getToday()).thenReturn(null);

        // and we are loading data
        mPresenter.loadData();

        // then
        verify(mView).showNoWeatherWarning(true);
    }

    @Test
    public void loadDataWithAllPermissionsGrantedButNoRecentWeather_ShowsNoRecentData() {
        // All permissions are granted
        allPermissionsGranted();

        noRecentWeather();

        // and we are loading data
        mPresenter.loadData();

        // then
        verify(mView).showNoRecentWeatherWarning(true);
    }

    @Test
    public void loadDataWithAllPermissionsGrantedWithRecentWeather_ShowsWeatherData() {
        allPermissionsGranted();

        // and no weather data
        when(mWeatherDataSource.getToday()).thenReturn(new Weather());
        LocalDateTime before4hours = LocalDateTime.now().minusHours(2);
        long before4hoursMillis = before4hours.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli();
        when(mWeatherDataSource.getLastUpdate()).thenReturn(before4hoursMillis);

        // and we are loading data
        mPresenter.loadData();

        // then
        verify(mView).showNoRecentWeatherWarning(false);
    }

    @Test
    public void selectingSettings_ShowsExtendedSettings() {
        allPermissionsGranted();

        // when expanding settings
        mPresenter.expandDashboardView(DashboardViewType.SETTINGS);

        // settings extended view is shown only
        verify(mView).showExtendedSettings(true);
        verify(mView).showExtendedCalendar(false);
        verify(mView).showExtendedWeather(false);
    }

    @Test
    public void launchSettings_callsSettingsController() {
        allPermissionsGranted();

        mPresenter.launchSettings();

        verify(mSettingsController).launchSettings();
    }

    @Test
    public void setGpsProvider_callsDeviceUtilsGpsProvider() {
        allPermissionsGranted();

        mPresenter.setGpsProvider();

        verify(mDeviceUtils).setGpsProviderStatus();
    }

    @Test
    public void setDndMode_callsSettingsController() {
        allPermissionsGranted();

        mPresenter.setDndStatus(false);

        verify(mSettingsController).setDndMode(false);
    }

    @Test
    public void setWifiStatus_callsSettingsController() {
        allPermissionsGranted();

        mPresenter.setWifiStatus(false);

        verify(mSettingsController).setWifiEnabled(false);
    }

    @Test
    public void setVolume_callsSettingsController() {
        allPermissionsGranted();

        mPresenter.setVolume(50);

        verify(mSettingsController).setVolume(50);
    }

    @Test
    public void loadVolume_callsSettingsControllerAndShowsVolume() {
        allPermissionsGranted();

        int expectedVolume = 50;
        when(mSettingsController.getVolume()).thenReturn(expectedVolume);

        mPresenter.loadVolume();

        verify(mSettingsController).getVolume();
        verify(mView).setVolume(expectedVolume);
    }

    @Test
    public void setBrightness_callsSettingsController() {
        allPermissionsGranted();

        int brightnessLevel = 50;
        mPresenter.setBrightness(brightnessLevel);

        verify(mSettingsController).setBrightness(brightnessLevel);
    }

    @Test
    public void selectingCalendar_ShowsExtendedCalendar() {
        allPermissionsGranted();

        // when expanding calendar
        mPresenter.expandDashboardView(DashboardViewType.CALENDAR);

        // then calendar extended view is shown
        verify(mView).showExtendedSettings(false);
        verify(mView).showExtendedWeather(false);
        verify(mView).showExtendedCalendar(true);
    }

    @Test
    public void openCalendarAppWithNoCalendarApp_ShowsInstallationProposal() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // and expanding calendar
        mPresenter.expandDashboardView(DashboardViewType.CALENDAR);

        // while katsuna calendar app is not installed
        when(mDeviceUtils.isAppInstalled(KATSUNA_CALENDAR_PACKAGE)).thenReturn(false);

        // then attempt to open calendar
        mPresenter.openCalendarApp();

        // popus installation suggestion
        verify(mDeviceUtils).isAppInstalled(KATSUNA_CALENDAR_PACKAGE);
        verify(mView).showCalendarAppInstallationDialog();
    }

    @Test
    public void openCalendarAppWithCalendarAppInstall_LaunchesCalendarApp() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // and expanding calendar
        mPresenter.expandDashboardView(DashboardViewType.CALENDAR);

        // while katsuna calendar app is not installed
        when(mDeviceUtils.isAppInstalled(KATSUNA_CALENDAR_PACKAGE)).thenReturn(true);

        // then attempt to open calendar
        mPresenter.openCalendarApp();

        // leads to attempt to open app
        verify(mDeviceUtils).isAppInstalled(KATSUNA_CALENDAR_PACKAGE);
        verify(mDeviceUtils).openApp(KATSUNA_CALENDAR_PACKAGE);
    }

    private void allPermissionsGranted() {
        when(mSettingsController.canModifySystemSetting()).thenReturn(true);
        when(mPermissionUtils.hasPermissions(LOCATION_PERMISSIONS)).thenReturn(true);
    }

    @Test
    public void tryingToSyncWeatherWithNoInternet_ShowsNoInternetWarning() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // and no internet connectivity
        when(mDeviceUtils.isNetworkConnected()).thenReturn(false);

        // and then trying to sync
        mPresenter.sync();

        verify(mView).showNoInternetConnection();
    }

    @Test
    public void tryingToSyncWeatherWithNoGpsProvider_ShowsWarning() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // with internet connectivity
        when(mDeviceUtils.isNetworkConnected()).thenReturn(true);
        // but no gps provider enabled
        when(mDeviceUtils.hasALocationProviderEnabled()).thenReturn(false);

        // and then trying to sync
        mPresenter.sync();

        verify(mView).showNoGpsProviderEnabled();
    }

    @Test
    public void syncWeatherButNoLocationFound_showsWarning() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // with internet connectivity and gps
        when(mDeviceUtils.isNetworkConnected()).thenReturn(true);
        when(mDeviceUtils.hasALocationProviderEnabled()).thenReturn(true);

        // and then trying to sync
        mPresenter.sync();

        verify(mLocationDatasource).getLocation(mLocationCallback.capture());
        mLocationCallback.getValue().noLocationFound();

        verify(mView).showNoLocationFound();
    }

    @Test
    public void syncWeatherWithNoPermission_showsWarning() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // with internet connectivity and gps
        when(mDeviceUtils.isNetworkConnected()).thenReturn(true);
        when(mDeviceUtils.hasALocationProviderEnabled()).thenReturn(true);

        // and then trying to sync
        mPresenter.sync();

        verify(mLocationDatasource).getLocation(mLocationCallback.capture());
        mLocationCallback.getValue().missingPermission();

        verify(mView).showMissingLocationPermissions(true);
    }

    @Test
    public void syncWeatherWithNoGps_showsWarning() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // with internet connectivity and gps
        when(mDeviceUtils.isNetworkConnected()).thenReturn(true);
        when(mDeviceUtils.hasALocationProviderEnabled()).thenReturn(true);

        // and then trying to sync
        mPresenter.sync();

        verify(mLocationDatasource).getLocation(mLocationCallback.capture());
        mLocationCallback.getValue().gpsSensorsTurnedOff();

        verify(mView).showNoGpsProviderEnabled();
    }

    @Test
    public void syncWeatherWithLocationFound_initiatesSyncing() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // with internet connectivity and gps
        when(mDeviceUtils.isNetworkConnected()).thenReturn(true);
        when(mDeviceUtils.hasALocationProviderEnabled()).thenReturn(true);

        // and then trying to sync
        mPresenter.sync();

        verify(mLocationDatasource).getLocation(mLocationCallback.capture());
        Location location = new Location("");
        mLocationCallback.getValue().onLocationFound(location);

        verify(mWeatherSync).sync(eq(location), anyObject());
    }

    @Test
    public void syncWeatherSyncError_showsWarning() {
        allPermissionsGranted();

        Location location = new Location("");

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // with internet connectivity and gps
        when(mDeviceUtils.isNetworkConnected()).thenReturn(true);
        when(mDeviceUtils.hasALocationProviderEnabled()).thenReturn(true);


        // and then trying to sync
        mPresenter.sync();

        verify(mLocationDatasource).getLocation(mLocationCallback.capture());

        mLocationCallback.getValue().onLocationFound(location);

        verify(mWeatherSync).sync(eq(location), mWeatherSyncCallback.capture());

        mWeatherSyncCallback.getValue().onError();
        verify(mView).showSyncProblem();
    }

    @Test
    public void syncWeatherSucceeded_showsWeather() {
        allPermissionsGranted();

        Location location = new Location("");

        // When loading data
        mPresenter.loadData();

        // with no recent weather
        noRecentWeather();

        // with internet connectivity and gps
        when(mDeviceUtils.isNetworkConnected()).thenReturn(true);
        when(mDeviceUtils.hasALocationProviderEnabled()).thenReturn(true);


        // and then trying to sync
        mPresenter.sync();

        verify(mLocationDatasource).getLocation(mLocationCallback.capture());

        mLocationCallback.getValue().onLocationFound(location);

        verify(mWeatherSync).sync(eq(location), mWeatherSyncCallback.capture());

        mWeatherSyncCallback.getValue().onSuccess();
        verify(mView).setCurrentWeather(any());
    }

    @Test
    public void selectingWeather_ShowsExtendedWeather() {
        allPermissionsGranted();

        // When loading data
        mPresenter.loadData();

        // and then expanding weather
        mPresenter.expandDashboardView(DashboardViewType.WEATHER);

        // then calendar extended view is shown
        verify(mView).showExtendedSettings(false);
        verify(mView).showExtendedCalendar(false);
        verify(mView).showExtendedWeather(true);
    }

    @Test
    public void selectingLongTermWeather_ShowsLongTermWeather() {
        allPermissionsGranted();

        // When loading data
        List<Weather> weatherList = new ArrayList<>();
        when(mWeatherDataSource.getLongTerm()).thenReturn(weatherList);
        mPresenter.loadData();

        // and then expanding weather
        mPresenter.expandDashboardView(DashboardViewType.WEATHER);

        mPresenter.selectLongTermWeather();

        // then long term weather is set and presented
        verify(mView).setLongTermWeather(weatherList);
        verify(mView).showLongTermWeather();
    }

    @Test
    public void selectingShortTermWeather_ShowsShortTermWeather() {
        allPermissionsGranted();

        // When loading data
        List<Weather> weatherList = new ArrayList<>();
        when(mWeatherDataSource.getShortTerm()).thenReturn(weatherList);
        mPresenter.loadData();

        // and then expanding weather
        mPresenter.expandDashboardView(DashboardViewType.WEATHER);

        mPresenter.selectShortTermWeather();

        // then short term weather is set and presented
        verify(mView, times(2)).setShortTermWeather(weatherList);
        verify(mView, times(2)).showShortTermWeather();
    }


    private void noRecentWeather() {
        // and no weather data
        when(mWeatherDataSource.getToday()).thenReturn(new Weather());
        LocalDateTime before4hours = LocalDateTime.now().minusHours(4);
        long before4hoursMillis = before4hours.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli();
        when(mWeatherDataSource.getLastUpdate()).thenReturn(before4hoursMillis);
    }

}

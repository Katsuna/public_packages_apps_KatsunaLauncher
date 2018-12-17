package com.katsuna.launcher.katsuna.dashboard.utils;

public interface IDeviceUtils {

    boolean isNetworkConnected();

    boolean isAppInstalled(String packageName);

    void openApp(String packageName);

    boolean hasALocationProviderEnabled();

    boolean isLocationGpsProviderEnabled();

    boolean isLocationNetworkProviderEnabled();

}

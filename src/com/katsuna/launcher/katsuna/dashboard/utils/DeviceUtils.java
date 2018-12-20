package com.katsuna.launcher.katsuna.dashboard.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import static android.content.Context.LOCATION_SERVICE;

public class DeviceUtils implements IDeviceUtils {

    private final Context mContext;
    private LocationManager mLocationManager;

    public DeviceUtils(Context context) {
        mContext = context;
    }

    @Override
    public boolean isNetworkConnected() {
        ConnectivityManager cm =
            (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            return cm.getActiveNetworkInfo() != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean isAppInstalled(String packageName) {
        PackageManager manager = mContext.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        return i != null;
    }

    @Override
    public void openApp(String packageName) {
        PackageManager manager = mContext.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i != null) {
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            mContext.startActivity(i);
        }
    }

    @Override
    public boolean hasALocationProviderEnabled() {
        return isLocationGpsProviderEnabled() || isLocationNetworkProviderEnabled();
    }

    @Override
    public boolean isLocationGpsProviderEnabled() {
        return getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public boolean isLocationNetworkProviderEnabled() {
        return getLocationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void setGpsProviderStatus() {
        Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        mContext.startActivity(i);
    }

    private LocationManager getLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        }
        return mLocationManager;
    }


}



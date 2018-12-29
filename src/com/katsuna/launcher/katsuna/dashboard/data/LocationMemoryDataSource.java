package com.katsuna.launcher.katsuna.dashboard.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import org.threeten.bp.LocalDateTime;

import timber.log.Timber;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class LocationMemoryDataSource implements LocationDataSource {

    private static final String TAG = LocationMemoryDataSource.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static final LocationMemoryDataSource mInstance = new LocationMemoryDataSource();
    private static final long SECONDS_TO_WAIT = 1000 * 5;
    private Context mContext;
    private LocationManager mLocationManager;
    private Location mLastLocation;
    private LocalDateTime mLastLocationTime;
    private String mSelectedProvider;

    private LocationMemoryDataSource() {
    }

    public static LocationMemoryDataSource getInstance() {
        return mInstance;
    }

    public LocationDataSource init(Context context) {
        mContext = context.getApplicationContext();
        mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        return this;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void getLocation(@NonNull final GetLocationCallback callback) {
        Timber.tag(TAG).d("getLocation ...");

        // get cached location
        if (mLastLocationTime != null &&
            mLastLocationTime.isAfter(LocalDateTime.now().minusMinutes(30))) {
            callback.onLocationFound(mLastLocation);
            Timber.tag(TAG).d("return recently found location ... %s", mLastLocation);
            return;
        }

        // check for missing permissions
        if (noGpsPermissionGranted()) {
            callback.missingPermission();
            return;
        }

        // check for enabled gps provider
        if (useNetworkForLocation()) {
            mSelectedProvider = NETWORK_PROVIDER;
        } else if (useGpsSensor()) {
            mSelectedProvider = GPS_PROVIDER;
        } else {
            callback.gpsSensorsTurnedOff();
            return;
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Timber.tag(TAG).d("onLocationChanged %s", location);

                mLastLocation = location;
                mLastLocationTime = LocalDateTime.now();
                stopListeningForUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Timber.tag(TAG).d("onStatusChanged %s %d", provider, status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Timber.tag(TAG).d("onProviderEnabled %s", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Timber.tag(TAG).d("onProviderDisabled %s", provider);
                callback.gpsSensorsTurnedOff();
            }
        };

        mLocationManager.requestLocationUpdates(mSelectedProvider, 0, 0, locationListener);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            Location location = mLocationManager.getLastKnownLocation(mSelectedProvider);
            if (location == null) {
                callback.noLocationFound();
            } else {
                callback.onLocationFound(mLastLocation);
            }
        }, SECONDS_TO_WAIT);
    }

    private void stopListeningForUpdates(LocationListener locationListener) {
        try {
            mLocationManager.removeUpdates(locationListener);
        } catch (SecurityException e) {
            Timber.tag(TAG).e(e, "Error while trying to stop listening for location updates.");
        }
    }

    private boolean noGpsPermissionGranted() {
        return !(fineGpsPermissionGranted() || coarseGpsPermissionGranted());
    }

    private boolean fineGpsPermissionGranted() {
        return ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION)
            == PERMISSION_GRANTED;
    }

    private boolean coarseGpsPermissionGranted() {
        return ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION)
            == PERMISSION_GRANTED;
    }

    private boolean useGpsSensor() {
        return mLocationManager.isProviderEnabled(GPS_PROVIDER);
    }

    private boolean useNetworkForLocation() {
        return mLocationManager.isProviderEnabled(NETWORK_PROVIDER);
    }
}

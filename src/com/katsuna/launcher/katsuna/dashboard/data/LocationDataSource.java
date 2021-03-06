package com.katsuna.launcher.katsuna.dashboard.data;

import android.location.Location;
import android.support.annotation.NonNull;

public interface LocationDataSource {

    void getLocation(@NonNull GetLocationCallback callback);

    interface GetLocationCallback {
        void onLocationFound(@NonNull Location location);

        void missingPermission();

        void gpsSensorsTurnedOff();

        void noLocationFound();
    }
}

package com.katsuna.launcher.katsuna.dashboard.services;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.katsuna.launcher.katsuna.dashboard.data.LocationDataSource;
import com.katsuna.launcher.katsuna.dashboard.data.LocationMemoryDataSource;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.factories.WeatherDataSourceFactory;
import com.katsuna.launcher.katsuna.dashboard.tasks.LongTermWeatherTask;
import com.katsuna.launcher.katsuna.dashboard.tasks.ShortTermWeatherTask;
import com.katsuna.launcher.katsuna.dashboard.tasks.WeatherTask;

import timber.log.Timber;

import static com.katsuna.launcher.katsuna.dashboard.utils.WeatherJobScheduler.JOB_CURRENT_ID;
import static com.katsuna.launcher.katsuna.dashboard.utils.WeatherJobScheduler.JOB_LONG_ID;
import static com.katsuna.launcher.katsuna.dashboard.utils.WeatherJobScheduler.JOB_SHORT_ID;

public class WeatherJobService extends JobService {

    private static final String TAG = WeatherJobService.class.getSimpleName();

    private WeatherDataSource mWeatherDataSource;

    @Override
    public void onCreate() {
        super.onCreate();
        mWeatherDataSource = WeatherDataSourceFactory.getDataSource(this);
    }

    @Override
    @SuppressLint("MissingPermission")
    public boolean onStartJob(final JobParameters params) {
        Timber.tag(TAG).d("startjon %d", params.getJobId());

        LocationDataSource locationDataSource = LocationMemoryDataSource.getInstance().init(this);
        locationDataSource.getLocation(new LocationDataSource.GetLocationCallback() {
            @Override
            public void onLocationFound(@NonNull Location location) {
                Timber.tag(TAG).d("onLocationFound location= %s", location);

                // fetch weather with corresponding async task
                if (params.getJobId() == JOB_CURRENT_ID) {
                    new WeatherTask(getApplicationContext(), location, mWeatherDataSource,
                        (response) -> {
                            Timber.tag(TAG).d("onFinish called");
                            jobFinished(params, false);
                        })
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else if (params.getJobId() == JOB_SHORT_ID) {
                    new ShortTermWeatherTask(getApplicationContext(), location,
                        mWeatherDataSource,
                        (response) -> {
                            Timber.tag(TAG).d("onFinish called");
                            jobFinished(params, false);
                        })
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (params.getJobId() == JOB_LONG_ID) {
                    new LongTermWeatherTask(getApplicationContext(), location,
                        mWeatherDataSource,
                        (response) -> {
                            Timber.tag(TAG).d("onFinish called");
                            jobFinished(params, false);
                        })
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void missingPermission() {
                Timber.tag(TAG).d("missingPermissions for location");
            }

            @Override
            public void gpsSensorsTurnedOff() {
                Timber.tag(TAG).d("gps sensors turned off");
            }

            @Override
            public void noLocationFound() {
                Timber.tag(TAG).d("no location found");
            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // dont retry failed job
        return false;
    }
}

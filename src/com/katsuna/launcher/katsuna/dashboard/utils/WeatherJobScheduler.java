package com.katsuna.launcher.katsuna.dashboard.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.katsuna.launcher.katsuna.dashboard.services.WeatherJobService;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class WeatherJobScheduler {

    public static final int JOB_CURRENT_ID = 1;
    public static final int JOB_SHORT_ID = 2;
    public static final int JOB_LONG_ID = 3;
    private static final String TAG = WeatherJobScheduler.class.getSimpleName();
    private static final int ONE_MIN = 60 * 1000;
    private static final int ONE_HOUR = 60 * ONE_MIN;

    private final Context ctx;

    public WeatherJobScheduler(Context context) {
        ctx = context;
    }

    public void schedule() {

        final ComponentName component = new ComponentName(ctx, WeatherJobService.class);

        final JobScheduler scheduler = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        Timber.tag(TAG).d("scheduling if needed");

        if (scheduler.getPendingJob(JOB_CURRENT_ID) == null) {
            Timber.d("scheduling current job: %d", JOB_CURRENT_ID);
            JobInfo currentWeatherJob = getCurrentWeatherJob(component);
            scheduler.schedule(currentWeatherJob);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (scheduler.getPendingJob(JOB_SHORT_ID) == null) {
                    Timber.tag(TAG).d("scheduling short job: %d", JOB_SHORT_ID);
                    JobInfo shortTermWeatherJob = getShortTermWeatherJob(component);
                    scheduler.schedule(shortTermWeatherJob);
                }
            }
        }, 5000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (scheduler.getPendingJob(JOB_LONG_ID) == null) {
                    Timber.tag(TAG).d("scheduling long job: %d", JOB_LONG_ID);
                    JobInfo longTermWeatherJob = getLongTermWeatherJob(component);
                    scheduler.schedule(longTermWeatherJob);
                }
            }
        }, 10000);
    }

    private JobInfo getCurrentWeatherJob(ComponentName component) {
        return new JobInfo.Builder(JOB_CURRENT_ID, component)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(15 * ONE_MIN)
            .setPersisted(true)
            .build();
    }

    private JobInfo getShortTermWeatherJob(ComponentName component) {
        return new JobInfo.Builder(JOB_SHORT_ID, component)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(3 * ONE_HOUR)
            .setPersisted(true)
            .build();
    }

    private JobInfo getLongTermWeatherJob(ComponentName component) {
        return new JobInfo.Builder(JOB_LONG_ID, component)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(20 * ONE_HOUR)
            .setPersisted(true)
            .build();
    }

}

package com.katsuna.launcher.katsuna.dashboard.tasks;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.katsuna.launcher.katsuna.WeatherConstants;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.utils.DownloadResponse;
import com.katsuna.launcher.katsuna.utils.DownloadUtils;

import java.lang.ref.WeakReference;
import java.util.Locale;

import timber.log.Timber;

abstract class WeatherTaskBase extends AsyncTask<String, String, DownloadResponse> {

    final WeatherDataSource mDataSource;
    final String mLatitude;
    final String mLongitude;
    private final IAsyncTaskFinished mAsyncTaskFinished;
    String TAG;
    String mLanguage;
    String mUrl;
    String mIntentAction;
    private WeakReference<Context> mContextWeakReference;

    WeatherTaskBase(Context context, Location location, WeatherDataSource dataSource,
                    IAsyncTaskFinished asyncTaskFinished) {
        if (context != null) {
            mContextWeakReference = new WeakReference<>(context);
        }
        mLatitude = String.valueOf(location.getLatitude());
        mLongitude = String.valueOf(location.getLongitude());
        mDataSource = dataSource;
        mAsyncTaskFinished = asyncTaskFinished;
        mLanguage = Locale.getDefault().getLanguage();
        if (mLanguage.equals("cs")) {
            mLanguage = "cz";
        }
    }

    @Override
    protected DownloadResponse doInBackground(String... params) {
        Timber.tag(TAG).d("From api day forecast inside alarm with lat: %s long: %s", mLatitude,
            mLongitude);

        DownloadResponse response = DownloadUtils.getUrl(mUrl);
        if (response.allGood()) {
            saveResponse(response.toString());
            mDataSource.saveLastUpdateTime();
        } else {
            Timber.tag(TAG).e("Problem fetching url: %s problems: %s", mUrl, response.problems());
        }

        return response;
    }

    @Override
    protected void onPostExecute(DownloadResponse response) {
        if (mContextWeakReference == null) {
            mAsyncTaskFinished.onFinish(response);
            return;
        }

        Context ctx = mContextWeakReference.get();
        Timber.d("Broadcasting message");
        Intent intent = new Intent(mIntentAction);
        // You can also include some extra data.
        intent.putExtra(WeatherConstants.DOWNLOAD_RESPONSE, response);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);

        mAsyncTaskFinished.onFinish(response);

    }

    abstract void saveResponse(String response);
}

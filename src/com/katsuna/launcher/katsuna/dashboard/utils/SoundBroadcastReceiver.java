package com.katsuna.launcher.katsuna.dashboard.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import timber.log.Timber;

public abstract class SoundBroadcastReceiver extends BroadcastReceiver {

    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final String STREAM_DEVICES_CHANGED_ACTION =
        "android.media.STREAM_DEVICES_CHANGED_ACTION";
    private static final String RINGER_MODE_CHANGED_ACTION = "android.media.RINGER_MODE_CHANGED";
    private static final String INTERNAL_RINGER_MODE_CHANGED_ACTION =
        "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION";
    private static final String STREAM_MUTE_CHANGED_ACTION =
        "android.media.STREAM_MUTE_CHANGED_ACTION";
    private static final String ACTION_EFFECTS_SUPPRESSOR_CHANGED =
        "android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED";

    private final Context mContext;

    public SoundBroadcastReceiver(Context context) {
        mContext = context;
    }

    public void setListening(boolean listening) {
        if (listening) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(VOLUME_CHANGED_ACTION);
            filter.addAction(STREAM_DEVICES_CHANGED_ACTION);
            filter.addAction(RINGER_MODE_CHANGED_ACTION);
            filter.addAction(INTERNAL_RINGER_MODE_CHANGED_ACTION);
            filter.addAction(STREAM_MUTE_CHANGED_ACTION);
            filter.addAction(ACTION_EFFECTS_SUPPRESSOR_CHANGED);
            mContext.registerReceiver(this, filter);
        } else {
            try {
                mContext.unregisterReceiver(this);
            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onBroadcastReceived();
    }

    protected abstract void onBroadcastReceived();

}
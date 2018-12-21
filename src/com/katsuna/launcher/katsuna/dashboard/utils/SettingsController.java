package com.katsuna.launcher.katsuna.dashboard.utils;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.katsuna.commons.KatsunaIntents;
import com.katsuna.commons.utils.KatsunaUtils;

import timber.log.Timber;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class SettingsController implements ISettingsController {

    private final static int VOLUME_FACTOR = 10;

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private AudioManager mAudioManager;
    private WifiManager mWifiManager;
    private PowerManager mPowerManager;
    private NotificationManager mNotificationManager;
    private BatteryManager mBatteryManager;
    private TelephonyManager mTelephoneManager;

    public SettingsController(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    @Override
    public int getBrightness() {
        int output = 0;

        try {
            output = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Timber.e(e, "Cannot access system brightness");
        }

        return output;
    }

    @Override
    public void setBrightness(int value) {
        if (canModifySystemSetting()) {
            Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, value);
        }
    }

    @Override
    public int getVolume() {
        return getAudioManager().getStreamVolume(AudioManager.STREAM_RING) * VOLUME_FACTOR;
    }

    @Override
    public void setVolume(int value) {
        int index = Math.round(value / VOLUME_FACTOR);
        Timber.e("index= %d", index);
        if (index != 0) {
            getAudioManager().setStreamVolume(AudioManager.STREAM_RING, index, 0);
        } else {
            getAudioManager().setStreamVolume(AudioManager.STREAM_RING, 1, 0);
        }
    }

    @Override
    public int getBatterLevel() {
        return getBatteryManager().getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    @Override
    public boolean isBatterySaverOn() {
        return getPowerManager().isPowerSaveMode();
    }

    @Override
    public boolean isWifiEnabled() {
        return getWifiManager().isWifiEnabled();
    }

    @Override
    public void setWifiEnabled(boolean enabled) {
        try {
            getWifiManager().setWifiEnabled(enabled);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    @Override
    public boolean isDataEnabled() {
        boolean output = false;
        TelephonyManager tm = getTelephonyManager();
        if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            output = Settings.Global.getInt(mContentResolver, "mobile_data", 1) == 1;
        }
        return output;
    }

    @Override
    public boolean isDndModeOn() {
        try {
            int zenMode = Settings.Global.getInt(mContentResolver, "zen_mode");
            return zenMode > 0;
        } catch (Settings.SettingNotFoundException e) {
            Timber.e(e);
        }
        return false;
    }

    @Override
    public void setDndMode(boolean enabled) {
        // Check if the notification policy access has been granted for the app.
        NotificationManager manager = getNotificationManager();
        if (manager.isNotificationPolicyAccessGranted()) {
            if (enabled) {
                manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
            } else {
                manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        } else {
            // permission needed
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            mContext.startActivity(intent);
        }
    }

    @Override
    public boolean canModifySystemSetting() {
        return Settings.System.canWrite(mContext);
    }

    @Override
    public void launchSettings() {
        if (KatsunaUtils.katsunaOsDetected()) {
            Intent i = new Intent(KatsunaIntents.SETTINGS);
            mContext.startActivity(i);
        } else {
            Intent i = new Intent(Settings.ACTION_SETTINGS);
            mContext.startActivity(i);
        }
    }

    private AudioManager getAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        }

        return mAudioManager;
    }

    private PowerManager getPowerManager() {
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        }

        return mPowerManager;
    }

    private WifiManager getWifiManager() {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        }

        return mWifiManager;
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        }

        return mNotificationManager;
    }

    private BatteryManager getBatteryManager() {
        if (mBatteryManager == null) {
            mBatteryManager = (BatteryManager) mContext.getSystemService(BATTERY_SERVICE);
        }

        return mBatteryManager;
    }

    private TelephonyManager getTelephonyManager() {
        if (mTelephoneManager == null) {
            mTelephoneManager = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
        }

        return mTelephoneManager;
    }
}

package com.katsuna.launcher.katsuna.dashboard.utils;

public interface ISettingsController {

    int getBrightness();

    void setBrightness(int value);

    int getVolume();

    void setVolume(int value);

    int getBatterLevel();

    boolean isBatterySaverOn();

    boolean isWifiEnabled();

    void setWifiEnabled(boolean enabled);

    boolean isDataEnabled();

    boolean isDndModeOn();

    void setDndMode(boolean enabled);

    boolean canModifySystemSetting();

    void launchSettings();
}

package com.katsuna.launcher.katsuna.settings.callbacks;

import com.katsuna.commons.entities.UserProfile;
import com.katsuna.launcher.katsuna.settings.BaseSetting;

public interface SettingsCallback {
    boolean isReading();

    UserProfile getUserProfile();

    void focusOnSetting(BaseSetting setting);

    void loadProfile();

    void showTransparency(boolean enabled);
}

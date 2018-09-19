package com.katsuna.launcher.katsuna;

import com.katsuna.commons.entities.UserProfile;

public interface AppInteraction {

    void selectAppsGroup(int position);

    void deselectAppsGroup();

    UserProfile getUserProfile();

    void uninstall(String packageName);
}

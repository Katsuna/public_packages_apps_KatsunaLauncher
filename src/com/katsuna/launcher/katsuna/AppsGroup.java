package com.katsuna.launcher.katsuna;

import com.katsuna.launcher.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppsGroup {

    public List<AppInfo> apps;
    public boolean premium;
    public String firstLetter;

    public AppsGroup() {
        apps = new ArrayList<>();
    }

}

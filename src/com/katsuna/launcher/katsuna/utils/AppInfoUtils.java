package com.katsuna.launcher.katsuna.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.katsuna.launcher.katsuna.interfaces.IAppInfoUtils;

import java.util.LinkedHashMap;

public class AppInfoUtils implements IAppInfoUtils {

    private static final AppInfoUtils instance = new AppInfoUtils();

    private final LinkedHashMap<String, ApplicationInfo> map;

    private AppInfoUtils() {
        map = new LinkedHashMap<>();
    }

    public static AppInfoUtils getInstance() {
        return instance;
    }

    @Override
    public boolean isUserApp(ApplicationInfo appInfo) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (appInfo.flags & mask) == 0;
    }

    @Override
    public boolean isUserApp(Context ctx, String packageName) {
        ApplicationInfo appInfo;
        if (map.containsKey(packageName)) {
            appInfo = map.get(packageName);
        } else {
            try {
                appInfo = ctx.getPackageManager().getApplicationInfo(packageName, 0);
                map.put(packageName, appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
        return isUserApp(appInfo);
    }
}

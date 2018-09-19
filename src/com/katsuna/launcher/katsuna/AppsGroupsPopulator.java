package com.katsuna.launcher.katsuna;

import android.content.Context;

import com.katsuna.launcher.AppInfo;
import com.katsuna.launcher.katsuna.interfaces.LauncherStatsProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class AppsGroupsPopulator {

    private final List<AppInfo> mAppInfos;
    private final Context mContext;
    private final LauncherStatsProvider mLauncherStatsProvider;

    public AppsGroupsPopulator(Context context, LauncherStatsProvider launcherStatsProvider,
                               List<AppInfo> appInfos) {
        mContext = context;
        mLauncherStatsProvider = launcherStatsProvider;
        mAppInfos = appInfos;
    }

    public List<AppsGroup> populate(boolean filtered) {
        List<AppsGroup> appsGroups = new ArrayList<>();
        if (!filtered) {
            appsGroups.add(getTopAppsGroup());
        }
        appsGroups.addAll(getAppsGroups());
        return appsGroups;
    }

    private List<AppsGroup> getAppsGroups() {

        LinkedHashMap<String, AppsGroup> map = new LinkedHashMap<>();

        for (AppInfo appInfo : mAppInfos) {
            String firstLetterNormalized = appInfo.getStartLetterNormalized();

            if (map.containsKey(firstLetterNormalized)) {
                map.get(firstLetterNormalized).apps.add(appInfo);
            } else {
                AppsGroup appsGroup = new AppsGroup();
                appsGroup.firstLetter = firstLetterNormalized;
                appsGroup.apps.add(appInfo);
                map.put(firstLetterNormalized, appsGroup);
            }
        }

        return new ArrayList<>(map.values());
    }

    private AppsGroup getTopAppsGroup() {
        calcLauncCounts();

        AppsGroup appsGroup = new AppsGroup();
        appsGroup.premium = true;
        appsGroup.apps = filterTopApps();

        return appsGroup;
    }

    private void calcLauncCounts() {
        HashMap<String, Integer> launchCountMap = mLauncherStatsProvider.getLauncherStats();
        if (launchCountMap != null) {
            for (AppInfo app : mAppInfos) {
                String key = IntentKeyCalculator.getIntentKey(app.getIntent());
                if (launchCountMap.containsKey(key)) {
                    app.setLaunchCount(launchCountMap.get(key));
                } else {
                    app.setLaunchCount(0);
                }
            }
        }
    }

    private List<AppInfo> filterTopApps() {
        List<AppInfo> output = new ArrayList<>();
        List<String> hotSeatApps = LauncherUtils.getHotSeatApps(mContext);
        int appsFound = 0;
        for (AppInfo appInfo : mAppInfos) {
            // exclude hot seat apps and Settings app
            if (hotSeatApps.contains(appInfo.componentName.toShortString())
                    || isSettingsApp(appInfo)) {
                continue;
            }
            appsFound++;
            output.add(appInfo);
            if (appsFound == 3) {
                break;
            }
        }

        Collections.sort(output, new Comparator<AppInfo>(){
            public int compare(AppInfo o1, AppInfo o2){
                if(o1.getLaunchCount().equals(o2.getLaunchCount()))
                    return 0;
                return o1.getLaunchCount() > o2.getLaunchCount() ? -1 : 1;
            }
        });

        return output;
    }

    private boolean isSettingsApp(AppInfo appInfo) {
        return appInfo.componentName.getPackageName().equals("com.android.settings");
    }
}

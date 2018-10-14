package com.katsuna.launcher.katsuna.interfaces;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public interface IAppInfoUtils {

    boolean isUserApp(ApplicationInfo appInfo);

    boolean isUserApp(Context ctx, String packageName);
}

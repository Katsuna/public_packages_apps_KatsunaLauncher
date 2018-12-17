package com.katsuna.launcher.katsuna.dashboard.utils;

import android.content.Context;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionUtils implements IPermissionUtils {

    private final Context mContext;

    public PermissionUtils(Context context) {
        mContext = context;
    }

    @Override
    public boolean hasPermissions(String... permissions) {
        if (mContext != null && permissions != null) {
            for (String p : permissions) {
                if (mContext.checkSelfPermission(p) != PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}

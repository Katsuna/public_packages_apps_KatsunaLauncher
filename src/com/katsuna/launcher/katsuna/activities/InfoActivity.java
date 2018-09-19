package com.katsuna.launcher.katsuna.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.katsuna.commons.ui.KatsunaInfoActivity;
import com.katsuna.launcher.R;

public class InfoActivity extends KatsunaInfoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initControls();
    }

    private void initControls() {
        initToolbar();

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mAppIcon.setImageResource(R.mipmap.ic_launcher);
            mAppName.setText(R.string.app_name);

            String version = getString(R.string.common_version);
            mAppVersion.setText(version + " " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

    }
}

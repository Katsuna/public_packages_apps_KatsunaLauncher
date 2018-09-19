package com.katsuna.launcher.katsuna.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.katsuna.commons.controls.KatsunaNavigationView;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.ui.KatsunaActivity;
import com.katsuna.commons.utils.BrowserUtils;
import com.katsuna.commons.utils.ColorAdjuster;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.launcher.R;

import static com.katsuna.commons.utils.Constants.KATSUNA_PRIVACY_URL;
import static com.katsuna.commons.utils.Constants.KATSUNA_TERMS_OF_USE;

public class SetupActivity extends KatsunaActivity {
    private static final String TAG = "SetupActivity";
    private DrawerLayout mDrawerLayout;
    private Button mSetLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_activity);


        initControls();
    }

    @Override
    protected void onResume() {
        super.onResume();

        applyProfiles();
    }

    private void applyProfiles() {
        UserProfile profile = mUserProfileContainer.getActiveUserProfile();

        ViewGroup topViewGroup = findViewById(android.R.id.content);
        SizeAdjuster.applySizeProfile(this, topViewGroup, profile.opticalSizeProfile);

        ColorAdjuster.adjustPrimaryButton(this, profile.colorProfile, mSetLauncher);
    }

    private void initControls() {
        initToolbar();

        initDrawer();

        mSetLauncher = findViewById(R.id.set_launcher);
        mSetLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPreferredLauncherAndOpenChooser();
            }
        });

        // calculate version
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            String version = getString(R.string.common_version_info, pInfo.versionName);
            TextView appVersion = findViewById(R.id.app_version);
            appVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void resetPreferredLauncherAndOpenChooser() {
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, FakeLauncherActivity.class);
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.common_navigation_drawer_open,
                R.string.common_navigation_drawer_close);
        assert mDrawerLayout != null;
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupDrawerLayout();
    }

    private void setupDrawerLayout() {
        KatsunaNavigationView view = findViewById(R.id.katsuna_navigation_view);
        assert view != null;
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                mDrawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.drawer_info:
                        startActivity(new Intent(SetupActivity.this, InfoActivity.class));
                        break;
                    case R.id.drawer_privacy:
                        BrowserUtils.openUrl(SetupActivity.this, KATSUNA_PRIVACY_URL);
                        break;
                    case R.id.drawer_terms:
                        BrowserUtils.openUrl(SetupActivity.this, KATSUNA_TERMS_OF_USE);
                        break;
                }

                return true;
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
            }
        });
    }


    @Override
    protected void showPopup(boolean flag) {
        // no op here
    }
}
package com.katsuna.launcher.katsuna.activities;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.katsuna.commons.entities.KatsunaApp;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.ui.KatsunaActivity;
import com.katsuna.commons.utils.DeviceUtils;
import com.katsuna.commons.utils.KatsunaUtils;
import com.katsuna.commons.utils.Log;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.IUserProfileProvider;
import com.katsuna.launcher.katsuna.adapters.RecyclerAdsAdapter;
import com.katsuna.launcher.katsuna.ads.AdListEntry;
import com.katsuna.launcher.katsuna.ads.AdListEntryType;

import java.util.ArrayList;
import java.util.List;

import static com.katsuna.launcher.katsuna.AdsConstants.FB_PLACEMENT_ID_1;

public class AdsActivity extends KatsunaActivity implements NativeAdsManager.Listener,
        IUserProfileProvider {
    private static final String TAG = "AdsActivity";
    private final int adsNumber = 10;
    private final List<AdListEntry> adListEntries = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private NativeAdsManager mAds;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ads_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initControls();
        loadAds();
        applyProfiles();

        /* Disabled
        // register connectivity receiver to load facebook and admob ads when internet is available
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ServiceManager.isNetworkAvailable(AdsActivity.this)) {
                    loadAds();
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister connectivity receiver
        //unregisterReceiver(mReceiver);
    }

    private void applyProfiles() {
        UserProfile profile = mUserProfileContainer.getActiveUserProfile();

        ViewGroup topViewGroup = findViewById(android.R.id.content);
        SizeAdjuster.applySizeProfile(this, topViewGroup, profile.opticalSizeProfile);
    }

    private void initControls() {
        initToolbar();

        // disable back arrow
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        mRecyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adListEntries.clear();
    }

    @Override
    protected void showPopup(boolean flag) {
        // no op here
    }

    private void loadFacebookNativeAds() {
        mAds = new NativeAdsManager(this, FB_PLACEMENT_ID_1, adsNumber);
        mAds.setListener(this);

        mAds.loadAds();
    }

    @Override
    public void onAdsLoaded() {
        NativeAd ad = mAds.nextNativeAd();
        int i = 0;
        boolean facebookNativeAdAdded = false;
        List<AdListEntry> adListEntriesToAdd = new ArrayList<>();
        int uniqueAds = mAds.getUniqueNativeAdCount();
        Log.d(TAG, "uniqueAds=" + uniqueAds);
        while (ad != null && i++ <  uniqueAds && i < adsNumber) {
            facebookNativeAdAdded = true;
            AdListEntry adListEntry = new AdListEntry();
            adListEntry.facebookNativeAd = ad;
            adListEntry.adListEntryType = AdListEntryType.FACEBOOK_NATIVE;
            adListEntriesToAdd.add(adListEntry);
            ad = mAds.nextNativeAd();
        }

        if (facebookNativeAdAdded) {
            AdListEntry suggestionText = new AdListEntry();
            suggestionText.text = getResources().getString(R.string.suggested_apps_desc);
            suggestionText.adListEntryType = AdListEntryType.TEXT;
            adListEntries.add(suggestionText);

            adListEntries.addAll(adListEntriesToAdd);
        }

        // last step
        bindAdListEntries();
    }

    @Override
    public void onAdError(AdError adError) {
        Log.d(TAG, "Facebook AdError: " + adError);
        // No ads from facebook. Let's try admob...

        loadAdmobAds();
    }

    private void loadAdmobAds() {
        // check if we have already an entry for admob
        // this can happen because facebook onAdError may be called more than once
        boolean admobEntryFound = false;
        for (AdListEntry adListEntry : adListEntries) {
            if (adListEntry.adListEntryType == AdListEntryType.ADMOB_BANNER) {
                admobEntryFound = true;
                break;
            }
        }

        if (!admobEntryFound) {
            AdListEntry adListEntry = new AdListEntry();
            adListEntry.adListEntryType = AdListEntryType.ADMOB_BANNER;
            adListEntry.admobAds = 1;
            adListEntries.add(adListEntry);

            // last step
            bindAdListEntries();
        }
    }

    private void loadAds() {
        adListEntries.clear();

        // Load katsuna suggested apps
        List<KatsunaApp> kApps = getSuggestedKatsunaApps();
        for (KatsunaApp kApp : kApps) {
            AdListEntry adListEntry = new AdListEntry();
            adListEntry.katsunaApp = kApp;
            adListEntry.adListEntryType = AdListEntryType.KATSUNA_APP;
            adListEntries.add(adListEntry);
        }

        // Add spacer if we have katsuna apps
        if (adListEntries.size() > 0) {
            AdListEntry adListEntry = new AdListEntry();
            adListEntry.adListEntryType = AdListEntryType.SPACER;
            adListEntries.add(adListEntry);
        } else {
            finish();
        }

        bindAdListEntries();

        /*
        if (ServiceManager.isNetworkAvailable(this)) {
            // Load Facebook native ads
            loadFacebookNativeAds();
        } else {
            AdListEntry suggestionText = new AdListEntry();
            suggestionText.text = getResources().getString(R.string.missing_internet_for_suggestions);
            suggestionText.adListEntryType = AdListEntryType.TEXT;
            adListEntries.add(suggestionText);

            bindAdListEntries();
        }
        */
    }

    private void bindAdListEntries() {
        RecyclerAdsAdapter mAdapter = new RecyclerAdsAdapter(this, adListEntries, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private List<KatsunaApp> getSuggestedKatsunaApps() {
        List<KatsunaApp> output = new ArrayList<>();
        List<KatsunaApp> katsunaApps = KatsunaUtils.getKatsunaApps(this);
        for (KatsunaApp app : katsunaApps) {
            if (!DeviceUtils.isPackageInstalled(this, app.packageName)) {
                output.add(app);
            }
        }
        return output;
    }

    @Override
    public UserProfile getUserProfile() {
        return mUserProfileContainer.getActiveUserProfile();
    }
}

package com.katsuna.launcher.katsuna;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.katsuna.commons.entities.OpticalParams;
import com.katsuna.commons.entities.SizeProfileKey;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.Constants;
import com.katsuna.commons.utils.ProfileReader;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.commons.utils.SizeCalc;
import com.katsuna.commons.utils.ViewUtils;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.settings.AgeSetting;
import com.katsuna.launcher.katsuna.settings.BaseSetting;
import com.katsuna.launcher.katsuna.settings.ColorSetting;
import com.katsuna.launcher.katsuna.settings.GenderSetting;
import com.katsuna.launcher.katsuna.settings.HandSetting;
import com.katsuna.launcher.katsuna.settings.SizeSetting;
import com.katsuna.launcher.katsuna.settings.callbacks.SettingsCallback;

public class UsabilitySettingsActivity extends Activity implements SettingsCallback {

    private static final String TAG = "UsabilitySettingsAct";
    private AgeSetting mAgeSetting;
    private GenderSetting mGenderSetting;
    private HandSetting mHandSetting;
    private SizeSetting mSizeSetting;
    private ColorSetting mColorSetting;
    private UserProfile mProfile;
    private boolean mIsReading;
    private long mLastSelectionTimestamp;
    private ScrollView mScrollViewContainer;
    private RelativeLayout mSettingsContainer;
    private Handler mDeselectionActionHandler;
    private FrameLayout mTranslucentFrame;
    private TextView mUsabilitySettingsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usability_settings);

        mScrollViewContainer = findViewById(R.id.scroll_view_container);

        mUsabilitySettingsText = findViewById(R.id.text_usability_settings);

        mAgeSetting = findViewById(R.id.age_setting);
        mAgeSetting.setSettingsCallback(this);

        mGenderSetting = findViewById(R.id.gender_setting);
        mGenderSetting.setSettingsCallback(this);

        mHandSetting = findViewById(R.id.hand_setting);
        mHandSetting.setSettingsCallback(this);

        mSizeSetting = findViewById(R.id.size_setting);
        mSizeSetting.setSettingsCallback(this);

        mColorSetting = findViewById(R.id.color_setting);
        mColorSetting.setSettingsCallback(this);

        mTranslucentFrame = findViewById(R.id.translucent_frame);

        mSettingsContainer = findViewById(R.id.settings_container);

        initDeselectionActionHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadProfile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAgeSetting.updateAge();
        mGenderSetting.updateOtherGender();
    }


    @Override
    public boolean isReading() {
        return mIsReading;
    }

    @Override
    public UserProfile getUserProfile() {
        return mProfile;
    }

    @Override
    public void focusOnSetting(BaseSetting setting) {
        mLastSelectionTimestamp = System.currentTimeMillis();
        hideExpanded();
        setting.show();
        final View v = setting.getExpandedContainer();
        v.post(new Runnable() {
            @Override
            public void run() {
                showTransparency(true);

                ViewUtils.verticalScrollToView(UsabilitySettingsActivity.this, mScrollViewContainer, v);
            }
        });
    }

    private void initDeselectionActionHandler() {
        mDeselectionActionHandler = new Handler();
        mDeselectionActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (now - Constants.SELECTION_THRESHOLD > mLastSelectionTimestamp) {
                    hideExpanded();
                    showTransparency(false);
                }
                mDeselectionActionHandler.postDelayed(this, Constants.HANDLER_DELAY);
            }
        }, Constants.HANDLER_DELAY);
    }


    @Override
    public void loadProfile() {
        mProfile = ProfileReader.getUserProfileFromKatsunaServices(this);
        Log.d(TAG, mProfile.toString());

        mIsReading = true;


        // load age
        mAgeSetting.loadProfile(mProfile);
        mAgeSetting.setAge(mProfile.age);

        // load gender
        mGenderSetting.loadProfile(mProfile);

        // load hand settings
        mHandSetting.loadProfile(mProfile);

        // load optical size
        mSizeSetting.loadProfile(mProfile);

        // load color profile
        mColorSetting.loadProfile(mProfile);

        adjustProfile(mProfile);

        mIsReading = false;
    }

    @Override
    public void showTransparency(boolean enabled) {
        if (enabled) {
            // adjust translucent frame height
            if (mSettingsContainer.getMeasuredHeight() > mTranslucentFrame.getMeasuredHeight()) {
                mTranslucentFrame.setMinimumHeight(mSettingsContainer.getMeasuredHeight());
            }
            mTranslucentFrame.setVisibility(View.VISIBLE);
        } else {
            mTranslucentFrame.setVisibility(View.GONE);
        }
    }

    private void hideExpanded() {
        mTranslucentFrame.setMinimumHeight(0);
        mAgeSetting.hide();
        mGenderSetting.hide();
        mHandSetting.hide();
        mSizeSetting.hide();
        mColorSetting.hide();
    }


    private void adjustProfile(UserProfile profile) {
        OpticalParams body2Params = SizeCalc.getOpticalParams(SizeProfileKey.BODY_2, profile.opticalSizeProfile);
        SizeAdjuster.adjustText(this, mUsabilitySettingsText, body2Params);

        RelativeLayout.LayoutParams mUsabilitySettingsTextLayoutParams =
                (RelativeLayout.LayoutParams) mUsabilitySettingsText.getLayoutParams();
        if (profile.isRightHanded) {
            mUsabilitySettingsTextLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            mUsabilitySettingsTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        } else {
            mUsabilitySettingsTextLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START);
            mUsabilitySettingsTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        // apply layouts
        mUsabilitySettingsText.setLayoutParams(mUsabilitySettingsTextLayoutParams);
    }
}

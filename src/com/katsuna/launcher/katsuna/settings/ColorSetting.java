package com.katsuna.launcher.katsuna.settings;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.Preference;
import com.katsuna.commons.entities.PreferenceKey;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorAdjuster;
import com.katsuna.commons.utils.PreferenceUtils;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.launcher.R;

public class ColorSetting extends BaseSetting {

    private View mColorInitialContainer;

    private RadioGroup mColorProfiles;
    private RadioButton mProfileMain;
    private RadioButton mProfileImpairement;
    private RadioButton mProfileContrast;
    private RadioButton mProfileContrastImpairement;

    public ColorSetting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mTopContainer = findViewById(R.id.color_setting);
        mExpandedContainer = findViewById(R.id.color_expanded_container);

        mColorInitialContainer = findViewById(R.id.color_initial_container);
        mColorInitialContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });

        mProfileMain = findViewById(R.id.profile_main);
        mProfileImpairement = findViewById(R.id.profile_impairement);
        mProfileContrast = findViewById(R.id.profile_contrast);
        mProfileContrastImpairement =
                findViewById(R.id.profile_contrast_impairement);

        mColorProfiles = findViewById(R.id.color_profiles);
        mColorProfiles.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // do nothing because we are just reading values and this is not a use
                // interaction.
                if (mSettingsCallback.isReading()) return;

                Preference preference = new Preference();
                preference.setKey(PreferenceKey.COLOR_PROFILE);
                ColorProfile colorProfile = null;
                switch (checkedId) {
                    case R.id.profile_main:
                        preference.setValue(ColorProfile.MAIN.name());
                        colorProfile = ColorProfile.MAIN;
                        break;
                    case R.id.profile_impairement:
                        preference.setValue(ColorProfile.COLOR_IMPAIREMENT.name());
                        colorProfile = ColorProfile.COLOR_IMPAIREMENT;
                        break;
                    case R.id.profile_contrast:
                        preference.setValue(ColorProfile.CONTRAST.name());
                        colorProfile = ColorProfile.CONTRAST;
                        break;
                    case R.id.profile_contrast_impairement:
                        preference.setValue(ColorProfile.COLOR_IMPAIRMENT_AND_CONTRAST.name());
                        colorProfile = ColorProfile.COLOR_IMPAIRMENT_AND_CONTRAST;
                        break;
                }
                PreferenceUtils.updatePreference(getContext(), preference);
                updateColorProfile(colorProfile);
            }
        });

    }

    public void loadProfile(final UserProfile profile) {
        // change hand layouts if necessary
        if (rightHandLayout != profile.isRightHanded) {
            int layoutId = profile.isRightHanded ? R.layout.katsuna_color_profile_rh :
                    R.layout.katsuna_color_profile_lh;

            ViewGroup parent = findViewById(R.id.color_setting);
            replaceFirstViewInParent(parent, layoutId);

            init();
        }

        rightHandLayout = profile.isRightHanded;

        mProfileMain.post(new Runnable() {
            @Override
            public void run() {
                selectColorProfile(profile.colorProfile);
            }
        });

        SizeAdjuster.applySizeProfile(getContext(), this, profile);

        ColorAdjuster.adjustRadioButton(getContext(), profile, mProfileContrast);
        ColorAdjuster.adjustRadioButton(getContext(), profile, mProfileContrastImpairement);
        ColorAdjuster.adjustRadioButton(getContext(), profile, mProfileImpairement);
        ColorAdjuster.adjustRadioButton(getContext(), profile, mProfileMain);
    }

    private void selectColorProfile(ColorProfile colorProfile) {

        switch (colorProfile) {
            case MAIN:
                mProfileMain.setChecked(true);
                break;
            case COLOR_IMPAIREMENT:
                mProfileImpairement.setChecked(true);
                break;
            case CONTRAST:
                mProfileContrast.setChecked(true);
                break;
            case COLOR_IMPAIRMENT_AND_CONTRAST:
                mProfileContrastImpairement.setChecked(true);
                break;
        }
    }

    private void updateColorProfile(ColorProfile colorProfile) {
        Preference preference = new Preference();
        preference.setKey(PreferenceKey.COLOR_PROFILE);
        preference.setValue(colorProfile.name());
        PreferenceUtils.updatePreference(getContext(), preference);

        mSettingsCallback.loadProfile();
    }
}

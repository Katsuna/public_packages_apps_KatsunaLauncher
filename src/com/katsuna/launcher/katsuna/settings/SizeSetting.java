package com.katsuna.launcher.katsuna.settings;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.katsuna.commons.entities.Preference;
import com.katsuna.commons.entities.PreferenceKey;
import com.katsuna.commons.entities.SizeProfile;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.profile.Adjuster;
import com.katsuna.commons.utils.ColorAdjuster;
import com.katsuna.commons.utils.PreferenceUtils;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.launcher.R;

public class SizeSetting extends BaseSetting {

    private View mSizeInitialContainer;
    private RadioGroup mRadioGroupSize;
    private RadioButton mRadioSizeAdvanced;
    private RadioButton mRadioSizeIntermediate;
    private RadioButton mRadioSizeSimple;
    private View mFabSample;
    private TextView mFabSampleText;

    public SizeSetting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mTopContainer = findViewById(R.id.size_setting);
        mRadioSizeAdvanced = findViewById(R.id.radio_size_advanced);
        mRadioSizeIntermediate = findViewById(R.id.radio_size_intermediate);
        mRadioSizeSimple = findViewById(R.id.radio_size_simple);

        mRadioGroupSize = findViewById(R.id.radio_group_size);
        mRadioGroupSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // do nothing because we are just reading values and this is not a use
                // interaction.
                if (mSettingsCallback.isReading()) return;

                Preference preference = new Preference();
                preference.setKey(PreferenceKey.OPTICAL_SIZE_PROFILE);
                SizeProfile sizeProfile = SizeProfile.INTERMEDIATE;
                switch (checkedId) {
                    case R.id.radio_size_advanced:
                        sizeProfile = SizeProfile.ADVANCED;
                        break;
                    case R.id.radio_size_intermediate:
                        sizeProfile = SizeProfile.INTERMEDIATE;
                        break;
                    case R.id.radio_size_simple:
                        sizeProfile = SizeProfile.SIMPLE;
                        break;
                }
                preference.setValue(sizeProfile.name());
                PreferenceUtils.updatePreference(getContext(), preference);

                mSettingsCallback.loadProfile();
                mSettingsCallback.focusOnSetting(SizeSetting.this);
            }
        });

        mExpandedContainer = findViewById(R.id.size_expanded_container);

        mSizeInitialContainer = findViewById(R.id.size_initial_container);
        mSizeInitialContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });

        mFabSample = findViewById(R.id.commom_size_sample_fab);
        mFabSampleText = findViewById(R.id.commom_size_sample_fab_text);
    }

    public void loadProfile(UserProfile profile) {
        // change hand layouts if necessary
        if (rightHandLayout != profile.isRightHanded) {
            int layoutId = profile.isRightHanded ? R.layout.katsuna_size_rh :
                    R.layout.katsuna_size_lh;

            ViewGroup parent = findViewById(R.id.size_setting);
            replaceFirstViewInParent(parent, layoutId);

            init();
        }

        rightHandLayout = profile.isRightHanded;

        switch (profile.opticalSizeProfile) {
            case ADVANCED:
                mRadioSizeAdvanced.setChecked(true);
                break;
            case AUTO:
            case INTERMEDIATE:
                mRadioSizeIntermediate.setChecked(true);
                break;
            case SIMPLE:
                mRadioSizeSimple.setChecked(true);
                break;
        }

        SizeAdjuster.applySizeProfile(getContext(), this, profile);

        ColorAdjuster.adjustRadioButton(getContext(), profile, mRadioSizeAdvanced, 1);
        ColorAdjuster.adjustRadioButton(getContext(), profile, mRadioSizeIntermediate, 1);
        ColorAdjuster.adjustRadioButton(getContext(), profile, mRadioSizeSimple, 1);

        Adjuster adjuster = new Adjuster(getContext(), profile);
        adjuster.adjustFabSample(mFabSample, mFabSampleText);
        adjuster.adjustFabSampleSize(mFabSample, mFabSampleText);

    }
}

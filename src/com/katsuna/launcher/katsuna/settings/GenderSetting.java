package com.katsuna.launcher.katsuna.settings;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.katsuna.commons.entities.Gender;
import com.katsuna.commons.entities.OpticalParams;
import com.katsuna.commons.entities.Preference;
import com.katsuna.commons.entities.PreferenceKey;
import com.katsuna.commons.entities.SizeProfileKey;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorAdjuster;
import com.katsuna.commons.utils.PreferenceUtils;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.commons.utils.SizeCalc;
import com.katsuna.launcher.R;

public class GenderSetting extends BaseSetting {

    private View mGenderInitialContainer;
    private RadioGroup mRadioGroupGender;
    private RadioButton mRadioMale;
    private RadioButton mRadioFemale;
    private RadioButton mRadioOther;
    private EditText mTextOtherGenderDescr;

    public GenderSetting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mTopContainer = findViewById(R.id.gender_setting);
        mExpandedContainer = findViewById(R.id.gender_expanded_container);
        mRadioMale = findViewById(R.id.radio_gender_male);
        mRadioFemale = findViewById(R.id.radio_gender_female);
        mRadioOther = findViewById(R.id.radio_gender_other);
        mTextOtherGenderDescr = findViewById(R.id.text_other_gender_descr);
        mRadioGroupGender = findViewById(R.id.radio_group_gender);
        mRadioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // do nothing because we are just reading values and this is not a use
                // interaction.
                if (mSettingsCallback.isReading()) return;

                Preference preference = new Preference();
                preference.setKey(PreferenceKey.GENDER);
                switch (checkedId) {
                    case R.id.radio_gender_male:
                        preference.setValue(Gender.MALE.name());
                        mTextOtherGenderDescr.setVisibility(View.GONE);
                        mTextOtherGenderDescr.setText(null);
                        break;
                    case R.id.radio_gender_female:
                        preference.setValue(Gender.FEMALE.name());
                        mTextOtherGenderDescr.setVisibility(View.GONE);
                        mTextOtherGenderDescr.setText(null);
                        break;
                    case R.id.radio_gender_other:
                        preference.setValue(Gender.OTHER.name());
                        preference.setDescr(mTextOtherGenderDescr.getText().toString());
                        mTextOtherGenderDescr.setVisibility(View.VISIBLE);
                        break;
                }
                PreferenceUtils.updatePreference(getContext(), preference);
            }
        });

        mGenderInitialContainer = findViewById(R.id.gender_initial_container);
        mGenderInitialContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });
    }

    public void updateOtherGender() {
        UserProfile profile = mSettingsCallback.getUserProfile();
        if (profile != null) {
            if (mRadioOther.isChecked()) {
                String newValue = mTextOtherGenderDescr.getText().toString().trim();
                if (!newValue.equals(profile.genderInfo.descr)) {
                    Preference preference = new Preference(PreferenceKey.GENDER, Gender.OTHER.name(),
                            String.valueOf(newValue));
                    PreferenceUtils.updatePreference(getContext(), preference);
                }
            }
        }
    }

    public void loadProfile(final UserProfile profile) {
        // change hand layouts if necessary
        if (rightHandLayout != profile.isRightHanded) {
            int layoutId = profile.isRightHanded ? R.layout.katsuna_gender_rh :
                    R.layout.katsuna_gender_lh;

            ViewGroup parent = findViewById(R.id.gender_setting);
            replaceFirstViewInParent(parent, layoutId);

            init();
        }

        rightHandLayout = profile.isRightHanded;

        mRadioMale.post(new Runnable() {
            @Override
            public void run() {
                switch (profile.genderInfo.gender) {
                    case MALE:
                        mRadioMale.setChecked(true);
                        mTextOtherGenderDescr.setVisibility(View.GONE);
                        break;
                    case FEMALE:
                        mRadioFemale.setChecked(true);
                        mTextOtherGenderDescr.setVisibility(View.GONE);
                        break;
                    case OTHER:
                        mRadioOther.setChecked(true);
                        mTextOtherGenderDescr.setText(profile.genderInfo.descr);
                        mTextOtherGenderDescr.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        SizeAdjuster.applySizeProfile(getContext(), this, profile);

        OpticalParams body2Params = SizeCalc.getOpticalParams(SizeProfileKey.BODY_2,
                profile.opticalSizeProfile);
        SizeAdjuster.adjustText(getContext(), mRadioMale, body2Params);
        SizeAdjuster.adjustText(getContext(), mRadioFemale, body2Params);
        SizeAdjuster.adjustText(getContext(), mRadioOther, body2Params);

        ColorAdjuster.adjustRadioButton(getContext(), profile, mRadioMale);
        ColorAdjuster.adjustRadioButton(getContext(), profile, mRadioFemale);
        ColorAdjuster.adjustRadioButton(getContext(), profile, mRadioOther);
    }
}

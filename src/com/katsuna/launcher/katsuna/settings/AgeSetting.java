package com.katsuna.launcher.katsuna.settings;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.katsuna.commons.entities.Preference;
import com.katsuna.commons.entities.PreferenceKey;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.AlertUtils;
import com.katsuna.commons.utils.PreferenceUtils;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.launcher.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AgeSetting extends BaseSetting implements AlertUtils.Callback {

    private static final String TAG = "AgeSetting";

    private View mAgeInitialContainer;
    private EditText mDay;
    private EditText mMonth;
    private EditText mYear;
    private TextView mValidationError;
    private Date mDate;

    public AgeSetting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mTopContainer = findViewById(R.id.age_setting);
        mValidationError = findViewById(R.id.validation_error);
        mDay = findViewById(R.id.day);
        mMonth = findViewById(R.id.month);
        mYear = findViewById(R.id.year);

        mYear.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    updateAge();
                }
                return false;
            }
        });

        mExpandedContainer = findViewById(R.id.age_expanded_container);

        mAgeInitialContainer = findViewById(R.id.age_initial_container);
        mAgeInitialContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });
    }

    public void setAge(String age) {
        if (TextUtils.isEmpty(age)) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(age);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);

            mYear.setText(String.valueOf(year));
            mMonth.setText(String.valueOf(month));
            mDay.setText(String.valueOf(day));
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void updateAge() {
        UserProfile profile = mSettingsCallback.getUserProfile();
        if (profile != null && mDay.getVisibility() == View.VISIBLE) {
            if (isValid()) {
                mValidationError.setVisibility(GONE);
                String ageStr = getDateString();

                if (!profile.age.equals(ageStr)) {
                    Preference preference = new Preference(PreferenceKey.AGE, ageStr);
                    PreferenceUtils.updatePreference(getContext(), preference);
                }
            } else {
                mValidationError.setVisibility(VISIBLE);
            }
        }
    }

    private boolean shouldCheckForValidity() {
        return (mDay.getText().length() > 0
                && mMonth.getText().length() > 0
                && mYear.getText().length() > 0);
    }

    private boolean isValid() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            mDate = dateFormat.parse(getDateString());
        } catch (ParseException pe) {
            return false;
        }

        mValidationError.setVisibility(View.GONE);
        return true;
    }

    private String getDateString() {
        String day = mDay.getText().toString();
        String month = mMonth.getText().toString();
        String year = mYear.getText().toString();
        return year + "-" + month + "-" + day;
    }


    public void loadProfile(UserProfile profile) {
        // change hand layouts if necessary
        if (rightHandLayout != profile.isRightHanded) {
            int layoutId = profile.isRightHanded ? R.layout.katsuna_age_rh :
                    R.layout.katsuna_age_lh;

            ViewGroup parent = findViewById(R.id.age_setting);
            replaceFirstViewInParent(parent, layoutId);

            init();
        }

        rightHandLayout = profile.isRightHanded;

        SizeAdjuster.applySizeProfile(getContext(), this, profile);

        String dayTitle = getContext().getString(R.string.common_select_day);
        String monthTitle = getContext().getString(R.string.common_select_month);
        String yearTitle = getContext().getString(R.string.common_select_year);

        AlertUtils.createListAlert(getContext(), mDay, null, dayTitle,
                mSettingsCallback.getUserProfile(), AlertUtils.getDays(), null, this);
        AlertUtils.createListAlert(getContext(), mMonth, null, monthTitle,
                mSettingsCallback.getUserProfile(), AlertUtils.getMonths(),
                AlertUtils.getMonthsLabels(), this);
        AlertUtils.createListAlert(getContext(), mYear, "1955", yearTitle,
                mSettingsCallback.getUserProfile(), AlertUtils.getYears(), null, this);
    }

    @Override
    public void onListItemSelected() {

        if (shouldCheckForValidity()) {
            if (isValid()) {
                updateAge();
            } else {
                Toast.makeText(getContext(), R.string.common_date_validation_error,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
}

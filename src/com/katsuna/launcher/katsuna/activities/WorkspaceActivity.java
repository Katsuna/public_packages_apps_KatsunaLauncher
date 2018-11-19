package com.katsuna.launcher.katsuna.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.entities.UserProfileContainer;
import com.katsuna.commons.utils.DeviceUtils;
import com.katsuna.commons.utils.KatsunaAlertBuilder;
import com.katsuna.commons.utils.KatsunaUtils;
import com.katsuna.commons.utils.ProfileReader;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.calendar.CalendarCellDecorator;
import com.katsuna.launcher.katsuna.calendar.KatsunaCalendarCellDecorator;
import com.katsuna.launcher.katsuna.calendar.MonthCellDescriptor;
import com.katsuna.launcher.katsuna.calendar.MonthView;
import com.katsuna.launcher.katsuna.calendar.MonthViewFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class WorkspaceActivity extends AppCompatActivity {


    private ViewGroup mCalendarWrapper;

    private View mCalendarContainer;
    private CardView mCalendarCard;
    private Button mCalendarButton;
    private AlertDialog mDialog;
    private UserProfile mUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.katsuna_workspace);

        mCalendarWrapper = findViewById(R.id.calendar_wrapper);
        mCalendarContainer = findViewById(R.id.calendar_container);
        mCalendarCard = findViewById(R.id.calendar_card);
        mCalendarCard.setOnClickListener(v -> {
            if (mCalendarContainer.getVisibility() == View.GONE) {
                mCalendarContainer.setVisibility(View.VISIBLE);
                int bgColor = ContextCompat.getColor(WorkspaceActivity.this, R
                    .color.workspace_activity_bg);
                mCalendarCard.setCardBackgroundColor(bgColor);
            } else {
                mCalendarContainer.setVisibility(View.GONE);

                int bgColor = ContextCompat.getColor(WorkspaceActivity.this, R
                    .color.workspace_card_handler_bg);
                mCalendarCard.setCardBackgroundColor(bgColor);
            }
        });

        mCalendarButton = findViewById(R.id.calendar_button);
        mCalendarButton.setOnClickListener(v -> openCalendarApp());
    }

    private void openCalendarApp() {
        String targetPackage = KatsunaUtils.KATSUNA_CALENDAR_PACKAGE;

        if (!DeviceUtils.openApp(this, targetPackage)) {
            showCalendarAppInstallationDialog();
        }
    }

    private void showCalendarAppInstallationDialog() {
        KatsunaAlertBuilder builder = new KatsunaAlertBuilder(this);
        String appName = getString(R.string.common_katsuna_calendar_app);
        String title = getString(R.string.common_missing_app, appName);
        builder.setTitle(title);
        builder.setView(R.layout.common_katsuna_alert);
        builder.setUserProfile(mUserProfile);
        builder.setOkListener(v -> KatsunaUtils.goToGooglePlay(WorkspaceActivity.this,
            KatsunaUtils.KATSUNA_CALENDAR_PACKAGE));

        mDialog = builder.create();
        mDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // calc current userProfile
        UserProfileContainer userProfileContainer = ProfileReader.getKatsunaUserProfile(this);
        mUserProfile = userProfileContainer.getActiveUserProfile();
        mCalendarWrapper.removeAllViews();
        addCalendar();
    }

    private void addCalendar() {
        List<CalendarCellDecorator> decorators = new ArrayList<>();

        decorators.add(new KatsunaCalendarCellDecorator(mUserProfile));

        MonthViewFactory monthViewFactory = new MonthViewFactory(this);
        monthViewFactory.setListener(new CellClickedListener());
        monthViewFactory.setDecorators(decorators);

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        MonthView monthView = monthViewFactory.getMonthView(month, year, mCalendarWrapper);
        mCalendarWrapper.addView(monthView);
    }


    private class CellClickedListener implements MonthView.Listener {
        @Override
        public void handleClick(MonthCellDescriptor cell) {
            Timber.d("Handling click on %s", cell);
        }
    }
}

package com.katsuna.launcher.katsuna.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.entities.UserProfileContainer;
import com.katsuna.commons.utils.ProfileReader;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.calendar.CalendarCellDecorator;
import com.katsuna.launcher.katsuna.calendar.KatsunaCalendarCellDecorator;
import com.katsuna.launcher.katsuna.calendar.MonthCellDescriptor;
import com.katsuna.launcher.katsuna.calendar.MonthView;
import com.katsuna.launcher.katsuna.calendar.MonthViewFactory;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class WorkspaceActivity extends AppCompatActivity {


    private ViewGroup mCalendarWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.katsuna_workspace);

        mCalendarWrapper = findViewById(R.id.calendar_wrapper);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCalendarWrapper.removeAllViews();
        addCalendar();
    }

    private void addCalendar() {
        List<CalendarCellDecorator> decorators = new ArrayList<>();
        UserProfileContainer userProfileContainer = ProfileReader.getKatsunaUserProfile(this);

        UserProfile userProfile = userProfileContainer.getActiveUserProfile();
        decorators.add(new KatsunaCalendarCellDecorator(userProfile));

        MonthViewFactory monthViewFactory = new MonthViewFactory(this);
        monthViewFactory.setListener(new CellClickedListener());
        monthViewFactory.setDecorators(decorators);

        MonthView monthView = monthViewFactory.getMonthView(10, 2018, mCalendarWrapper);
        mCalendarWrapper.addView(monthView);
    }


    private class CellClickedListener implements MonthView.Listener {
        @Override
        public void handleClick(MonthCellDescriptor cell) {
            Timber.d("Handling click on %s", cell);
        }
    }
}

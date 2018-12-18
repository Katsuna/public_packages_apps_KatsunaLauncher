package com.katsuna.launcher.katsuna.dashboard.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.katsuna.commons.color.ColorTransparentUtils;
import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.ColorProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.entities.UserProfileContainer;
import com.katsuna.commons.utils.BackgroundGenerator;
import com.katsuna.commons.utils.ColorCalcV2;
import com.katsuna.commons.utils.DrawUtils;
import com.katsuna.commons.utils.KatsunaAlertBuilder;
import com.katsuna.commons.utils.KatsunaUtils;
import com.katsuna.commons.utils.ProfileReader;
import com.katsuna.commons.utils.SeekBarUtils;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.commons.utils.ToggleButtonAdjuster;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.calendar.CalendarCellDecorator;
import com.katsuna.launcher.katsuna.calendar.KatsunaCalendarCellDecorator;
import com.katsuna.launcher.katsuna.calendar.MonthView;
import com.katsuna.launcher.katsuna.calendar.MonthViewFactory;
import com.katsuna.launcher.katsuna.dashboard.DashboardContract;
import com.katsuna.launcher.katsuna.dashboard.DashboardPresenter;
import com.katsuna.launcher.katsuna.dashboard.DashboardViewType;
import com.katsuna.launcher.katsuna.dashboard.data.LocationDataSource;
import com.katsuna.launcher.katsuna.dashboard.data.LocationMemoryDataSource;
import com.katsuna.launcher.katsuna.dashboard.data.WeatherDataSource;
import com.katsuna.launcher.katsuna.dashboard.domain.Weather;
import com.katsuna.launcher.katsuna.dashboard.factories.WeatherDataSourceFactory;
import com.katsuna.launcher.katsuna.dashboard.utils.ConditionUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.DeviceUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.IDeviceUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.IPermissionUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.ISettingsController;
import com.katsuna.launcher.katsuna.dashboard.utils.PermissionUtils;
import com.katsuna.launcher.katsuna.dashboard.utils.SettingsController;
import com.katsuna.launcher.katsuna.dashboard.utils.SoundBroadcastReceiver;
import com.katsuna.launcher.katsuna.dashboard.utils.WeatherJobScheduler;
import com.katsuna.launcher.katsuna.dashboard.utils.WeatherUtils;

import org.threeten.bp.LocalTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.katsuna.launcher.katsuna.WeatherConstants.LOCATION_PERMISSIONS;

public class DashboardActivity extends AppCompatActivity implements DashboardContract.View {

    private static final String TAG = DashboardActivity.class.getSimpleName();

    private static final int PERMISSION_LOCATION_CODE = 1;

    private DashboardContract.Presenter mPresenter;
    private TextView mCurrentDate;
    private View mExtendedWeatherContainer;
    private View mPermissionContainer;
    private View mNoRecentWeatherContainer;
    private View mNoWeatherContainer;
    private IPermissionUtils mPermissionUtils;
    private View mWeatherContainer;
    private ImageView mCurrentWeatherIcon;
    private TextView mCurrentWeatherTemp;
    private TextView mCurrentWeatherTempUnit;
    private TextView mCurrentWeatherDesc;
    private int[] mDescIDS;
    private int[] mIconsIDs;
    private int[] mTempIDs;
    private TextView mSelectDay;
    private TextView mSelectWeek;
    private View mSelectDayUnderline;
    private View mSelectWeekUnderline;
    private TextView mBatteryLevel;
    private TextView mWifiStatus;
    private TextView mDataStatus;
    private TextView mDndStatus;

    private ViewGroup mCalendarWrapper;

    private View mCalendarContainer;
    private UserProfile mUserProfile;
    private View mQsLayout;
    private View mQsExpandedLayout;
    private SeekBar mBrightnessSeekbar;
    private SeekBar mSoundSeekbar;
    private ToggleButton mWifiToggle;
    private ToggleButton mDndToggle;
    private boolean mVolumeUnderChange = false;
    private SoundBroadcastReceiver mSoundReceiver;
    private int mPrimaryColor1Trans54;
    private int mPrimaryColor1;
    private boolean mShortTermShown = false;
    private int mWhite54;
    private TextView mRetrySyncButton;
    private TextView mInitButton;
    private TextView mInitWeatherLabel;
    private TextView mRetrySyncLabel;
    private TextView mBatteryExLevel;
    private TextView mDataExStatus;
    private ImageView mWifiIcon;
    private ImageView mDataIcon;
    private ImageView mDndIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        init();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mPresenter.loadData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.loadData();

        // calc current userProfile
        UserProfileContainer userProfileContainer = ProfileReader.getKatsunaUserProfile(this);
        mUserProfile = userProfileContainer.getActiveUserProfile();
        mCalendarWrapper.removeAllViews();
        addCalendar();

        readVolume();

        adjustProfiles();
        mSoundReceiver.setListening(true);
    }

    @Override
    protected void onStop() {
        mSoundReceiver.setListening(false);
        super.onStop();
    }

    private void addCalendar() {
        List<CalendarCellDecorator> decorators = new ArrayList<>();

        decorators.add(new KatsunaCalendarCellDecorator(mUserProfile));

        MonthViewFactory monthViewFactory = new MonthViewFactory(this);
        monthViewFactory.setDecorators(decorators);

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        MonthView monthView = monthViewFactory.getMonthView(month, year, mCalendarWrapper);
        mCalendarWrapper.addView(monthView);
    }

    private void scheduleWeatherJob() {
        WeatherJobScheduler scheduler = new WeatherJobScheduler(this);
        scheduler.schedule();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_CODE: {
                if (mPermissionUtils.hasPermissions(LOCATION_PERMISSIONS)) {
                    scheduleWeatherJob();
                    mPresenter.loadWeather();
                }
                break;
            }
        }
    }

    private void init() {
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.common_black38));

        mWhite54 = ColorTransparentUtils.convertIntoColorInt(Color.WHITE, 54);
        mPermissionContainer = findViewById(R.id.permission_container);
        Button mPermissionButton = findViewById(R.id.permission_button);
        mPermissionButton.setOnClickListener(v ->
            requestPermissions(LOCATION_PERMISSIONS, PERMISSION_LOCATION_CODE));

        mNoWeatherContainer = findViewById(R.id.no_weather_container);
        mInitWeatherLabel = findViewById(R.id.init_weather_label);
        mInitButton = findViewById(R.id.init_button);
        mInitButton.setPaintFlags(mInitButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mInitButton.setOnClickListener(v -> mPresenter.sync());

        mNoRecentWeatherContainer = findViewById(R.id.no_recent_weather_container);
        mRetrySyncLabel = findViewById(R.id.sync_weather_label);
        mRetrySyncButton = findViewById(R.id.retry_sync_button);
        mRetrySyncButton.setPaintFlags(mRetrySyncButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mRetrySyncButton.setOnClickListener(v -> mPresenter.sync());

        mWeatherContainer = findViewById(R.id.weather_container);

        mCurrentDate = findViewById(R.id.current_date);
        mCurrentWeatherIcon = findViewById(R.id.current_weather_icon);
        mCurrentWeatherTemp = findViewById(R.id.current_weather_temp);
        mCurrentWeatherTempUnit = findViewById(R.id.current_weather_temp_unit);
        mCurrentWeatherDesc = findViewById(R.id.current_weather_desc);

        mExtendedWeatherContainer = findViewById(R.id.extended_weather_container);
        LinearLayout mCurrentWeatherContainer = findViewById(R.id.current_weather_container);
        mCurrentWeatherContainer.setOnClickListener(
            v -> mPresenter.expandDashboardView(DashboardViewType.WEATHER)
        );
        mSelectDay = findViewById(R.id.select_day);
        mSelectDayUnderline = findViewById(R.id.select_day_underline);
        mSelectDay.setOnClickListener(v -> mPresenter.selectShortTermWeather());
        mSelectWeek = findViewById(R.id.select_week);
        mSelectWeekUnderline = findViewById(R.id.select_week_underline);
        mSelectWeek.setOnClickListener(v -> mPresenter.selectLongTermWeather());

        // long term and short term weather controls
        mDescIDS = new int[]{R.id.desc1, R.id.desc2, R.id.desc3, R.id.desc4, R.id.desc5, R.id.desc6};
        mIconsIDs = new int[]{R.id.icon1, R.id.icon2, R.id.icon3, R.id.icon4, R.id.icon5, R.id.icon6};
        mTempIDs = new int[]{R.id.temp1, R.id.temp2, R.id.temp3, R.id.temp4, R.id.temp5, R.id.temp6};

        // calendar stuff
        mCalendarWrapper = findViewById(R.id.calendar_wrapper);
        mCalendarContainer = findViewById(R.id.calendar_container);
        CardView mCalendarCard = findViewById(R.id.calendar_card);
        mCalendarCard.setOnClickListener(
            v -> mPresenter.expandDashboardView(DashboardViewType.CALENDAR)
        );

        Button mCalendarButton = findViewById(R.id.calendar_button);
        mCalendarButton.setOnClickListener(v -> mPresenter.openCalendarApp());

        // settings stuff
        mQsLayout = findViewById(R.id.qs_layout);
        mBatteryLevel = findViewById(R.id.battery_status);
        mWifiStatus = findViewById(R.id.wifi_status);
        mWifiIcon = findViewById(R.id.wifi_icon);
        mDataIcon = findViewById(R.id.data_icon);
        mDndIcon = findViewById(R.id.dnd_icon);
        mDataStatus = findViewById(R.id.data_status);
        mDndStatus = findViewById(R.id.dnd_status);

        mQsExpandedLayout = findViewById(R.id.qs_expanded_layout);
        CardView mSettingsCard = findViewById(R.id.settings_card);
        mSettingsCard.setOnClickListener(
            v -> mPresenter.expandDashboardView(DashboardViewType.SETTINGS)
        );

        mBrightnessSeekbar = findViewById(R.id.brightness_seekbar);
        mBrightnessSeekbar.setMax(255);
        mBrightnessSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.setBrightness(progress);
            }
        });

        mSoundSeekbar = findViewById(R.id.sound_seekbar);
        mSoundSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                disableVolumeListeners();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.setVolume(progress);
                enableVolumeListeners();
            }
        });
        mWifiToggle = findViewById(R.id.wifi_toggle);
        mWifiToggle.setOnCheckedChangeListener((buttonView, isChecked) ->
            mPresenter.setWifiStatus(isChecked));
        mDataExStatus = findViewById(R.id.data_ex_status);
        mDataExStatus.setOnClickListener(v -> {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$DataUsageSummaryActivity"));

                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Timber.v(TAG, "Data settings usage Activity is not present");
            }

        });
        mBatteryExLevel = findViewById(R.id.battery_ex_label);
        mBatteryExLevel.setOnClickListener(v -> {
                Intent batterySaverIntent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                startActivity(batterySaverIntent);
            }
        );
        mDndToggle = findViewById(R.id.dnd_toggle);
        mDndToggle.setOnCheckedChangeListener((buttonView, isChecked) ->
            mPresenter.setDndStatus(isChecked));
        Button mSettingsButton = findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(v ->
            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0)
        );

        WeatherDataSource dataSource = WeatherDataSourceFactory.getDataSource(this);
        mPermissionUtils = new PermissionUtils(this);
        LocationDataSource locationDataSource = LocationMemoryDataSource.getInstance().init(this);
        IDeviceUtils deviceUtils = new DeviceUtils(this);
        ISettingsController settingsController = new SettingsController(this);
        new DashboardPresenter(dataSource, this, mPermissionUtils, locationDataSource, deviceUtils,
            settingsController);

        mPresenter.start();

        setupListeners();
    }

    private void disableVolumeListeners() {
        mVolumeUnderChange = true;
    }

    private void enableVolumeListeners() {
        new Handler().postDelayed(() -> mVolumeUnderChange = false, 100);
    }

    private void setupListeners() {
        if (mSoundReceiver == null) {
            mSoundReceiver = new SoundBroadcastReceiver(this) {
                @Override
                public void onBroadcastReceived() {
                    readVolume();
                }
            };
        }
    }

    private void readVolume() {
        if (mVolumeUnderChange) return;
        mPresenter.loadVolume();
    }

    @Override
    public void setPresenter(DashboardContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showMissingLocationPermissions(boolean flag) {
        mPermissionContainer.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showSyncProblem() {
        Toast.makeText(this, R.string.sync_problem, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showNoInternetConnection() {
        Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showNoGpsProviderEnabled() {
        Toast.makeText(this, R.string.no_gps_enabled, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showWeather(boolean flag) {
        mWeatherContainer.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showNoWeatherWarning(boolean flag) {
        mNoWeatherContainer.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showNoRecentWeatherWarning(boolean flag) {
        mNoRecentWeatherContainer.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setCurrentDate(String date) {
        mCurrentDate.setText(date);
    }

    @Override
    public void setCurrentWeather(Weather weather) {
        Timber.tag(TAG).d("setCurrentWeather %s", weather.toString());

        int hour = LocalTime.now().getHour();
        int resId = ConditionUtils.getDrawableId(weather.getCondition(), hour);
        if (resId != 0) {
            Drawable drawable = ContextCompat.getDrawable(this, resId);
            mCurrentWeatherIcon.setImageDrawable(drawable);
        }
        mCurrentWeatherTemp.setText(weather.getTemperature());
        mCurrentWeatherTempUnit.setText(weather.getTemperatureUnit());
        mCurrentWeatherDesc.setText(weather.getFullDescription());
    }

    @Override
    public void showShortTermWeather(List<Weather> weatherList) {
        mShortTermShown = true;
        StringBuilder list = new StringBuilder();
        for (Weather weather : weatherList) {
            list.append(weather.toString()).append("\\n");
        }
        Timber.tag(TAG).d("setShortTermWeather %s", list);

        DateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        DateFormat formatDay = new SimpleDateFormat("HH", Locale.getDefault());
        if (weatherList.size() > 0) {
            for (int i = 0; i < 6; i++) {
                Weather weather = weatherList.get(i);

                // desc
                TextView tv = findViewById(mDescIDS[i]);
                tv.setText(format.format(weather.getDate()));

                // icon
                int hour = Integer.parseInt(formatDay.format(weather.getDate()));
                int resId = ConditionUtils.getDrawableId(weather.getCondition(), hour);
                if (resId != 0) {
                    ImageView iv = findViewById(mIconsIDs[i]);
                    Drawable drawable = ContextCompat.getDrawable(this, resId);
                    iv.setImageDrawable(drawable);
                }

                // temperature
                tv = findViewById(mTempIDs[i]);
                tv.setText(weather.getTemperatureFull());
            }
        }

        adjustShortTermColors();
    }

    private void adjustShortTermColors() {
        if (mShortTermShown) {
            if (mUserProfile.colorProfile != ColorProfile.CONTRAST) {
                mSelectDay.setTextColor(mPrimaryColor1);
                mSelectWeek.setTextColor(mPrimaryColor1Trans54);
                mSelectDayUnderline.setBackgroundColor(mPrimaryColor1);
            } else {
                mSelectDay.setTextColor(Color.WHITE);
                mSelectWeek.setTextColor(mWhite54);
                mSelectDayUnderline.setBackgroundColor(Color.WHITE);
            }
            mSelectWeekUnderline.setBackgroundColor(Color.TRANSPARENT);
        } else {
            if (mUserProfile.colorProfile != ColorProfile.CONTRAST) {
                mSelectDay.setTextColor(mPrimaryColor1Trans54);
                mSelectWeek.setTextColor(mPrimaryColor1);
                mSelectWeekUnderline.setBackgroundColor(mPrimaryColor1);
            } else {
                mSelectDay.setTextColor(mWhite54);
                mSelectWeek.setTextColor(Color.WHITE);
                mSelectWeekUnderline.setBackgroundColor(Color.WHITE);
            }
            mSelectDayUnderline.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void showLongTermWeather(List<Weather> weatherList) {
        mShortTermShown = false;
        StringBuilder list = new StringBuilder();
        for (Weather weather : weatherList) {
            list.append(weather.toString()).append("\\n");
        }
        Timber.tag(TAG).d("setLongTermWeather %s", list);

        if (weatherList.size() > 0) {
            for (int i = 0; i < 6; i++) {
                Weather weather = weatherList.get(i);

                // desc
                String dayName = WeatherUtils.getDay(this, weather.getDate());
                TextView tv = findViewById(mDescIDS[i]);
                tv.setText(dayName);

                // icon
                int hour = LocalTime.now().getHour();
                int resId = ConditionUtils.getDrawableId(weather.getCondition(), hour);
                if (resId != 0) {
                    ImageView iv = findViewById(mIconsIDs[i]);
                    Drawable drawable = ContextCompat.getDrawable(this, resId);
                    iv.setImageDrawable(drawable);
                }

                // temperature
                tv = findViewById(mTempIDs[i]);
                tv.setText(weather.getTemperatureFull());
            }
        }

        adjustShortTermColors();
    }

    @Override
    public void showDate(boolean flag) {
        mCurrentDate.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showExtendedSettings(boolean flag) {
        if (flag) {
            mQsLayout.setVisibility(View.GONE);
            mQsExpandedLayout.setVisibility(View.VISIBLE);
        } else {
            mQsExpandedLayout.setVisibility(View.GONE);
            mQsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showExtendedCalendar(boolean flag) {
        mCalendarContainer.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showExtendedWeather(boolean flag) {
        mExtendedWeatherContainer.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showCalendarAppInstallationDialog() {
        KatsunaAlertBuilder builder = new KatsunaAlertBuilder(this);
        String appName = getString(R.string.common_katsuna_calendar_app);
        String title = getString(R.string.common_missing_app, appName);
        builder.setTitle(title);
        builder.setView(R.layout.common_katsuna_alert);
        builder.setUserProfile(mUserProfile);
        builder.setOkListener(v -> KatsunaUtils.goToGooglePlay(DashboardActivity.this,
            KatsunaUtils.KATSUNA_CALENDAR_PACKAGE));

        AlertDialog mDialog = builder.create();
        mDialog.show();
    }

    @Override
    public void setBrightnessLevel(int level) {
        mBrightnessSeekbar.setProgress(level);
    }

    @Override
    public void setVolume(int level) {
        mSoundSeekbar.setProgress(level);
    }

    @Override
    public void showBatteryLevel(int level) {
        mBatteryLevel.setText(getString(R.string.battery_charge_percent, level));
        mBatteryExLevel.setText(getString(R.string.battery_charge_info, level));
    }

    @Override
    public void showWifiStatus(boolean status) {
        int resId = status ? R.string.on : R.string.off;
        mWifiStatus.setText(resId);

        setQsIconColor(mWifiIcon, status);

        if (mWifiToggle.isChecked() != status) {
            mWifiToggle.setChecked(status);
        }
    }

    private void setQsIconColor(ImageView view, boolean status) {
        int iconColorResId = status ? R.color.common_white : R.color.common_white38;
        int iconColor = ContextCompat.getColor(this, iconColorResId);
        DrawUtils.setColor(view.getDrawable(), iconColor);
    }

    @Override
    public void showDataStatus(boolean status) {
        int resId = status ? R.string.on : R.string.off;
        mDataStatus.setText(resId);
        mDataExStatus.setText(resId);
        setQsIconColor(mDataIcon, status);
    }

    @Override
    public void showDndStatus(boolean status) {
        int resId = status ? R.string.on : R.string.off;
        mDndStatus.setText(resId);

        setQsIconColor(mDndIcon, status);

        if (mDndToggle.isChecked() != status) {
            mDndToggle.setChecked(status);
        }
    }

    @Override
    public void askPermissionToWriteSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void adjustProfiles() {
        SeekBarUtils.adjustSeekbarV3(this, mBrightnessSeekbar, mUserProfile,
            R.drawable.ic_brightness_7_black_24dp, R.drawable.seekbar_progress);
        SeekBarUtils.adjustSeekbarV3(this, mSoundSeekbar, mUserProfile,
            R.drawable.ic_volume_up_black_24dp, R.drawable.seekbar_progress);

        Drawable toggleBg = BackgroundGenerator.createToggleBgV3(this, mUserProfile);
        ToggleButtonAdjuster.adjustToggleButton(this, mWifiToggle, toggleBg, mUserProfile);
        ToggleButtonAdjuster.adjustToggleButton(this, mDndToggle, toggleBg, mUserProfile);

        // adjust week and days weather selectors
        mPrimaryColor1 = ColorCalcV2.getColor(this, ColorProfileKeyV2.PRIMARY_COLOR_1,
            mUserProfile.colorProfile);
        mPrimaryColor1Trans54 = ColorTransparentUtils.convertIntoColorInt(mPrimaryColor1, 54);

        adjustShortTermColors();

        adjustTextWarning(mInitWeatherLabel, mPrimaryColor1);
        adjustTextWarning(mRetrySyncLabel, mPrimaryColor1);

        ViewGroup root = findViewById(android.R.id.content);
        SizeAdjuster.applySizeProfileV2(this, root, mUserProfile.opticalSizeProfile);
    }

    private void adjustTextWarning(TextView tv, int color) {
        tv.setTextColor(color);
        for (Drawable dr : tv.getCompoundDrawablesRelative()) {
            if (dr != null) {
                DrawUtils.setColor(dr, color);
            }
        }
    }

}

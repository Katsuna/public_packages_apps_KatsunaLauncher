package com.katsuna.launcher.katsuna.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

import static com.katsuna.commons.utils.DateUtils.betweenDates;
import static com.katsuna.commons.utils.DateUtils.containsDate;
import static com.katsuna.commons.utils.DateUtils.sameDate;
import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class MonthViewFactory {

    private final TimeZone timezone;
    private final Locale locale;
    private final DayViewAdapter dayViewAdapter;
    private final Context mContext;
    private Calendar mMinCal;
    private Calendar mMaxCal;
    private List<Calendar> mSelectedCals;
    private List<Calendar> mHighlightedCals;
    private Calendar today;
    private MonthView.Listener mListener;
    private List<CalendarCellDecorator> mDecorators;
    private boolean mDisplayOnly = true;

    public MonthViewFactory(Context context) {
        mContext = context;

        timezone = TimeZone.getDefault();
        locale = Locale.getDefault();
        dayViewAdapter = new DefaultDayViewAdapter();
    }

    public void setMinCal(Calendar minCal) {
        mMinCal = minCal;
    }

    public void setMaxCal(Calendar maxCal) {
        mMaxCal = maxCal;
    }

    public void setSelectedCals(List<Calendar> selectedCals) {
        mSelectedCals = selectedCals;
    }

    public void setHighlightedCals(List<Calendar> highlightedCals) {
        mHighlightedCals = highlightedCals;
    }

    public void setListener(MonthView.Listener listener) {
        mListener = listener;
    }

    public void setDecorators(List<CalendarCellDecorator> decorators) {
        mDecorators = decorators;
    }

    public void setDisplayOnly(boolean displayOnly) {
        mDisplayOnly = displayOnly;
    }

    public MonthView getMonthView(int month, int year, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        today = Calendar.getInstance(timezone, locale);
        boolean isCurrentMonth = isCurrentMonth(today, month, year);

        SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", locale);
        weekdayNameFormat.setTimeZone(timezone);

        MonthView monthView = MonthView.create(parent, inflater, weekdayNameFormat, today,
            isCurrentMonth, mDecorators, dayViewAdapter);

        monthView.setListener(mListener);
        monthView.setDecorators(mDecorators);

        List<List<MonthCellDescriptor>> cells = getMonthCells(month, year);
        monthView.init(cells, mDisplayOnly);

        return monthView;
    }

    private boolean isCurrentMonth(Calendar cal, int month, int year) {
        Calendar requestedMonth = Calendar.getInstance(timezone, locale);
        requestedMonth.set(MONTH, month);
        requestedMonth.set(YEAR, year);
        requestedMonth.set(DAY_OF_MONTH, 1);

        return cal.get(MONTH) == month && cal.get(YEAR) == year;
    }

    private List<List<MonthCellDescriptor>> getMonthCells(int month, int year) {
        Calendar cal = Calendar.getInstance(timezone, locale);
        // start from day 1 on the requested month
        cal.set(MONTH, month);
        cal.set(YEAR, year);
        cal.set(DAY_OF_MONTH, 1);
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
        if (offset > 0) {
            offset -= 7;
        }
        cal.add(Calendar.DATE, offset);

        List<List<MonthCellDescriptor>> cells = new ArrayList<>();

        while ((cal.get(MONTH) < month + 1 || cal.get(YEAR) < year) && cal.get(YEAR) <= year) {
            Timber.d("Building week row starting at %s", cal.getTime());
            List<MonthCellDescriptor> weekCells = new ArrayList<>();
            cells.add(weekCells);
            for (int c = 0; c < 7; c++) {
                Date date = cal.getTime();
                boolean isCurrentMonth = cal.get(MONTH) == month;
                boolean isSelected = isCurrentMonth && containsDate(mSelectedCals, cal);
                boolean isSelectable = isCurrentMonth && betweenDates(cal, mMinCal, mMaxCal);
                boolean isToday = sameDate(cal, today);
                boolean isHighlighted = containsDate(mHighlightedCals, cal);
                int value = cal.get(DAY_OF_MONTH);

                weekCells.add(new MonthCellDescriptor(date, isCurrentMonth, isSelectable,
                    isSelected, isToday, isHighlighted, value));

                cal.add(DATE, 1);
            }
        }
        return cells;
    }
}

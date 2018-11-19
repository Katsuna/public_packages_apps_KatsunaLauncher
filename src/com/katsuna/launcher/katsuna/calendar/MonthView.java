package com.katsuna.launcher.katsuna.calendar;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.katsuna.launcher.R;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class MonthView extends LinearLayout {
    private CalendarGridView grid;
    private Listener mListener;
    private List<CalendarCellDecorator> decorators;

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static MonthView create(ViewGroup parent, LayoutInflater inflater,
                                   DateFormat weekdayNameFormat, Calendar today,
                                   boolean isCurrentMonth, List<CalendarCellDecorator> decorators,
                                   DayViewAdapter adapter) {

        final MonthView view = (MonthView) inflater.inflate(R.layout.month, parent, false);

        // Set the views
        view.grid = view.findViewById(R.id.calendar_grid);
        view.setDayViewAdapter(adapter);

        int firstDayOfWeek = today.getFirstDayOfWeek();
        final CalendarRowView headerRow = (CalendarRowView) view.grid.getChildAt(0);

        // show days name row
        final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        for (int offset = 0; offset < 7; offset++) {
            today.set(Calendar.DAY_OF_WEEK, getDayOfWeek(firstDayOfWeek + 1, offset));
            final TextView textView = (TextView) headerRow.getChildAt(offset);
            textView.setText(weekdayNameFormat.format(today.getTime()));

            int textColor;
            if (originalDayOfWeek == today.get(Calendar.DAY_OF_WEEK) && isCurrentMonth) {
                textColor = ContextCompat.getColor(view.getContext(), R.color.common_white);
            } else {
                textColor = ContextCompat.getColor(view.getContext(), R.color.common_white54);
            }
            textView.setTextColor(textColor);
        }
        today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);

        view.decorators = decorators;
        return view;
    }

    private static int getDayOfWeek(int firstDayOfWeek, int offset) {
        return firstDayOfWeek + offset;
    }

    public List<CalendarCellDecorator> getDecorators() {
        return decorators;
    }

    public void setDecorators(List<CalendarCellDecorator> decorators) {
        this.decorators = decorators;
    }

    public void init(List<List<MonthCellDescriptor>> cells, boolean displayOnly) {
        long start = System.currentTimeMillis();
        NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);

        final int numRows = cells.size();
        grid.setNumRows(numRows);
        for (int i = 0; i < 6; i++) {
            CalendarRowView weekRow = (CalendarRowView) grid.getChildAt(i + 1);
            weekRow.setListener(mListener);
            if (i < numRows) {
                weekRow.setVisibility(VISIBLE);
                List<MonthCellDescriptor> week = cells.get(i);
                for (int c = 0; c < week.size(); c++) {
                    MonthCellDescriptor cell = week.get(c);
                    CalendarCellView cellView = (CalendarCellView) weekRow.getChildAt(c);

                    String cellDate = numberFormatter.format(cell.getValue());
                    if (!cellView.getDayOfMonthTextView().getText().equals(cellDate)) {
                        cellView.getDayOfMonthTextView().setText(cellDate);
                    }
                    cellView.setEnabled(cell.isCurrentMonth());
                    cellView.setClickable(!displayOnly);

                    cellView.setSelectable(cell.isSelectable());
                    cellView.setSelected(cell.isSelected());
                    cellView.setCurrentMonth(cell.isCurrentMonth());
                    cellView.setToday(cell.isToday());
                    cellView.setHighlighted(cell.isHighlighted());
                    cellView.setTag(cell);
                    cellView.setMonthCellDescriptor(cell);

                    if (null != decorators) {
                        for (CalendarCellDecorator decorator : decorators) {
                            decorator.decorate(cellView, cell.getDate());
                        }
                    }
                }
            } else {
                weekRow.setVisibility(GONE);
            }
        }

        Timber.d("MonthView.init took %d ms", System.currentTimeMillis() - start);
    }

    public void setDayBackground(int resId) {
        grid.setDayBackground(resId);
    }

    public void setDayViewAdapter(DayViewAdapter adapter) {
        grid.setDayViewAdapter(adapter);
    }

    public void setHeaderTextColor(int color) {
        grid.setHeaderTextColor(color);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void handleClick(MonthCellDescriptor cell);
    }
}

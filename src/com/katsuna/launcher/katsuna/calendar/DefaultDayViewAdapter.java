package com.katsuna.launcher.katsuna.calendar;

import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.katsuna.launcher.R;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DefaultDayViewAdapter implements DayViewAdapter {
    @Override
    public void makeCellView(CalendarCellView parent) {

        // create bg imageview
        ImageView bgView = new ImageView(parent.getContext());
        FrameLayout.LayoutParams bgLayoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT,
            WRAP_CONTENT, CENTER);
        parent.addView(bgView, bgLayoutParams);
        parent.setBgView(bgView);

        // create textView
        TextView tv = new TextView(new ContextThemeWrapper(parent.getContext(),
            R.style.CalendarCell_CalendarDate));

        FrameLayout.LayoutParams tvLayoutParams = new FrameLayout.LayoutParams(MATCH_PARENT,
            WRAP_CONTENT, CENTER_VERTICAL);
        parent.addView(tv, tvLayoutParams);
        parent.setDayOfMonthTextView(tv);
    }
}

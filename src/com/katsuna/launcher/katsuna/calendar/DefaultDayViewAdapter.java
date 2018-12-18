package com.katsuna.launcher.katsuna.calendar;

import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.katsuna.launcher.R;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DefaultDayViewAdapter implements DayViewAdapter {
    @Override
    public void makeCellView(CalendarCellView parent) {

        // create bg imageview
        ImageView bgView = new ImageView(parent.getContext());

        int margin = parent.getContext().getResources().getDimensionPixelSize(R.dimen.common_4dp);

        FrameLayout.LayoutParams bgLayoutParams = new FrameLayout.LayoutParams(MATCH_PARENT,
            MATCH_PARENT, CENTER_VERTICAL);
        bgLayoutParams.setMargins(margin, margin, margin, margin);
        parent.addView(bgView, bgLayoutParams);
        parent.setBgView(bgView);

        // create textView
        TextView tv = new TextView(new ContextThemeWrapper(parent.getContext(),
            R.style.CalendarCell_CalendarDate));

        FrameLayout.LayoutParams tvLayoutParams = new FrameLayout.LayoutParams(MATCH_PARENT,
            WRAP_CONTENT, BOTTOM);
        parent.addView(tv, tvLayoutParams);
        parent.setDayOfMonthTextView(tv);
    }
}

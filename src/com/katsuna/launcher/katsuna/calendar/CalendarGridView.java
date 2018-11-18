package com.katsuna.launcher.katsuna.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import timber.log.Timber;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * ViewGroup that draws a grid of calendar cells.  All children must be {@link CalendarRowView}s.
 * The first row is assumed to be a header and no divider is drawn above it.
 */
public class CalendarGridView extends ViewGroup {

    private int oldWidthMeasureSize;
    private int oldNumRows;

    public CalendarGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDayViewAdapter(DayViewAdapter adapter) {
        for (int i = 0; i < getChildCount(); i++) {
            ((CalendarRowView) getChildAt(i)).setDayViewAdapter(adapter);
        }
    }

    public void setDayBackground(int resId) {
        for (int i = 1; i < getChildCount(); i++) {
            ((CalendarRowView) getChildAt(i)).setCellBackground(resId);
        }
    }

    public void setHeaderTextColor(int color) {
        ((CalendarRowView) getChildAt(0)).setCellTextColor(color);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() == 0) {
            ((CalendarRowView) child).setIsHeaderRow(true);
        }
        super.addView(child, index, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Timber.d("Grid.onMeasure w=%s h=%s", MeasureSpec.toString(widthMeasureSpec),
            MeasureSpec.toString(heightMeasureSpec));
        int widthMeasureSize = MeasureSpec.getSize(widthMeasureSpec);
        if (oldWidthMeasureSize == widthMeasureSize) {
            Timber.d("SKIP Grid.onMeasure");
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
            return;
        }
        long start = System.currentTimeMillis();
        oldWidthMeasureSize = widthMeasureSize;
        int cellSize = widthMeasureSize / 7;
        // Remove any extra pixels since /7 is unlikely to give whole nums.
        widthMeasureSize = cellSize * 7;
        int totalHeight = 0;
        final int rowWidthSpec = makeMeasureSpec(widthMeasureSize, EXACTLY);
        // Most cells are gonna be cellSize tall, but we want to allow custom cells to be taller.
        final int rowHeightSpec = makeMeasureSpec(widthMeasureSize, AT_MOST);
        for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
            final View child = getChildAt(c);
            child.setMinimumHeight(cellSize);
            if (child.getVisibility() == View.VISIBLE) {
                if (c == 0) { // It's the header: height should be wrap_content.
                    measureChild(child, rowWidthSpec, makeMeasureSpec(cellSize, AT_MOST));
                } else {
                    measureChild(child, rowWidthSpec, rowHeightSpec);
                }
                totalHeight += child.getMeasuredHeight();
            }
        }
        final int measuredWidth = widthMeasureSize + 2; // Fudge factor to make the borders show up.
        setMeasuredDimension(measuredWidth, totalHeight);
        Timber.d("Grid.onMeasure %d ms", System.currentTimeMillis() - start);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        long start = System.currentTimeMillis();
        top = 0;
        for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
            final View child = getChildAt(c);
            final int rowHeight = child.getMeasuredHeight();
            child.layout(left, top, right, top + rowHeight);
            top += rowHeight;
        }
        Timber.d("Grid.onLayout %d ms", System.currentTimeMillis() - start);
    }

    public void setNumRows(int numRows) {
        if (oldNumRows != numRows) {
            // If the number of rows changes, make sure we do a re-measure next time around.
            oldWidthMeasureSize = 0;
        }
        oldNumRows = numRows;
    }
}

package com.katsuna.launcher.katsuna.calendar;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Describes the state of a particular date cell in a {@link MonthView}.
 */
public class MonthCellDescriptor {

    private final Date date;
    private final int value;
    private final boolean isCurrentMonth;
    private final boolean isToday;
    private final boolean isSelectable;
    private boolean isSelected;
    private boolean isHighlighted;

    public MonthCellDescriptor(Date date, boolean currentMonth, boolean selectable, boolean selected,
                               boolean today, boolean highlighted, int value) {
        this.date = date;
        isCurrentMonth = currentMonth;
        isSelectable = selectable;
        isHighlighted = highlighted;
        isSelected = selected;
        isToday = today;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public boolean isSelectable() {
        return isSelectable;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    boolean isHighlighted() {
        return isHighlighted;
    }

    void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isToday() {
        return isToday;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return "MonthCellDescriptor{"
            + "date="
            + date
            + ", value="
            + value
            + ", isCurrentMonth="
            + isCurrentMonth
            + ", isSelected="
            + isSelected
            + ", isToday="
            + isToday
            + ", isSelectable="
            + isSelectable
            + ", isHighlighted="
            + isHighlighted
            + '}';
    }
}

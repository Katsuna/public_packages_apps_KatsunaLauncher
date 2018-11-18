package com.katsuna.launcher.katsuna.calendar;

import java.util.Date;

public interface CalendarCellDecorator {
    void decorate(CalendarCellView cellView, Date date);
}

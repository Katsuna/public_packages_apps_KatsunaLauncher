package com.katsuna.launcher.katsuna.calendar;

import com.katsuna.commons.entities.UserProfile;

import java.util.Date;

public class KatsunaCalendarCellDecorator implements CalendarCellDecorator {

    private final UserProfile mUserProfile;

    public KatsunaCalendarCellDecorator(UserProfile userProfile) {
        mUserProfile = userProfile;
    }

    @Override
    public void decorate(CalendarCellView cellView, Date date) {
        cellView.decorate(mUserProfile);
    }
}

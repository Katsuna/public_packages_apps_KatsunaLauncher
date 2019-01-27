package com.katsuna.launcher.katsuna.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.ColorProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorCalcV2;
import com.katsuna.launcher.R;

public class ProfileAdjuster {

    public static void adjustHotSeatButtons(Context context, Button activitiesButton,
                                            Button settingsButton, UserProfile userProfile) {

        int pColor1 = ColorCalcV2.getColor(context, ColorProfileKeyV2.PRIMARY_COLOR_1,
            userProfile.colorProfile);

        int pGreyColor1 = ColorCalcV2.getColor(context, ColorProfileKeyV2.PRIMARY_GREY_1,
            userProfile.colorProfile);

        int radius = context.getResources().getDimensionPixelSize(R.dimen.hot_seat_radius);
        Drawable activitiesBg = getBackgroundDrawable(pColor1, radius);
        Drawable settingsBg = getBackgroundDrawable(pGreyColor1, radius);

        activitiesButton.setBackground(activitiesBg);
        settingsButton.setBackground(settingsBg);

        if (userProfile.colorProfile == ColorProfile.CONTRAST) {
            activitiesButton.setTextColor(Color.WHITE);
        } else {
            int black87 = ContextCompat.getColor(context, R.color.common_black87);
            activitiesButton.setTextColor(black87);
            settingsButton.setTextColor(black87);
        }
    }


    private static Drawable getBackgroundDrawable(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        shape.setColor(color);
        return shape;
    }

}

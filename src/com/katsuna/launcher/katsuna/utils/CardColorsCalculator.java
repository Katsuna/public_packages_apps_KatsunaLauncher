package com.katsuna.launcher.katsuna.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.ColorProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorCalcV2;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.AppsGroup;

public class CardColorsCalculator {

    public static CardColors calc(Context context, UserProfile profile, AppsGroup appsGroup,
                                  boolean focused) {
        // calc colors
        int cardColor;
        int cardColorAlpha;

        ColorProfile colorProfile = profile.colorProfile;
        if (appsGroup.premium) {
            cardColor = ColorCalcV2.getColor(context, ColorProfileKeyV2.PRIMARY_COLOR_2, colorProfile);
            cardColorAlpha = ColorCalcV2.getColor(context, ColorProfileKeyV2.SECONDARY_COLOR_2, colorProfile);
        }
        else if (focused) {
            cardColor = ColorCalcV2.getColor(context, ColorProfileKeyV2.PRIMARY_COLOR_1, colorProfile);
            cardColorAlpha = ColorCalcV2.getColor(context, ColorProfileKeyV2.SECONDARY_COLOR_1, colorProfile);
        }
        else {
            cardColor = ContextCompat.getColor(context, R.color.common_transparent);
            cardColorAlpha = ColorCalcV2.getColor(context, ColorProfileKeyV2.SECONDARY_GREY_2, colorProfile);
        }
        return new CardColors(cardColor, cardColorAlpha);
    }

}

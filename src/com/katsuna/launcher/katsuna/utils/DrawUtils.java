package com.katsuna.launcher.katsuna.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class DrawUtils {

    public static Drawable getCircle(int color, int size) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setSize(size, size);
        shape.setColor(color);
        return shape;
    }
}

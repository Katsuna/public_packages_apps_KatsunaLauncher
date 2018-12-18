package com.katsuna.launcher.katsuna.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.ColorProfileKeyV2;
import com.katsuna.commons.entities.OpticalParams;
import com.katsuna.commons.entities.SizeProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorCalcV2;
import com.katsuna.commons.utils.FontFamily;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.commons.utils.SizeCalcV2;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.utils.DrawUtils;

public class CalendarCellView extends FrameLayout {

    private boolean isSelectable = false;
    private boolean isCurrentMonth = false;
    private boolean isToday = false;
    private boolean isHighlighted = false;
    private TextView dayOfMonthTextView;
    private MonthCellDescriptor mMonthCellDescriptor;
    private ImageView mBgView;

    @SuppressWarnings("UnusedDeclaration") //
    public CalendarCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public void setCurrentMonth(boolean isCurrentMonth) {
        if (this.isCurrentMonth != isCurrentMonth) {
            this.isCurrentMonth = isCurrentMonth;
            refreshDrawableState();
        }
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean isToday) {
        if (this.isToday != isToday) {
            this.isToday = isToday;
            //refreshDrawableState();
        }
    }

    public boolean isSelectable() {
        return isSelectable;
    }

    public void setSelectable(boolean isSelectable) {
        if (this.isSelectable != isSelectable) {
            this.isSelectable = isSelectable;
            refreshDrawableState();
        }
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean isHighlighted) {
        if (this.isHighlighted != isHighlighted) {
            this.isHighlighted = isHighlighted;
            refreshDrawableState();
        }
    }

    public TextView getDayOfMonthTextView() {
        if (dayOfMonthTextView == null) {
            throw new IllegalStateException(
                "You have to setDayOfMonthTextView in your custom DayViewAdapter."
            );
        }
        return dayOfMonthTextView;
    }

    public void setDayOfMonthTextView(TextView textView) {
        dayOfMonthTextView = textView;
    }

    public void setBgView(ImageView bgView) {
        mBgView = bgView;
    }

    public void setMonthCellDescriptor(MonthCellDescriptor monthCellDescriptor) {
        mMonthCellDescriptor = monthCellDescriptor;
    }

    public void decorate(UserProfile userProfile) {
        if (!mMonthCellDescriptor.isCurrentMonth()) {
            this.setVisibility(INVISIBLE);
            return;
        }

        Context ctx = getContext();

        OpticalParams opticalParams = SizeCalcV2.getOpticalParams(SizeProfileKeyV2.BUTTON,
            userProfile.opticalSizeProfile);
        SizeAdjuster.adjustText(ctx, dayOfMonthTextView, opticalParams);


        int textColor;
        if (isToday) {
            int color = ColorCalcV2.getColor(ctx, ColorProfileKeyV2.PRIMARY_COLOR_1,
                userProfile.colorProfile);
            if (userProfile.colorProfile == ColorProfile.CONTRAST) {
                color = ContextCompat.getColor(ctx, R.color.common_white);
            }

            int circleSize = getResources().getDimensionPixelSize(R.dimen.common_36dp);
            Drawable bg = DrawUtils.getCircle(color, circleSize);

            mBgView.setImageDrawable(bg);
            textColor = ContextCompat.getColor(ctx, R.color.common_black);
            Typeface tp = Typeface.create(FontFamily.ROBOTO_BLACK, Typeface.NORMAL);
            dayOfMonthTextView.setTypeface(tp);
        } else {
            textColor = ContextCompat.getColor(ctx, R.color.common_white);
        }

        dayOfMonthTextView.setTextColor(textColor);

    }

}

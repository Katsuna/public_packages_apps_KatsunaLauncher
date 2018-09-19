package com.katsuna.launcher.katsuna.settings;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v13.view.ViewCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.settings.callbacks.SettingsCallback;

public class BaseSetting extends LinearLayout {

    private boolean mExpanded;
    View mTopContainer;
    View mExpandedContainer;
    Button mMoreButton;
    SettingsCallback mSettingsCallback;
    boolean rightHandLayout = true;

    public BaseSetting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSettingsCallback(SettingsCallback callback) {
        mSettingsCallback = callback;
    }

    public View getExpandedContainer() {
        return mExpandedContainer;
    }

    public void show() {
        mExpanded = true;
        mExpandedContainer.setVisibility(View.VISIBLE);

        int elevation = getContext().getResources()
                .getDimensionPixelSize(R.dimen.common_selection_elevation);
        ViewCompat.setElevation(mTopContainer, elevation);
    }

    public void hide() {
        mExpanded = false;
        mExpandedContainer.setVisibility(GONE);
        if (mMoreButton != null) {
            mMoreButton.setVisibility(INVISIBLE);
        }

        if (mTopContainer != null) {
            int color = ContextCompat.getColor(getContext(), R.color.common_grey50);
            mTopContainer.setBackgroundColor(color);

            ViewCompat.setElevation(mTopContainer, 0);
        }
    }

    void expand() {
        mExpanded = !mExpanded;
        if (mExpanded) {
            mSettingsCallback.focusOnSetting(BaseSetting.this);

            if (mTopContainer != null) {
                int color = ContextCompat.getColor(getContext(), R.color.common_white);
                mTopContainer.setBackgroundColor(color);
            }

            if (mMoreButton != null) {
                mMoreButton.setVisibility(VISIBLE);
            }
        } else {
            hide();
            mSettingsCallback.showTransparency(false);
        }
    }

    void replaceFirstViewInParent(ViewGroup parent, int layoutId) {
        // remove old one

        parent.removeViewAt(0);

        //add
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(layoutId, parent, false);
        parent.addView(view, 0);
    }
}

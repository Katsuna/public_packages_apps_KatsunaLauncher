package com.android.launcher3.pageindicators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.ViewConfiguration;

import com.android.launcher3.Utilities;
import com.android.launcher3.dynamicui.ExtractedColors;

import java.util.ArrayList;

/**
 * A PageIndicator that briefly shows a fraction of a line when moving between pages.
 *
 * The fraction is 1 / number of pages and the position is based on the progress of the page scroll.
 */
public class PageIndicatorLine extends View implements PageIndicator {
    private static final String TAG = "PageIndicatorLine";

    private static final int LINE_FADE_DURATION = ViewConfiguration.getScrollBarFadeDuration();
    private static final int LINE_FADE_DELAY = ViewConfiguration.getScrollDefaultDelay();
    public static final int WHITE_ALPHA = (int) (0.70f * 255);
    public static final int BLACK_ALPHA = (int) (0.65f * 255);

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private ValueAnimator mLineAlphaAnimator;
    private int mAlpha = 0;
    private float mProgress = 0f;
    private int mNumPages = 1;
    private Paint mLinePaint;

    private Property<Paint, Integer> mPaintAlphaProperty
            = new Property<Paint, Integer>(Integer.class, "paint_alpha") {
        @Override
        public Integer get(Paint paint) {
            return paint.getAlpha();
        }

        @Override
        public void set(Paint paint, Integer alpha) {
            paint.setAlpha(alpha);
            invalidate();
        }
    };

    private Runnable mHideLineRunnable = new Runnable() {
        @Override
        public void run() {
            animateLineToAlpha(0);
        }
    };

    public PageIndicatorLine(Context context) {
        this(context, null);
    }

    public PageIndicatorLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorLine(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLinePaint = new Paint();
        mLinePaint.setAlpha(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNumPages == 0) {
            return;
        }

        int availableWidth = canvas.getWidth();
        int lineWidth = availableWidth / mNumPages;
        int lineLeft = (int) (mProgress * (availableWidth - lineWidth));
        int lineRight = lineLeft + lineWidth;
        canvas.drawRect(lineLeft, 0, lineRight, canvas.getHeight(), mLinePaint);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setProgress(float progress) {
        if (getAlpha() == 0) {
            return;
        }
        progress = Utilities.boundToRange(progress, 0f, 1f);
        animateLineToAlpha(mAlpha);
        mProgress = progress;
        invalidate();

        // Hide after a brief period.
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(mHideLineRunnable, LINE_FADE_DELAY);
    }

    @Override
    public void removeAllMarkers(boolean allowAnimations) {
        mNumPages = 0;
    }

    @Override
    public void addMarkers(ArrayList<PageMarkerResources> markers, boolean allowAnimations) {
        mNumPages += markers.size();
    }

    @Override
    public void setActiveMarker(int activePage) {
    }

    @Override
    public void addMarker(int pageIndex, PageMarkerResources pageIndicatorMarker,
            boolean allowAnimations) {
        mNumPages++;
    }

    @Override
    public void removeMarker(int pageIndex, boolean allowAnimations) {
        mNumPages--;
    }

    @Override
    public void updateMarker(int pageIndex, PageMarkerResources pageIndicatorMarker) {
    }

    /**
     * The line's color will be:
     * - mostly opaque white if the hotseat is white (ignoring alpha)
     * - mostly opaque black if the hotseat is black (ignoring alpha)
     */
    public void updateColor(ExtractedColors extractedColors) {
        int originalLineAlpha = mLinePaint.getAlpha();
        int color = extractedColors.getColor(ExtractedColors.HOTSEAT_INDEX, Color.TRANSPARENT);
        if (color != Color.TRANSPARENT) {
            color = ColorUtils.setAlphaComponent(color, 255);
            if (color == Color.BLACK) {
                mAlpha = BLACK_ALPHA;
            } else if (color == Color.WHITE) {
                mAlpha = WHITE_ALPHA;
            } else {
                Log.e(TAG, "Setting workspace page indicators to an unsupported color: #"
                        + Integer.toHexString(color));
            }
            mLinePaint.setColor(color);
            mLinePaint.setAlpha(originalLineAlpha);
        }
    }

    private void animateLineToAlpha(int alpha) {
        if (mLineAlphaAnimator != null) {
            // An animation is already running, so ignore the new animation request unless we are
            // trying to hide the line, in which case we always allow the animation.
            if (alpha != 0) {
                return;
            }
            mLineAlphaAnimator.cancel();
        }
        mLineAlphaAnimator = ObjectAnimator.ofInt(mLinePaint, mPaintAlphaProperty, alpha);
        mLineAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLineAlphaAnimator = null;
            }
        });
        mLineAlphaAnimator.setDuration(LINE_FADE_DURATION);
        mLineAlphaAnimator.start();
    }
}
/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.allapps;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.launcher3.R;

public class FloatingHeaderView extends RelativeLayout implements
        ValueAnimator.AnimatorUpdateListener {

    private static final boolean SHOW_PREDICTIONS_ONLY_ON_TOP = true;

    private final Rect mClip = new Rect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final ValueAnimator mAnimator = ValueAnimator.ofInt(0, 0);
    private final Point mTempOffset = new Point();
    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (SHOW_PREDICTIONS_ONLY_ON_TOP) {
                return;
            }
            if (!mTopOnlyMode && newState == RecyclerView.SCROLL_STATE_IDLE
                    && mTranslationY != -mMaxTranslation && mTranslationY != 0) {
                float scroll = Math.abs(getCurrentScroll());
                boolean expand =  scroll > mMaxTranslation
                        ? Math.abs(mTranslationY) < mMaxTranslation / 2 : true;
                setExpanded(expand);
            }
        }

        @Override
        public void onScrolled(RecyclerView rv, int dx, int dy) {
            boolean isMainRV = rv == mMainRV;
            if (isMainRV != mMainRVActive) {
                return;
            }

            if (mAnimator.isStarted()) {
                mAnimator.cancel();
            }

            int current = - (isMainRV
                    ? mMainRV.getCurrentScrollY()
                    : mWorkRV.getCurrentScrollY());
            moved(current);
            apply();
        }
    };

    private PredictionRowView mPredictionRow;
    private ViewGroup mTabLayout;
    private View mDivider;
    private AllAppsRecyclerView mMainRV;
    private AllAppsRecyclerView mWorkRV;
    private ViewGroup mParent;
    private boolean mTopOnlyMode;
    private boolean mHeaderHidden;
    private int mMaxTranslation;
    private int mSnappedScrolledY;
    private int mTranslationY;
    private int mMainScrolledY;
    private int mWorkScrolledY;
    private boolean mMainRVActive = true;
    private boolean mForwardToRecyclerView;

    public FloatingHeaderView(@NonNull Context context) {
        this(context, null);
    }

    public FloatingHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTabLayout = findViewById(R.id.tabs);
        mDivider = findViewById(R.id.divider);
        mPredictionRow = findViewById(R.id.header_content);
    }

    public void setup(@NonNull AllAppsRecyclerView personalRV, @Nullable AllAppsRecyclerView workRV,
                      int predictionRowHeight) {
        mTopOnlyMode = workRV == null;
        mTabLayout.setVisibility(mTopOnlyMode ? View.GONE : View.VISIBLE);
        mPredictionRow.getLayoutParams().height = predictionRowHeight;
        mMaxTranslation = predictionRowHeight;
        mMainRV = setupRV(mMainRV, personalRV);
        mWorkRV = setupRV(mWorkRV, workRV);
        mParent = (ViewGroup) getRV().getParent();
        setMainActive(true);
        setupDivider();
    }

    private AllAppsRecyclerView setupRV(AllAppsRecyclerView old, AllAppsRecyclerView updated) {
        if (old != updated && updated != null ) {
            updated.addOnScrollListener(mOnScrollListener);
        }
        return updated;
    }

    private void setupDivider() {
        Resources res = getResources();
        int verticalGap = res.getDimensionPixelSize(R.dimen.all_apps_divider_margin_vertical);
        int sideGap = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        mDivider.setPadding(sideGap, verticalGap,sideGap, mTopOnlyMode ? verticalGap : 0);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mDivider.getLayoutParams();
        lp.removeRule(RelativeLayout.ALIGN_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_BOTTOM, mTopOnlyMode ? R.id.header_content : R.id.tabs);
        mDivider.setLayoutParams(lp);
    }

    public void setMainActive(boolean active) {
        mMainRVActive = active;
        mSnappedScrolledY = getCurrentScroll() - mMaxTranslation;
        setExpanded(true);
    }

    public PredictionRowView getPredictionRow() {
        return mPredictionRow;
    }

    public ViewGroup getTabLayout() {
        return mTabLayout;
    }

    public View getDivider() {
        return mDivider;
    }

    public void reset() {
        mMainScrolledY = 0;
        mWorkScrolledY = 0;
        setExpanded(true);
    }

    private boolean canSnapAt(int currentScrollY) {
        boolean snapOnlyOnTop = SHOW_PREDICTIONS_ONLY_ON_TOP || mTopOnlyMode;
        return !snapOnlyOnTop || Math.abs(currentScrollY) <= mPredictionRow.getHeight();
    }

    private void moved(final int currentScrollY) {
        if (mHeaderHidden) {
            if (currentScrollY <= mSnappedScrolledY) {
                if (canSnapAt(currentScrollY)) {
                    mSnappedScrolledY = currentScrollY;
                }
            } else {
                mHeaderHidden = false;
            }
            mTranslationY = currentScrollY;
        } else if (!mHeaderHidden) {
            mTranslationY = currentScrollY - mSnappedScrolledY - mMaxTranslation;

            // update state vars
            if (mTranslationY >= 0) { // expanded: must not move down further
                mTranslationY = 0;
                mSnappedScrolledY = currentScrollY - mMaxTranslation;
            } else if (mTranslationY <= -mMaxTranslation) { // hide or stay hidden
                mHeaderHidden = true;
                mSnappedScrolledY = currentScrollY;
            }
        }
    }

    private void apply() {
        int uncappedTranslationY = mTranslationY;
        mTranslationY = Math.max(mTranslationY, -mMaxTranslation);
        if (mTranslationY != uncappedTranslationY) {
            // we hide it completely if already capped (for opening search anim)
            mPredictionRow.setVisibility(View.INVISIBLE);
        } else {
            mPredictionRow.setVisibility(View.VISIBLE);
            mPredictionRow.setTranslationY(uncappedTranslationY);
        }
        mTabLayout.setTranslationY(mTranslationY);
        mDivider.setTranslationY(mTopOnlyMode ? uncappedTranslationY : mTranslationY);
        mClip.top = mMaxTranslation + mTranslationY;
        // clipping on a draw might cause additional redraw
        mMainRV.setClipBounds(mClip);
        if (mWorkRV != null) {
            mWorkRV.setClipBounds(mClip);
        }
    }

    private void setExpanded(boolean expand) {
        int translateTo = expand ? 0 : -mMaxTranslation;
        mAnimator.setIntValues(mTranslationY, translateTo);
        mAnimator.addUpdateListener(this);
        mAnimator.setDuration(150);
        mAnimator.start();
        mHeaderHidden = !expand;
        mSnappedScrolledY = expand ? getCurrentScroll() - mMaxTranslation : getCurrentScroll();
    }

    public boolean isExpanded() {
        return !mHeaderHidden;
    }

    private int getCurrentScroll() {
        return mMainRVActive ? mMainScrolledY : mWorkScrolledY;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mTranslationY = (Integer) animation.getAnimatedValue();
        apply();
    }

    private AllAppsRecyclerView getRV() {
        return mMainRVActive ? mMainRV : mWorkRV;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        calcOffset(mTempOffset);
        ev.offsetLocation(mTempOffset.x, mTempOffset.y);
        mForwardToRecyclerView = getRV().onInterceptTouchEvent(ev);
        ev.offsetLocation(-mTempOffset.x, -mTempOffset.y);
        return mForwardToRecyclerView || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mForwardToRecyclerView) {
            // take this view's and parent view's (view pager) location into account
            calcOffset(mTempOffset);
            event.offsetLocation(mTempOffset.x, mTempOffset.y);
            try {
                return getRV().onTouchEvent(event);
            } finally {
                event.offsetLocation(-mTempOffset.x, -mTempOffset.y);
            }
        } else {
            return super.onTouchEvent(event);
        }
    }

    private void calcOffset(Point p) {
        p.x = getLeft() - getRV().getLeft() - mParent.getLeft();
        p.y = getTop() - getRV().getTop() - mParent.getTop();
    }

}



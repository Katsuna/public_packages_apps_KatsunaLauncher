/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.katsuna.launcher.allapps;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Process;
import android.support.animation.DynamicAnimation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.katsuna.commons.KatsunaIntents;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.KatsunaAlertBuilder;
import com.katsuna.commons.utils.KatsunaUtils;
import com.katsuna.launcher.AppInfo;
import com.katsuna.launcher.DeviceProfile;
import com.katsuna.launcher.DeviceProfile.OnDeviceProfileChangeListener;
import com.katsuna.launcher.DragSource;
import com.katsuna.launcher.DropTarget.DragObject;
import com.katsuna.launcher.Insettable;
import com.katsuna.launcher.InsettableFrameLayout;
import com.katsuna.launcher.ItemInfo;
import com.katsuna.launcher.Launcher;
import com.katsuna.launcher.R;
import com.katsuna.launcher.Utilities;
import com.katsuna.launcher.config.FeatureFlags;
import com.katsuna.launcher.katsuna.AppInteraction;
import com.katsuna.launcher.katsuna.AppsGroupsPopulator;
import com.katsuna.launcher.katsuna.UsabilitySettingsActivity;
import com.katsuna.launcher.keyboard.FocusedItemDecorator;
import com.katsuna.launcher.model.KatsunaAppComparator;
import com.katsuna.launcher.userevent.nano.LauncherLogProto.Target;
import com.katsuna.launcher.util.ItemInfoMatcher;
import com.katsuna.launcher.util.Themes;
import com.katsuna.launcher.views.BottomUserEducationView;
import com.katsuna.launcher.views.RecyclerViewFastScroller;
import com.katsuna.launcher.views.SpringRelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * The all apps view container.
 */
public class AllAppsContainerView extends SpringRelativeLayout implements DragSource,
        Insettable, OnDeviceProfileChangeListener,
        AppInteraction {

    private static final String TAG = "AllAppsContainerView";
    private static final float FLING_VELOCITY_MULTIPLIER = 135f;
    // Starts the springs after at least 55% of the animation has passed.
    private static final float FLING_ANIMATION_THRESHOLD = 0.55f;

    private final Launcher mLauncher;
    private final AdapterHolder[] mAH;
    private final ItemInfoMatcher mPersonalMatcher = ItemInfoMatcher.ofUser(Process.myUserHandle());
    private final ItemInfoMatcher mWorkMatcher = ItemInfoMatcher.not(mPersonalMatcher);
    private final AllAppsStore mAllAppsStore = new AllAppsStore();

    private final Paint mNavBarScrimPaint;
    private int mNavBarScrimHeight = 0;

    private SearchUiManager mSearchUiManager;
    private View mSearchContainer;
    private AllAppsPagedView mViewPager;
    private FloatingHeaderView mHeader;

    private SpannableStringBuilder mSearchQueryBuilder = null;

    private boolean mUsingTabs;
    private boolean mSearchModeWhileUsingTabs = false;

    private RecyclerViewFastScroller mTouchHandler;
    private final Point mFastScrollerOffset = new Point();

    public AllAppsContainerView(Context context) {
        this(context, null);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLauncher = Launcher.getLauncher(context);
        mLauncher.addOnDeviceProfileChangeListener(this);

        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);

        mAH = new AdapterHolder[2];
        mAH[AdapterHolder.MAIN] = new AdapterHolder(false /* isWork */, this);
        mAH[AdapterHolder.WORK] = new AdapterHolder(true /* isWork */, this);

        mNavBarScrimPaint = new Paint();
        mNavBarScrimPaint.setColor(Themes.getAttrColor(context, R.attr.allAppsNavBarScrimColor));

        mAllAppsStore.addUpdateListener(this::onAppsUpdated);

        addSpringView(R.id.all_apps_header);
        addSpringView(R.id.apps_list_view);
        addSpringView(R.id.all_apps_tabs_view_pager);
    }

    public AllAppsStore getAppsStore() {
        return mAllAppsStore;
    }

    @Override
    protected void setDampedScrollShift(float shift) {
        // Bound the shift amount to avoid content from drawing on top (Y-val) of the QSB.
        float maxShift = getSearchView().getHeight() / 2f;
        super.setDampedScrollShift(Utilities.boundToRange(shift, -maxShift, maxShift));
    }

    @Override
    public void onDeviceProfileChanged(DeviceProfile dp) {
        for (AdapterHolder holder : mAH) {
            if (holder.recyclerView != null) {
                // Remove all views and clear the pool, while keeping the data same. After this
                // call, all the viewHolders will be recreated.
                holder.recyclerView.swapAdapter(holder.recyclerView.getAdapter(), true);
                holder.recyclerView.getRecycledViewPool().clear();
            }
        }
    }

    private void onAppsUpdated() {
        if (FeatureFlags.ALL_APPS_TABS_ENABLED) {
            boolean hasWorkApps = false;
            for (AppInfo app : mAllAppsStore.getApps()) {
                if (mWorkMatcher.matches(app, null)) {
                    hasWorkApps = true;
                    break;
                }
            }
            rebindAdapters(hasWorkApps);
        }
        sortApps();
    }

    // set to true to disable swipe when apps drawer is not scrolled down.
    private boolean mSwipeOverride = true;

    /**
     * Returns whether the view itself will handle the touch event or not.
     */
    public boolean shouldContainerScroll(MotionEvent ev) {
        if (mSwipeOverride) return false;

        // IF the MotionEvent is inside the search box, and the container keeps on receiving
        // touch input, container should move down.
        if (mLauncher.getDragLayer().isEventOverView(mSearchContainer, ev)) {
            return true;
        }
        AllAppsRecyclerView rv = getActiveRecyclerView();
        if (rv == null) {
            return true;
        }
        if (rv.getScrollbar().getThumbOffsetY() >= 0 &&
                mLauncher.getDragLayer().isEventOverView(rv.getScrollbar(), ev)) {
            return false;
        }
        return rv.shouldContainerScroll(ev, mLauncher.getDragLayer());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            AllAppsRecyclerView rv = getActiveRecyclerView();
            if (rv != null &&
                    rv.getScrollbar().isHitInParent(ev.getX(), ev.getY(), mFastScrollerOffset)) {
                mTouchHandler = rv.getScrollbar();
            }
        }
        if (mTouchHandler != null) {
            return mTouchHandler.handleTouchEvent(ev, mFastScrollerOffset);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mTouchHandler != null) {
            mTouchHandler.handleTouchEvent(ev, mFastScrollerOffset);
            return true;
        }
        return false;
    }

    public String getDescription() {
        @StringRes int descriptionRes;
        if (mUsingTabs) {
            descriptionRes =
                    mViewPager.getNextPage() == 0
                            ? R.string.all_apps_button_personal_label
                            : R.string.all_apps_button_work_label;
        } else {
            descriptionRes = R.string.all_apps_button_label;
        }
        return getContext().getString(descriptionRes);
    }

    public AllAppsRecyclerView getActiveRecyclerView() {
        if (!mUsingTabs || mViewPager.getNextPage() == 0) {
            return mAH[AdapterHolder.MAIN].recyclerView;
        } else {
            return mAH[AdapterHolder.WORK].recyclerView;
        }
    }

    /**
     * Resets the state of AllApps.
     */
    public void reset(boolean animate) {
        for (int i = 0; i < mAH.length; i++) {
            if (mAH[i].recyclerView != null) {
                mAH[i].recyclerView.scrollToTop();
            }
        }
        if (isHeaderVisible()) {
            mHeader.reset(animate);
        }
        // Reset the search bar and base recycler view after transitioning home
        mSearchUiManager.resetSearch();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // This is a focus listener that proxies focus from a view into the list view.  This is to
        // work around the search box from getting first focus and showing the cursor.
        setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && getActiveRecyclerView() != null) {
                getActiveRecyclerView().requestFocus();
            }
        });

        mHeader = findViewById(R.id.all_apps_header);
        rebindAdapters(mUsingTabs, true /* force */);

        mSearchContainer = findViewById(R.id.search_container_all_apps);
        mSearchUiManager = (SearchUiManager) mSearchContainer;
        mSearchUiManager.initialize(this);

        mMoreOptions = findViewById(R.id.more_options);
        mMoreOptions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPopupWindow(mMoreOptions);
            }
        });
    }

    public SearchUiManager getSearchUiManager() {
        return mSearchUiManager;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        mSearchUiManager.preDispatchKeyEvent(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean success) { }

    @Override
    public void fillInLogContainerData(View v, ItemInfo info, Target target, Target targetParent) {
        // This is filled in {@link AllAppsRecyclerView}
    }

    @Override
    public void setInsets(Rect insets) {
        int valueInPixels = (int) getResources().getDimension(R.dimen.all_apps_bottom_inset);


        for (int i = 0; i < mAH.length; i++) {
            mAH[i].padding.bottom = valueInPixels;
            mAH[i].applyPadding();
        }

        mNavBarScrimHeight = valueInPixels;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mNavBarScrimHeight > 0) {
            canvas.drawRect(0, getHeight() - mNavBarScrimHeight, getWidth(), getHeight(),
                    mNavBarScrimPaint);
        }
    }

    private void rebindAdapters(boolean showTabs) {
        rebindAdapters(showTabs, false /* force */);
    }

    private void rebindAdapters(boolean showTabs, boolean force) {
        if (showTabs == mUsingTabs && !force) {
            return;
        }
        replaceRVContainer(showTabs);
        mUsingTabs = showTabs;

        mAllAppsStore.unregisterIconContainer(mAH[AdapterHolder.MAIN].recyclerView);
        mAllAppsStore.unregisterIconContainer(mAH[AdapterHolder.WORK].recyclerView);

        if (mUsingTabs) {
            mAH[AdapterHolder.MAIN].setup(mViewPager.getChildAt(0), mPersonalMatcher);
            mAH[AdapterHolder.WORK].setup(mViewPager.getChildAt(1), mWorkMatcher);
            onTabChanged(mViewPager.getNextPage());
        } else {
            mAH[AdapterHolder.MAIN].setup(findViewById(R.id.apps_list_view), null);
            mAH[AdapterHolder.WORK].recyclerView = null;
        }
        setupHeader();

        mAllAppsStore.registerIconContainer(mAH[AdapterHolder.MAIN].recyclerView);
        mAllAppsStore.registerIconContainer(mAH[AdapterHolder.WORK].recyclerView);
    }

    private void replaceRVContainer(boolean showTabs) {
        for (int i = 0; i < mAH.length; i++) {
            if (mAH[i].recyclerView != null) {
                mAH[i].recyclerView.setLayoutManager(null);
            }
        }
        View oldView = getRecyclerViewContainer();
        int index = indexOfChild(oldView);
        removeView(oldView);
        int layout = showTabs ? R.layout.all_apps_tabs : R.layout.all_apps_rv_layout;
        View newView = LayoutInflater.from(getContext()).inflate(layout, this, false);
        addView(newView, index);
        if (showTabs) {
            mViewPager = (AllAppsPagedView) newView;
            mViewPager.initParentViews(this);
            mViewPager.getPageIndicator().setContainerView(this);
        } else {
            mViewPager = null;
        }
    }

    public View getRecyclerViewContainer() {
        return mViewPager != null ? mViewPager : findViewById(R.id.apps_list_view);
    }

    public void onTabChanged(int pos) {
        mHeader.setMainActive(pos == 0);
        reset(true /* animate */);
        if (mAH[pos].recyclerView != null) {
            mAH[pos].recyclerView.bindFastScrollbar();

            findViewById(R.id.tab_personal)
                    .setOnClickListener((View view) -> mViewPager.snapToPage(AdapterHolder.MAIN));
            findViewById(R.id.tab_work)
                    .setOnClickListener((View view) -> mViewPager.snapToPage(AdapterHolder.WORK));

        }
        if (pos == AdapterHolder.WORK) {
            BottomUserEducationView.showIfNeeded(mLauncher);
        }
    }

    public AlphabeticalAppsList getApps() {
        return mAH[AdapterHolder.MAIN].appsList;
    }

    public FloatingHeaderView getFloatingHeaderView() {
        return mHeader;
    }

    public View getSearchView() {
        return mSearchContainer;
    }

    public View getContentView() {
        return mViewPager == null ? getActiveRecyclerView() : mViewPager;
    }

    public RecyclerViewFastScroller getScrollBar() {
        AllAppsRecyclerView rv = getActiveRecyclerView();
        return rv == null ? null : rv.getScrollbar();
    }

    public void setupHeader() {
        mHeader.setVisibility(View.VISIBLE);
        mHeader.setup(mAH, mAH[AllAppsContainerView.AdapterHolder.WORK].recyclerView == null);

        int padding = mHeader.getMaxTranslation();
        for (int i = 0; i < mAH.length; i++) {
            mAH[i].padding.top = padding;
            mAH[i].applyPadding();
        }
    }

    public void setLastSearchQuery(String query) {
        mLauncher.showFabToolbar(false);

        for (int i = 0; i < mAH.length; i++) {
            mAH[i].adapter.setLastSearchQuery(query);
        }
        if (mUsingTabs) {
            mSearchModeWhileUsingTabs = true;
            rebindAdapters(false); // hide tabs
        }
    }

    public void onClearSearchResult() {
        if (mSearchModeWhileUsingTabs) {
            rebindAdapters(true); // show tabs
            mSearchModeWhileUsingTabs = false;
        }
    }

    public void onSearchResultsChanged() {
        for (int i = 0; i < mAH.length; i++) {
            if (mAH[i].recyclerView != null) {
                mAH[i].recyclerView.onSearchResultsChanged();
            }
        }
    }

    public void setRecyclerViewVerticalFadingEdgeEnabled(boolean enabled) {
        for (int i = 0; i < mAH.length; i++) {
            mAH[i].applyVerticalFadingEdgeEnabled(enabled);
        }
    }

    public void addElevationController(RecyclerView.OnScrollListener scrollListener) {
        if (!mUsingTabs) {
            mAH[AdapterHolder.MAIN].recyclerView.addOnScrollListener(scrollListener);
        }
    }

    public boolean isHeaderVisible() {
        return mHeader != null && mHeader.getVisibility() == View.VISIBLE;
    }

    public void onScrollUpEnd() {
        if (mUsingTabs) {
            ((PersonalWorkSlidingTabStrip) findViewById(R.id.tabs)).highlightWorkTabIfNecessary();
        }
    }

    /**
     * Adds an update listener to {@param animator} that adds springs to the animation.
     */
    public void addSpringFromFlingUpdateListener(ValueAnimator animator, float velocity) {
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            boolean shouldSpring = true;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (shouldSpring
                        && valueAnimator.getAnimatedFraction() >= FLING_ANIMATION_THRESHOLD) {
                    int searchViewId = getSearchView().getId();
                    addSpringView(searchViewId);

                    finishWithShiftAndVelocity(1, velocity * FLING_VELOCITY_MULTIPLIER,
                            new DynamicAnimation.OnAnimationEndListener() {
                                @Override
                                public void onAnimationEnd(DynamicAnimation animation,
                                        boolean canceled, float value, float velocity) {
                                    removeSpringView(searchViewId);
                                }
                            });

                    shouldSpring = false;
                }
            }
        });
    }

    @Override
    public void selectAppsGroup(int position) {
        if (mLauncher.selectItem()) {
            AllAppsGridAdapter adapter = getMainAdapter();
            adapter.setSelectedAppsGroup(position);
            scrollToPositionWithOffset(position, 0);
        }
    }

    @Override
    public void deselectAppsGroup() {
        AllAppsGridAdapter adapter = getMainAdapter();
        adapter.setSelectedAppsGroup(AllAppsGridAdapter.NO_APPS_GROUP_POSITION);
        invalidateApps();
        mLauncher.deselectItem();
    }

    @Override
    public UserProfile getUserProfile() {
        return mLauncher.getUserProfile();
    }

    private AlertDialog mLatestDialog;

    @Override
    public void uninstall(String packageName) {
        try {
            Context context = getContext();
            // Search for system apps with this packageName.
            ApplicationInfo app = context.getPackageManager()
                    .getApplicationInfo(packageName, 0);

            if (isUserApp(app)) {
                // Not a system app. We can try uninstalling it.
                Uri packageUri = Uri.parse("package:" + packageName);
                Intent i = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                context.startActivity(i);

                mLauncher.getUsageStatistics();
                sortApps();
            } else {
                KatsunaAlertBuilder builder = new KatsunaAlertBuilder(context);
                String title = context.getResources().getString(R.string.common_warning);
                builder.setTitle(title);
                String message = context.getResources().getString(R.string.common_system_app_uninstall_error);
                builder.setMessage(message);
                builder.setView(R.layout.common_katsuna_alert);
                builder.setUserProfile(getUserProfile());
                builder.setCancelHidden(true);
                mLatestDialog = builder.create();
                mLatestDialog.show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }
    }

    private boolean isUserApp(ApplicationInfo ai) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (ai.flags & mask) == 0;
    }

    public void enableDeleteMode(boolean flag) {
        AllAppsGridAdapter adapter = getMainAdapter();
        adapter.enableDeleteMode(flag);
        invalidateApps();
    }

    private void scrollToPositionWithOffset(int position, int offset) {
        AllAppsRecyclerView rv = mAH[AdapterHolder.MAIN].recyclerView;
        LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();

        lm.scrollToPositionWithOffset(position, offset);
    }

    public class AdapterHolder {
        public static final int MAIN = 0;
        public static final int WORK = 1;

        public final AllAppsGridAdapter adapter;
        final LinearLayoutManager layoutManager;
        final AlphabeticalAppsList appsList;
        final Rect padding = new Rect();
        AllAppsRecyclerView recyclerView;
        boolean verticalFadingEdge;

        AdapterHolder(boolean isWork, AppInteraction appInteraction) {
            appsList = new AlphabeticalAppsList(mLauncher, mAllAppsStore, isWork);
            adapter = new AllAppsGridAdapter(mLauncher, appsList, appInteraction);
            appsList.setAdapter(adapter);
            layoutManager = adapter.getLayoutManager();
        }

        void setup(@NonNull View rv, @Nullable ItemInfoMatcher matcher) {
            appsList.updateItemFilter(matcher);
            recyclerView = (AllAppsRecyclerView) rv;
            recyclerView.setEdgeEffectFactory(createEdgeEffectFactory());
            recyclerView.setApps(appsList, mUsingTabs);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
            // No animations will occur when changes occur to the items in this RecyclerView.
            recyclerView.setItemAnimator(null);
            FocusedItemDecorator focusedItemDecorator = new FocusedItemDecorator(recyclerView);
            recyclerView.addItemDecoration(focusedItemDecorator);
            adapter.setIconFocusListener(focusedItemDecorator.getFocusListener());
            applyVerticalFadingEdgeEnabled(verticalFadingEdge);
            applyPadding();
        }

        void applyPadding() {
            if (recyclerView != null) {
                recyclerView.setPadding(padding.left, padding.top, padding.right, padding.bottom);
            }
        }

        public void applyVerticalFadingEdgeEnabled(boolean enabled) {
            verticalFadingEdge = enabled;
            mAH[AdapterHolder.MAIN].recyclerView.setVerticalFadingEdgeEnabled(!mUsingTabs
                    && verticalFadingEdge);
        }
    }

    private AllAppsGridAdapter getMainAdapter() {
        return mAH[AdapterHolder.MAIN].adapter;
    }

    private void sortApps() {
        //sort apps by launch count
        AlphabeticalAppsList appsList = getApps();

        List<AppInfo> topApps = new ArrayList<>();
        for (AppInfo appInfo : appsList.getApps()) {
            topApps.add(new AppInfo(appInfo));
        }
        Collections.sort(topApps, new KatsunaAppComparator(getContext()).getAppInfoComparator());

        mLauncher.loadFabToolbar(appsList.getAdapterItems());
    }

    public void selectItemByStartingLetter(String letter) {
        AllAppsGridAdapter adapter = getMainAdapter();
        if (adapter != null) {
            mLauncher.deselectItem();
            int position = adapter.getPositionByStartingLetter(letter);
            selectAppsGroup(position);
        }
    }

    public void clearFocusFromSearch() {
        if (mSearchUiManager != null) {
            mSearchUiManager.resetSearch();
        }
    }

    public void unfocusFromSearch() {
        AllAppsGridAdapter adapter = getMainAdapter();
        adapter.deselectAppsGroup();
    }

    public void invalidateApps() {
        AllAppsGridAdapter adapter = getMainAdapter();
        adapter.invalidate();
    }

    public void refreshApps() {
        AlphabeticalAppsList appsList = getApps();
        appsList.refreshApps();
    }

    private PopupWindow mPopupMoreOptions;
    private View mMoreOptions;

    private void displayPopupWindow(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater == null) return;

        mPopupMoreOptions = new PopupWindow(this);

        mPopupMoreOptions.setWidth(WRAP_CONTENT);
        mPopupMoreOptions.setHeight(WRAP_CONTENT);

        View layout = inflater.inflate(R.layout.apps_actions_menu, null);

        TextView settings = layout.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (KatsunaUtils.katsunaOsDetected()) {
                    Intent i = new Intent(KatsunaIntents.SETTINGS);
                    getContext().startActivity(i);
                } else {
                    Intent i = new Intent(getContext(), UsabilitySettingsActivity.class);
                    getContext().startActivity(i);
                }

                mPopupMoreOptions.dismiss();
            }
        });

        TextView deleteCallsItem = layout.findViewById(R.id.delete_apps_menu_item);
        deleteCallsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLauncher.enableDeleteMode(true);
                mPopupMoreOptions.dismiss();
            }
        });

        mPopupMoreOptions.setContentView(layout);

        // Closes the popup window when touch outside of it - when looses focus
        mPopupMoreOptions.setOutsideTouchable(true);
        mPopupMoreOptions.setFocusable(true);
        mPopupMoreOptions.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int margin4 = getResources().getDimensionPixelSize(R.dimen.common_4dp);
        int margin56 = getResources().getDimensionPixelSize(R.dimen.common_56dp);

        mPopupMoreOptions.showAtLocation(anchorView, Gravity.TOP | Gravity.END, margin4, margin56);
    }

    public void hidePopupMoreOptions() {
        if (mPopupMoreOptions != null) {
            mPopupMoreOptions.dismiss();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            // refresh usage stats
            mLauncher.getUsageStatistics();

            // load apps list with latest usage
            List<AppInfo> appInfos = getApps().getApps();
            AppsGroupsPopulator pp = new AppsGroupsPopulator(getContext(), mLauncher, appInfos);
            pp.calcLauncCounts();

            // reload apps
            refreshApps();
            sortApps();
        } else {
            hidePendingDialog();
        }
    }

    public void hidePendingDialog() {
        if (mLatestDialog != null) {
            mLatestDialog.dismiss();
        }

    }

}

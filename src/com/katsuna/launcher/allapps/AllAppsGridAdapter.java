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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import com.katsuna.launcher.AppInfo;
import com.katsuna.launcher.BubbleTextView;
import com.katsuna.launcher.Launcher;
import com.katsuna.launcher.R;
import com.katsuna.launcher.allapps.AlphabeticalAppsList.AdapterItem;
import com.katsuna.launcher.compat.UserManagerCompat;
import com.katsuna.launcher.katsuna.AppInteraction;
import com.katsuna.launcher.katsuna.AppsGroup;
import com.katsuna.launcher.katsuna.viewholders.AppsGroupViewHolder;
import com.katsuna.launcher.touch.ItemClickHandler;
import com.katsuna.launcher.touch.ItemLongClickListener;
import com.katsuna.launcher.util.PackageManagerHelper;

import java.util.List;

/**
 * The grid view adapter of all the apps.
 */
public class AllAppsGridAdapter extends RecyclerView.Adapter<AllAppsGridAdapter.ViewHolder> {

    public static final String TAG = "AppsGridAdapter";

    // A normal icon
    public static final int VIEW_TYPE_ICON = 1 << 1;
    // The message shown when there are no filtered results
    public static final int VIEW_TYPE_EMPTY_SEARCH = 1 << 2;
    // The message to continue to a market search when there are no filtered results
    public static final int VIEW_TYPE_SEARCH_MARKET = 1 << 3;

    // We use various dividers for various purposes.  They share enough attributes to reuse layouts,
    // but differ in enough attributes to require different view types

    // A divider that separates the apps list and the search market button
    public static final int VIEW_TYPE_ALL_APPS_DIVIDER = 1 << 4;
    public static final int VIEW_TYPE_WORK_TAB_FOOTER = 1 << 5;

    // Common view type masks
    public static final int VIEW_TYPE_MASK_DIVIDER = VIEW_TYPE_ALL_APPS_DIVIDER;
    public static final int VIEW_TYPE_MASK_ICON = VIEW_TYPE_ICON;


    public interface BindViewCallback {
        void onBindView(ViewHolder holder);
    }

    /**
     * ViewHolder for each icon.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mContent;

        public ViewHolder(View v) {
            super(v);
            mContent = v;
        }
    }

    /**
     * A subclass of GridLayoutManager that overrides accessibility values during app search.
     */
    public class AppsGridLayoutManager extends GridLayoutManager {

        public AppsGridLayoutManager(Context context) {
            super(context, 1, GridLayoutManager.VERTICAL, false);
        }

        @Override
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);

            // Ensure that we only report the number apps for accessibility not including other
            // adapter views
            final AccessibilityRecordCompat record = AccessibilityEventCompat
                    .asRecord(event);
            record.setItemCount(mApps.getNumFilteredApps());
            record.setFromIndex(Math.max(0,
                    record.getFromIndex() - getRowsNotForAccessibility(record.getFromIndex())));
            record.setToIndex(Math.max(0,
                    record.getToIndex() - getRowsNotForAccessibility(record.getToIndex())));
        }

        @Override
        public int getRowCountForAccessibility(RecyclerView.Recycler recycler,
                RecyclerView.State state) {
            return super.getRowCountForAccessibility(recycler, state) -
                    getRowsNotForAccessibility(mApps.getAdapterItems().size() - 1);
        }

        @Override
        public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler,
                RecyclerView.State state, View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfoForItem(recycler, state, host, info);

            ViewGroup.LayoutParams lp = host.getLayoutParams();
            AccessibilityNodeInfoCompat.CollectionItemInfoCompat cic = info.getCollectionItemInfo();
            if (!(lp instanceof LayoutParams) || (cic == null)) {
                return;
            }
            LayoutParams glp = (LayoutParams) lp;
            info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(
                    cic.getRowIndex() - getRowsNotForAccessibility(glp.getViewAdapterPosition()),
                    cic.getRowSpan(),
                    cic.getColumnIndex(),
                    cic.getColumnSpan(),
                    cic.isHeading(),
                    cic.isSelected()));
        }

        /**
         * Returns the number of rows before {@param adapterPosition}, including this position
         * which should not be counted towards the collection info.
         */
        private int getRowsNotForAccessibility(int adapterPosition) {
            List<AdapterItem> items = mApps.getAdapterItems();
            adapterPosition = Math.max(adapterPosition, mApps.getAdapterItems().size() - 1);
            int extraRows = 0;
            for (int i = 0; i <= adapterPosition; i++) {
                if (!isViewType(items.get(i).viewType, VIEW_TYPE_MASK_ICON)) {
                    extraRows++;
                }
            }
            return extraRows;
        }
    }

    /**
     * Helper class to size the grid items.
     */
    public class GridSpanSizer extends GridLayoutManager.SpanSizeLookup {

        public GridSpanSizer() {
            super();
            setSpanIndexCacheEnabled(true);
        }

        @Override
        public int getSpanSize(int position) {
            return 1;
        }
    }

    private final Launcher mLauncher;
    private final LayoutInflater mLayoutInflater;
    private final AlphabeticalAppsList mApps;
    private final GridLayoutManager mGridLayoutMgr;
    private final GridSpanSizer mGridSizer;

    private final int mAppsPerRow;

    private BindViewCallback mBindViewCallback;
    private OnFocusChangeListener mIconFocusListener;

    public static final int NO_APPS_GROUP_POSITION = -1;
    private int mSelectedAppsGroupPosition = NO_APPS_GROUP_POSITION;
    private boolean mDeleteMode;
    private AppInteraction mAppInteraction;

    // The text to show when there are no search results and no market search handler.
    private String mEmptySearchMessage;
    // The intent to send off to the market app, updated each time the search query changes.
    private Intent mMarketSearchIntent;

    public AllAppsGridAdapter(Launcher launcher, AlphabeticalAppsList apps,
                              AppInteraction appInteraction) {
        Resources res = launcher.getResources();
        mLauncher = launcher;
        mApps = apps;
        mEmptySearchMessage = res.getString(R.string.all_apps_loading_message);
        mGridSizer = new GridSpanSizer();
        mGridLayoutMgr = new AppsGridLayoutManager(launcher);
        mGridLayoutMgr.setSpanSizeLookup(mGridSizer);
        mLayoutInflater = LayoutInflater.from(launcher);

        mAppsPerRow = 1;
        // mAppsPerRow = mLauncher.getDeviceProfile().inv.numColumns;
        mGridLayoutMgr.setSpanCount(mAppsPerRow);

        mAppInteraction = appInteraction;
    }

    public static boolean isDividerViewType(int viewType) {
        return isViewType(viewType, VIEW_TYPE_MASK_DIVIDER);
    }

    public static boolean isIconViewType(int viewType) {
        return isViewType(viewType, VIEW_TYPE_MASK_ICON);
    }

    public static boolean isViewType(int viewType, int viewTypeMask) {
        return (viewType & viewTypeMask) != 0;
    }

    public void setIconFocusListener(OnFocusChangeListener focusListener) {
        mIconFocusListener = focusListener;
    }

    /**
     * Sets the last search query that was made, used to show when there are no results and to also
     * seed the intent for searching the market.
     */
    public void setLastSearchQuery(String query) {
        Resources res = mLauncher.getResources();
        mEmptySearchMessage = res.getString(R.string.all_apps_no_search_results, query);
        mMarketSearchIntent = PackageManagerHelper.getMarketSearchIntent(mLauncher, query);
    }

    /**
     * Sets the callback for when views are bound.
     */
    public void setBindViewCallback(BindViewCallback cb) {
        mBindViewCallback = cb;
    }

    /**
     * Returns the grid layout manager.
     */
    public GridLayoutManager getLayoutManager() {
        return mGridLayoutMgr;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup container = (ViewGroup) mLayoutInflater.inflate(R.layout.apps_group,
                parent, false);

        return new AppsGroupViewHolder(container, mAppInteraction, ItemClickHandler.INSTANCE,
                ItemLongClickListener.INSTANCE_ALL_APPS, ViewConfiguration.getLongPressTimeout());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Apps Groups binding
        final AppsGroup model = mApps.getAppsGroups().get(position);

        boolean focused = mSelectedAppsGroupPosition == position;
        boolean focusModeOn = mSelectedAppsGroupPosition != NO_APPS_GROUP_POSITION;

        AppsGroupViewHolder vh = (AppsGroupViewHolder) holder;
        vh.bind(model, position, focused, focusModeOn, mDeleteMode);



        if (mBindViewCallback != null) {
            mBindViewCallback.onBindView(holder);
        }
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolder holder) {
        // Always recycle and we will reset the view when it is bound
        return true;
    }

    @Override
    public int getItemCount() {
        return mApps.getAppsGroups().size();
    }

    public void setSelectedAppsGroup(int position) {
        mSelectedAppsGroupPosition = position;
        notifyDataSetChanged();
    }

    public void invalidate() {
        notifyDataSetChanged();
        setSelectedAppsGroup(NO_APPS_GROUP_POSITION);
    }

    public int getPositionByStartingLetter(String letter) {
        int position = NO_APPS_GROUP_POSITION;

        for(int i = 0; i < mApps.getAppsGroups().size(); i++) {
            AppsGroup appsGroup = mApps.getAppsGroups().get(i);
            if(appsGroup.firstLetter != null &&
                    appsGroup.firstLetter.startsWith(letter)) {
                position = i;
                break;
            }
        }

        return position;
    }

    public void deselectAppsGroup() {
        int prevFocused = mSelectedAppsGroupPosition;
        mSelectedAppsGroupPosition = NO_APPS_GROUP_POSITION;
        notifyItemChanged(prevFocused);
    }

    public void enableDeleteMode(boolean flag) {
        mDeleteMode = flag;
        notifyDataSetChanged();
    }


}

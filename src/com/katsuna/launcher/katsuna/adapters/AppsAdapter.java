package com.katsuna.launcher.katsuna.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.katsuna.launcher.AppInfo;
import com.katsuna.launcher.BubbleTextView;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.AppInteraction;
import com.katsuna.launcher.katsuna.AppsGroup;
import com.katsuna.launcher.katsuna.viewholders.AppViewHolder;

import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<AppInfo> mApps;
    private final AppInteraction mAppInteraction;
    private final View.OnClickListener mIconClickListener;
    private final View.OnLongClickListener mIconLongClickListener;
    private final int mIconLongPressTimeout;
    private final boolean mDeleteMode;
    private final AppsGroup mAppsGroup;
    private final boolean mFocused;

    public AppsAdapter(AppsGroup appsGroup, AppInteraction appInteraction,
                       View.OnClickListener iconClickListener,
                       View.OnLongClickListener iconLongClickListener,
                       int longPressTimeout, boolean deleteMode, boolean focused) {
        mAppsGroup = appsGroup;
        mApps = appsGroup.apps;
        mAppInteraction = appInteraction;
        mIconClickListener = iconClickListener;
        mIconLongClickListener = iconLongClickListener;
        mIconLongPressTimeout = longPressTimeout;
        mDeleteMode = deleteMode;
        mFocused = focused;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.app_info, parent, false);

        AppViewHolder vh = new AppViewHolder(viewGroup, mAppInteraction);
        BubbleTextView icon = vh.mContent.findViewById(R.id.icon);
        icon.setOnClickListener(mIconClickListener);
        icon.setOnLongClickListener(mIconLongClickListener);
        icon.setLongPressTimeout(mIconLongPressTimeout);
        icon.setFocusable(true);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AppInfo app = mApps.get(position);
        AppViewHolder vh = (AppViewHolder) holder;
        vh.bind(app, mDeleteMode, mAppsGroup, mFocused);
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

}
package com.katsuna.launcher.katsuna.viewholders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.ads.AdListEntry;

public class AdKatsunaHolder extends RecyclerView.ViewHolder {

    private TextView mAppName;
    private ImageView mIcon;

    public AdKatsunaHolder(View view) {
        super(view);
        mAppName = view.findViewById(R.id.app_name);
        mIcon = view.findViewById(R.id.app_icon);
    }

    public void bindView(AdListEntry ad) {
        mAppName.setText(ad.katsunaApp.title);
        mIcon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), ad.katsunaApp.drawableId));
    }

}

package com.katsuna.launcher.katsuna.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.ads.AdListEntry;

public class AdTextHolder extends RecyclerView.ViewHolder {

    private final TextView mText;

    public AdTextHolder(View view) {
        super(view);
        mText = view.findViewById(R.id.ad_info);

    }

    public void bindView(AdListEntry ad) {
        mText.setText(ad.text);
    }

}
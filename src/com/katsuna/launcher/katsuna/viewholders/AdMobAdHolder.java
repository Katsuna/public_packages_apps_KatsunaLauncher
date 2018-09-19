package com.katsuna.launcher.katsuna.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.ads.AdListEntry;

import static com.katsuna.launcher.katsuna.AdsConstants.ADMOB_PLACEMENT_ID_BANNERS;

public class AdMobAdHolder extends RecyclerView.ViewHolder {

    private final LinearLayout mBannerAdsContainer;

    public AdMobAdHolder(View view) {
        super(view);
        mBannerAdsContainer = itemView.findViewById(R.id.banner_ads_container);
    }

    public void bindView(AdListEntry ad) {

        mBannerAdsContainer.removeAllViews();

        for (int i = 0; i < ad.admobAds; i++) {
            showAdMobBanner(i);
        }
    }

    private void showAdMobBanner(int i) {
        Context context = itemView.getContext();

        final AdView adV = new AdView(context);
        adV.setAdSize(AdSize.LARGE_BANNER);
        adV.setAdUnitId(ADMOB_PLACEMENT_ID_BANNERS[i]);

        setLayoutParams(adV);

        AdRequest adRequest = new AdRequest.Builder().build();

        mBannerAdsContainer.addView(adV);
        adV.loadAd(adRequest);
    }

    private void setLayoutParams(View view) {
        Context context = itemView.getContext();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        float dpMargin = context.getResources().getDimension(R.dimen.ads_margin);
        int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpMargin,
                context.getResources().getDisplayMetrics());
        lp.setMargins(pxMargin, pxMargin, pxMargin, pxMargin);

        view.setLayoutParams(lp);
    }

}
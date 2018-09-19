package com.katsuna.launcher.katsuna.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.NativeAd;
import com.katsuna.launcher.R;

import java.util.ArrayList;
import java.util.List;

public class AdFacebookNativeHolder extends RecyclerView.ViewHolder {

    private final ImageView mNativeAdIcon;
    private final TextView mNativeAdTitle;
    private final LinearLayout mAdChoicesContainer;
    private final TextView mNativeAdBody;
    private final ImageView mNativeAdCoverImage;
    private final TextView mNativeAdSocialContext;
    private final Button mNativeAdCallToAction;

    public AdFacebookNativeHolder(View view) {
        super(view);

        mNativeAdIcon = itemView.findViewById(R.id.native_ad_icon);
        mNativeAdTitle = itemView.findViewById(R.id.native_ad_title);
        mAdChoicesContainer = itemView.findViewById(R.id.ad_choices_container);
        mNativeAdBody = itemView.findViewById(R.id.native_ad_body);
        mNativeAdCoverImage = itemView.findViewById(R.id.native_ad_cover_image);
        mNativeAdSocialContext = itemView.findViewById(R.id.native_ad_social_context);
        mNativeAdCallToAction = itemView.findViewById(R.id.native_ad_call_to_action);
    }

    public void bindView(NativeAd nativeAd) {
        // Download and display the ad icon.
        NativeAd.Image adIcon = nativeAd.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, mNativeAdIcon);

        // Set Ad title
        mNativeAdTitle.setText(nativeAd.getAdTitle());

        // Add the AdChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(itemView.getContext(), nativeAd, true);
        mAdChoicesContainer.removeAllViews();
        mAdChoicesContainer.addView(adChoicesView);

        // Set Ad body
        mNativeAdBody.setText(nativeAd.getAdBody());

        // Download and display the cover image.
        NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
        NativeAd.downloadAndDisplayImage(adCoverImage, mNativeAdCoverImage);

        mNativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        mNativeAdCallToAction.setText(nativeAd.getAdCallToAction());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(mNativeAdTitle);
        clickableViews.add(mNativeAdCallToAction);
        nativeAd.registerViewForInteraction(itemView, clickableViews);
    }

}

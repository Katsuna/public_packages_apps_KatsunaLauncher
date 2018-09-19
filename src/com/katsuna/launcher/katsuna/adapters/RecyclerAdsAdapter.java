package com.katsuna.launcher.katsuna.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.KatsunaAlertBuilder;
import com.katsuna.launcher.R;
import com.katsuna.launcher.katsuna.IUserProfileProvider;
import com.katsuna.launcher.katsuna.ads.AdListEntry;
import com.katsuna.launcher.katsuna.ads.AdListEntryType;
import com.katsuna.launcher.katsuna.viewholders.AdFacebookNativeHolder;
import com.katsuna.launcher.katsuna.viewholders.AdKatsunaHolder;
import com.katsuna.launcher.katsuna.viewholders.AdMobAdHolder;
import com.katsuna.launcher.katsuna.viewholders.AdTextHolder;

import java.util.List;

public class RecyclerAdsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private final List<AdListEntry> mAds;
    private final IUserProfileProvider mUserProfileProvider;

    public RecyclerAdsAdapter(Context context, List<AdListEntry> ads,
                              IUserProfileProvider userProfileProvider) {
        mContext = context;
        mAds = ads;
        mUserProfileProvider = userProfileProvider;
    }

    @Override
    public int getItemViewType(int position) {
        return mAds.get(position).adListEntryType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView;
        if (viewType == AdListEntryType.KATSUNA_APP) {
            int viewId;
            if (mUserProfileProvider.getUserProfile().isRightHanded) {
                viewId = R.layout.ad_katsuna;
            } else {
                viewId = R.layout.ad_katsuna_lh;
            }
            inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(viewId, parent, false);
            return new AdKatsunaHolder(inflatedView);
        } else if (viewType == AdListEntryType.FACEBOOK_NATIVE) {
            inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ad_facebook_native, parent, false);
            return new AdFacebookNativeHolder(inflatedView);
        } else if (viewType == AdListEntryType.ADMOB_BANNER) {
            inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ad_admob, parent, false);
            return new AdMobAdHolder(inflatedView);
        } else if (viewType == AdListEntryType.TEXT) {
            inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ad_text, parent, false);
            return new AdTextHolder(inflatedView);
        } else if (viewType == AdListEntryType.SPACER) {
            inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ad_spacer, parent, false);
            return new GenericViewHolder(inflatedView);
        }

        throw new RuntimeException("Unsupported ads viewType");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final AdListEntry ad = mAds.get(position);
        if (ad.adListEntryType == AdListEntryType.FACEBOOK_NATIVE) {
            ((AdFacebookNativeHolder) holder).bindView(ad.facebookNativeAd);
        } else if (ad.adListEntryType == AdListEntryType.ADMOB_BANNER) {
            ((AdMobAdHolder) holder).bindView(ad);
        } else if (ad.adListEntryType == AdListEntryType.KATSUNA_APP) {
            ((AdKatsunaHolder) holder).bindView(ad);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KatsunaAlertBuilder builder = new KatsunaAlertBuilder(mContext);
                    builder.setKatsunaApp(ad.katsunaApp);
                    UserProfile profile = mUserProfileProvider.getUserProfile();
                    if (profile.isRightHanded) {
                        builder.setView(R.layout.katsuna_app_suggestion);
                    } else {
                        builder.setView(R.layout.katsuna_app_suggestion_lh);
                    }
                    builder.setUserProfile(profile);
                    builder.setCancelHidden(true);
                    builder.createKatsunaAppSuggestion().show();
                }
            });
        } else if (ad.adListEntryType == AdListEntryType.TEXT) {
            ((AdTextHolder) holder).bindView(ad);
        }
    }

    @Override
    public int getItemCount() {
        return mAds.size();
    }

    public static class GenericViewHolder extends RecyclerView.ViewHolder {
        public GenericViewHolder(View itemView) {
            super(itemView);
        }
    }
}

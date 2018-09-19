package com.katsuna.launcher.katsuna.viewholders;

import android.content.res.ColorStateList;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;

import com.katsuna.commons.entities.OpticalParams;
import com.katsuna.commons.entities.SizeProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.SizeAdjuster;
import com.katsuna.commons.utils.SizeCalcV2;
import com.katsuna.launcher.AppInfo;
import com.katsuna.launcher.BubbleTextView;
import com.katsuna.launcher.LauncherAppState;
import com.katsuna.launcher.R;
import com.katsuna.launcher.allapps.AllAppsGridAdapter;
import com.katsuna.launcher.katsuna.AppInteraction;
import com.katsuna.launcher.katsuna.AppsGroup;
import com.katsuna.launcher.katsuna.utils.CardColors;
import com.katsuna.launcher.katsuna.utils.CardColorsCalculator;

public class AppViewHolder extends AllAppsGridAdapter.ViewHolder {

    private final BubbleTextView mIcon;
    private final AppInteraction mAppInteraction;
    private final View mDeleteAppContainer;
    private final CardView mAppCardContainer;

    public AppViewHolder(View itemView, AppInteraction appInteraction) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.icon);
        mAppInteraction = appInteraction;
        mDeleteAppContainer = itemView.findViewById(R.id.delete_app_container);
        mAppCardContainer = itemView.findViewById(R.id.app_card_container);
    }

    public void bind(final AppInfo appInfo, boolean deleteMode, AppsGroup appsGroup,
                     boolean focused) {
        adjustProfile(appsGroup, focused);

        mIcon.applyFromApplicationInfo(appInfo);
        //mIcon.setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mAppCardContainer.getLayoutParams();
        if (deleteMode) {
            int marginEnd = itemView.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.delete_button_container_margin);
            params.setMarginEnd(marginEnd);
            mDeleteAppContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAppInteraction.uninstall(appInfo.componentName.getPackageName());
                }
            });
            mDeleteAppContainer.setVisibility(View.VISIBLE);
        } else {
            params.setMarginEnd(0);

            mDeleteAppContainer.setVisibility(View.GONE);
            mDeleteAppContainer.setOnClickListener(null);
        }
    }

    private void adjustProfile(AppsGroup appsGroup, boolean focused) {
        UserProfile profile = mAppInteraction.getUserProfile();
        int mIconSize = itemView.getResources()
                .getDimensionPixelSize(R.dimen.common_contact_photo_size_intemediate);
        if (profile != null) {
            switch (profile.opticalSizeProfile) {
                case ADVANCED:
                    mIconSize = itemView.getResources()
                            .getDimensionPixelSize(R.dimen.common_contact_photo_size_advanced);
                    break;
                case SIMPLE:
                    mIconSize = itemView.getResources()
                            .getDimensionPixelSize(R.dimen.common_contact_photo_size_simple);
                    break;
            }
            //mIcon.setIconSize(mIconSize);

            // app name
            OpticalParams opticalParams = SizeCalcV2.getOpticalParams(SizeProfileKeyV2.TITLE,
                    profile.opticalSizeProfile);
            SizeAdjuster.adjustText(itemView.getContext(), mIcon, opticalParams);
        }

        UserProfile userProfile = mAppInteraction.getUserProfile();
        CardColors cardColors = CardColorsCalculator.calc(itemView.getContext(), userProfile,
                appsGroup, focused);

        mAppCardContainer.setCardBackgroundColor(ColorStateList.valueOf(cardColors.cardColorAlpha));

    }
}

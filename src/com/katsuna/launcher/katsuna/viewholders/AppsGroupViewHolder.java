package com.katsuna.launcher.katsuna.viewholders;

import android.content.res.ColorStateList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.katsuna.commons.entities.ColorProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorCalcV2;
import com.katsuna.launcher.R;
import com.katsuna.launcher.allapps.AllAppsGridAdapter;
import com.katsuna.launcher.katsuna.AppInteraction;
import com.katsuna.launcher.katsuna.AppsGroup;
import com.katsuna.launcher.katsuna.adapters.AppsAdapter;
import com.katsuna.launcher.katsuna.utils.CardColors;
import com.katsuna.launcher.katsuna.utils.CardColorsCalculator;

public class AppsGroupViewHolder extends AllAppsGridAdapter.ViewHolder {

    private final AppInteraction mAppInteraction;
    private final View.OnClickListener mIconClickListener;
    private final View.OnLongClickListener mIconLongClickListener;
    private final RecyclerView mAppsList;
    private final TextView mStartLetter;
    private final ImageView mStarIcon;
    private final TextView mStarDesc;
    private final CardView mAppsGroupContainerCard;
    private final View mAppsGroupContainerCardInner;
    private final View mStarContainer;
    private final View mOpacityLayer;

    public AppsGroupViewHolder(View itemView, AppInteraction appInteraction,
                               View.OnClickListener iconClickListener, View.OnLongClickListener iconLongClickListener) {
        super(itemView);
        mAppInteraction = appInteraction;
        mStarIcon = itemView.findViewById(R.id.star_icon);
        mStarDesc = itemView.findViewById(R.id.star_desc);
        mStartLetter = itemView.findViewById(R.id.start_letter);
        mAppsList = itemView.findViewById(R.id.apps_list);
        mIconClickListener = iconClickListener;
        mIconLongClickListener = iconLongClickListener;

        mStarContainer = itemView.findViewById(R.id.star_container);
        mAppsGroupContainerCard = itemView.findViewById(R.id.apps_group_container_card);
        mAppsGroupContainerCardInner = itemView.findViewById(R.id.apps_group_container_card_inner);
        mOpacityLayer = itemView.findViewById(R.id.opacity_layer);
    }

    public void bind(AppsGroup appsGroup, final int position, boolean focused, boolean focusModeOn,
                     boolean deleteMode) {

        if (appsGroup.premium) {
            mStarIcon.setVisibility(View.VISIBLE);
            mStarDesc.setVisibility(View.VISIBLE);
            mStartLetter.setVisibility(View.GONE);
        } else {
            mStarIcon.setVisibility(View.GONE);
            mStarDesc.setVisibility(View.GONE);
            mStartLetter.setVisibility(View.VISIBLE);
            mStartLetter.setText(appsGroup.firstLetter);
        }

        mStarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppInteraction.selectAppsGroup(position);
            }
        });

        mOpacityLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppInteraction.selectAppsGroup(position);
            }
        });

        AppsAdapter adapter = new AppsAdapter(appsGroup, mAppInteraction, mIconClickListener,
                mIconLongClickListener, deleteMode, focused);
        mAppsList.setAdapter(adapter);

        adjustState(appsGroup, focused, focusModeOn);
    }

    private void adjustState(AppsGroup appsGroup, boolean focused, boolean focusModeOn) {

        UserProfile userProfile = mAppInteraction.getUserProfile();
        CardColors cardColors = CardColorsCalculator.calc(itemView.getContext(), userProfile,
                appsGroup, focused);

        // set colors
        mAppsGroupContainerCard.setCardBackgroundColor(ColorStateList.valueOf(cardColors.cardColorAlpha));
        mAppsGroupContainerCardInner.setBackgroundColor(cardColors.cardColor);

        // adjust star icon, desc and start Letter
        int primaryColor2 = ColorCalcV2.getColor(itemView.getContext(),
                ColorProfileKeyV2.PRIMARY_COLOR_2, userProfile.colorProfile);
        mStarDesc.setTextColor(primaryColor2);
        mStartLetter.setTextColor(primaryColor2);
        mStarIcon.setColorFilter(primaryColor2);

        if (focusModeOn) {
            if (focused) {
                mOpacityLayer.setVisibility(View.INVISIBLE);
            } else {
                mOpacityLayer.setVisibility(View.VISIBLE);
            }
        } else {
            mOpacityLayer.setVisibility(View.INVISIBLE);
        }

    }

}

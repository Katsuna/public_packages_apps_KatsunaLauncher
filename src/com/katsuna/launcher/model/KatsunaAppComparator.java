package com.katsuna.launcher.model;

import android.content.Context;

import com.katsuna.launcher.AppInfo;
import com.katsuna.launcher.util.Thunk;

import java.text.Collator;
import java.util.Comparator;


/**
 * Created by alkis on 25/10/2016.
 */

public class KatsunaAppComparator {
    private final Collator mCollator;
    private final AbstractUserComparator<AppInfo> mAppInfoComparator;
    private final Comparator<String> mSectionNameComparator;

    public KatsunaAppComparator(Context context) {
        mCollator = Collator.getInstance();
        mAppInfoComparator = new AbstractUserComparator<AppInfo>(context) {

            @Override
            public final int compare(AppInfo a, AppInfo b) {
                // Order by launch count
                int result = b.getLaunchCount().compareTo(a.getLaunchCount());
                if (result == 0) {
                    // Order by the title in the current locale
                    result = compareTitles(a.title.toString(), b.title.toString());
                    if (result == 0 && a instanceof AppInfo && b instanceof AppInfo) {
                        AppInfo aAppInfo = (AppInfo) a;
                        AppInfo bAppInfo = (AppInfo) b;
                        // If two apps have the same title, then order by the component name
                        result = aAppInfo.componentName.compareTo(bAppInfo.componentName);
                        if (result == 0) {
                            // If the two apps are the same component, then prioritize by the order that
                            // the app user was created (prioritizing the main user's apps)
                            return super.compare(a, b);
                        }
                    }
                }
                return result;
            }
        };
        mSectionNameComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return compareTitles(o1, o2);
            }
        };
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of applications.
     */
    public Comparator<AppInfo> getAppInfoComparator() {
        return mAppInfoComparator;
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of section names.
     */
    public Comparator<String> getSectionNameComparator() {
        return mSectionNameComparator;
    }

    /**
     * Compares two titles with the same return value semantics as Comparator.
     */
    @Thunk
    int compareTitles(String titleA, String titleB) {
        // Ensure that we de-prioritize any titles that don't start with a linguistic letter or digit
        boolean aStartsWithLetter = (titleA.length() > 0) &&
                Character.isLetterOrDigit(titleA.codePointAt(0));
        boolean bStartsWithLetter = (titleB.length() > 0) &&
                Character.isLetterOrDigit(titleB.codePointAt(0));
        if (aStartsWithLetter && !bStartsWithLetter) {
            return -1;
        } else if (!aStartsWithLetter && bStartsWithLetter) {
            return 1;
        }

        // Order by the title in the current locale
        return mCollator.compare(titleA, titleB);
    }

}

package com.katsuna.launcher.katsuna.ads;

import com.facebook.ads.NativeAd;
import com.katsuna.commons.entities.KatsunaApp;

/**
 * This class is used to load different type of entries for AdsActivity ads list
 */

public class AdListEntry {

    public KatsunaApp katsunaApp;
    public NativeAd facebookNativeAd;
    public int admobAds;
    public int adListEntryType;
    public String text;

}
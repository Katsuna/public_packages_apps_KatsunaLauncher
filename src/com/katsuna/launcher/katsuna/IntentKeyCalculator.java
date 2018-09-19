package com.katsuna.launcher.katsuna;

import android.content.Intent;

/**
 * Created by alkis on 25/10/2016.
 */

public class IntentKeyCalculator {

    public static String getIntentKey(Intent intent) {
        Intent i = new Intent(intent);
        i.setSourceBounds(null);
        return i.toUri(0);
    }

}

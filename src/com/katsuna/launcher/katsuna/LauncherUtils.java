package com.katsuna.launcher.katsuna;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.katsuna.launcher.LauncherSettings;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class LauncherUtils {

    private static final String TAG = "LauncherUtils";

    public static List<String> getHotSeatApps(Context context) {

        List<String> output = new ArrayList<>();

        final Uri contentUri = LauncherSettings.Favorites.CONTENT_URI;
        String selection = LauncherSettings.Favorites.CONTAINER + " = ? ";
        String[] selectionArgs = {String.valueOf(LauncherSettings.Favorites.CONTAINER_HOTSEAT)};
        Cursor c = null;
        try {
            c = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);

            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);

            String intentDescription;
            Intent intent;

            while (c.moveToNext()) {
                try {
                    int itemType = c.getInt(itemTypeIndex);
                    intentDescription = c.getString(intentIndex);

                    switch (itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            try {
                                intent = Intent.parseUri(intentDescription, 0);
                                ComponentName cn = intent.getComponent();
                                if (cn != null) {
                                    output.add(cn.toShortString());
                                }
                            } catch (URISyntaxException e) {
                                Log.e(TAG, "Invalid uri: " + intentDescription);
                                continue;
                            }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return output;
    }

}

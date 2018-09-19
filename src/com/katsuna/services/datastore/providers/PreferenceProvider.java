package com.katsuna.services.datastore.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.katsuna.commons.entities.Preference;
import com.katsuna.services.datastore.db.DatabaseHelper;

import static com.katsuna.commons.providers.PreferenceProvider.URI_PREFERENCE;

/**
 * This provider is responsible for managing Katsuna preferences.
 */

public class PreferenceProvider extends ContentProvider {

    private static final String TAG = "PreferenceProvider";

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result;
        if (URI_PREFERENCE.equals(uri)) {
            result = DatabaseHelper
                    .getInstance(getContext())
                    .getReadableDatabase()
                    .query(Preference.TABLE_NAME, projection, selection, selectionArgs, null,
                            null, sortOrder, null);
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsAffected = DatabaseHelper
                .getInstance(getContext())
                .getWritableDatabase()
                .update(Preference.TABLE_NAME, values, selection, selectionArgs);

        if (rowsAffected != 1) {
//            Log.e(TAG, " Problem updating prefs. Reason: rowsAffected=" + rowsAffected
//                    + " values=" + values + " selection=" + selection
//                    + " selectionArgs=" + Arrays.toString(selectionArgs));
        }

        return rowsAffected;
    }
}

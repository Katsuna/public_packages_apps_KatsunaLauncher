package com.katsuna.services.datastore.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.katsuna.commons.entities.LauncherAccess;
import com.katsuna.services.datastore.db.DatabaseHelper;

import static com.katsuna.commons.providers.LauncherProvider.LAUNCHER_ACCESS_BASE;
import static com.katsuna.commons.providers.LauncherProvider.URI_LAUNCHER_ACCESS;

public class LauncherProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result;
        if (URI_LAUNCHER_ACCESS.equals(uri)) {
            result = DatabaseHelper
                    .getInstance(getContext())
                    .getReadableDatabase()
                    .query(LauncherAccess.TABLE_NAME, projection, selection, selectionArgs, null,
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
        long rowID = DatabaseHelper
                .getInstance(getContext())
                .getWritableDatabase()
                .insert(LauncherAccess.TABLE_NAME, null, values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(Uri.parse(LAUNCHER_ACCESS_BASE), rowID);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(_uri, null);
            }
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

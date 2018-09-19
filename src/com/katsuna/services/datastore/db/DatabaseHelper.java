package com.katsuna.services.datastore.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.Gender;
import com.katsuna.commons.entities.Notification;
import com.katsuna.commons.entities.Preference;
import com.katsuna.commons.entities.PreferenceKey;
import com.katsuna.commons.entities.SizeProfile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "katsuna.db";
    private static final int DATABASE_VERSION = 6;
    private static DatabaseHelper sInstance;
    private final Context context;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
        //Log.i(TAG, "Creating katsuna.db");
        executeSQLScript(_db, "create_katsuna_db.sql");
        initKatsunaPreferences(_db);
    }

    private void initKatsunaPreferences(SQLiteDatabase _db) {
        ArrayList<Preference> preferences = new ArrayList<>();
        preferences.add(new Preference(PreferenceKey.OPTICAL_SIZE_PROFILE,
                SizeProfile.INTERMEDIATE.name()));
        preferences.add(new Preference(PreferenceKey.COLOR_PROFILE, ColorProfile.COLOR_IMPAIREMENT.name()));
        preferences.add(new Preference(PreferenceKey.RIGHT_HAND, String.valueOf(true)));
        preferences.add(new Preference(PreferenceKey.AGE, ""));
        preferences.add(new Preference(PreferenceKey.GENDER, Gender.MALE.name(), ""));
        preferences.add(new Preference(PreferenceKey.NOTIFICATION, Notification.ON.name()));

        for (Preference pref : preferences) {
            String sqlInsertCommand = getSqlInsertCommand(pref);
            _db.execSQL(sqlInsertCommand);
        }
    }

    private String getSqlInsertCommand(Preference preference) {
        return String.format("INSERT INTO %s (%s, %s, %s) VALUES ('%s', '%s', '%s');",
                Preference.TABLE_NAME,
                Preference.COL_KEY, Preference.COL_VALUE, Preference.COL_DESCR,
                preference.getKey(), preference.getValue(), preference.getDescr());
    }

    @Override
    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
        //Log.i(TAG, "Upgrading katsuna.db from version " + _oldVersion + " to " + _newVersion);
        // Uncomment the following lines for development purposes only.
        // For production use an appropriate upgrade script.
        // executeSQLScript(_db, "destroy_katsuna_db.sql");
        // onCreate(_db);
    }

    private void executeSQLScript(SQLiteDatabase database, String sqlscript) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;

        try {
            inputStream = assetManager.open(sqlscript);
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

            String[] createScript = outputStream.toString("UTF-8").split(";");
            for (String aCreateScript : createScript) {
                String sqlStatement = aCreateScript.trim();

                if (sqlStatement.length() > 0) {
                    database.execSQL(sqlStatement + ";");
                }
            }
        } catch (IOException | SQLException e) {
            Log.e(TAG, "ex: " + e.getMessage() + " stack: " + e.toString());
        }
    }
}

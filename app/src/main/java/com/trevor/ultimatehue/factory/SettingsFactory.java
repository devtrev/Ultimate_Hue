package com.trevor.ultimatehue.factory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trevor.ultimatehue.helpers.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nemo on 9/22/15.
 */
public class SettingsFactory {
    private static final String TAG = SettingsFactory.class.toString();

    private static final String QUERY_SETTING_EXISTS = "SELECT COUNT(1) FROM " + DatabaseHelper.SETTINGS_TABLE + " WHERE " + DatabaseHelper.COLUMN_KEY + " = ?";
    private static final String QUERY_SETTING = "SELECT * FROM " + DatabaseHelper.SETTINGS_TABLE + " WHERE " + DatabaseHelper.COLUMN_KEY + " = ?";

    public static void insertSetting(SQLiteDatabase db, String setting, String value) {
        Log.d(TAG, "insertSetting " + value);

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_KEY, setting);
            values.put(DatabaseHelper.COLUMN_VALUE, value);
            db.insert(DatabaseHelper.SETTINGS_TABLE, null, values);

        } catch(Exception e) {
            Log.e(TAG, "Error while inserting new Setting : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void updateSetting(SQLiteDatabase db, String setting, String value) {
        Log.d(TAG, "updateSetting");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_KEY, setting);
            values.put(DatabaseHelper.COLUMN_VALUE, value);
            db.update(DatabaseHelper.SETTINGS_TABLE, values, DatabaseHelper.COLUMN_KEY + "=?", new String[]{setting});
        } catch(Exception e) {
            Log.e(TAG, "Error while updating new Setting : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void updateOrInsert(SQLiteDatabase db, String setting, String value) {
        Log.d(TAG, "updateOrInsert");

        try {
            Cursor cursor = db.rawQuery(QUERY_SETTING_EXISTS, new String[] {setting});

            if(cursor.moveToFirst()) {
                if (cursor.getInt(0) > 0) {
                    Log.i(TAG, "Updating the setting as existing setting has been found");

                    updateSetting(db, setting, value);

                } else {
                    Log.i(TAG, "Inserting the setting as no rows were returned");

                    insertSetting(db, setting,value);
                }
            }

        } catch(Exception e) {
            Log.e(TAG, "Error while updating jump information");
            e.printStackTrace();
        }
    }

    public static String getSetting(SQLiteDatabase db, String setting) {
        Log.d(TAG , "getSetting( " + setting + " )");

        try {
            Cursor cursor = db.rawQuery(QUERY_SETTING, new String[] {setting});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());

            if(cursor.moveToFirst()) {
                String value = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_VALUE));
                cursor.close();

                Log.d(TAG, "Value is " + value);
                return value;
            }

            return "-1";
        } catch(Exception e) {
            Log.e(TAG, "Error while retrieving Setting ");
            e.printStackTrace();

            return "-1";
        }
    }

}

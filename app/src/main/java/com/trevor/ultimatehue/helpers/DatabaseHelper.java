package com.trevor.ultimatehue.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.effect.EffectFactory;
import android.util.Log;

import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.factory.EffectsFactory;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.factory.TriggerFactory;

import java.io.Serializable;

/**
 * Created by nemo on 9/19/15.
 */

public class DatabaseHelper extends SQLiteOpenHelper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6944787293682711750L;
    public static final int DATABASE_VERSION = 3;

    private static DatabaseHelper mInstance = null;

    private static final String TAG = DatabaseHelper.class.getName();

    // DATABASE NAME
    public static final String DATABASE_NAME = "ULTIMATE_HUE";

    // TABLES
    public static final String SETTINGS_TABLE = "SETTINGS";
    public static final String TABLE_COLORS = "COLORS";
    public static final String TABLE_EFFECTS = "EFFECTS";
    public static final String TABLE_TRIGGERS = "TRIGGERS";

    // COLUMNS
    public static final String COLUMN_ID = "ID";
    public static final String _ID = "_ID";
    public static final String COLUMN_KEY = "KEY";
    public static final String COLUMN_VALUE = "VALUE";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_HUE = "HUE";
    public static final String COLUMN_SATURATION = "SATURATION";
    public static final String COLUMN_BRIGHTNESS = "BRIGHTNESS";
    public static final String COLUMN_IMAGE_ID = "IMAGE_ID";
    public static final String COLUMN_TIMES_CLICKED = "TIMES_CLICKED";
    public static final String COLUMN_FAVORITE = "FAVORITE";
    public static final String COLUMN_SLEEP = "SLEEP";
    public static final String COLUMN_SOUND_ID = "SOUND_ID";
    public static final String COLUMN_RANDOM_LIGHT = "COLUMN_RANDOM_LIGHT";
    public static final String COLUMN_TRANSTITION_TIME = "COLUMN_TRANSTITION_TIME";
    public static final String COLUMN_DESCRIPTION = "COLUMN_DESCRIPTION";
    public static final String COLUMN_ACTION = "ACTION";
    public static final String COLUMN_COLOR = "COLOR";
    public static final String COLUMN_ON_OFF = "ON_OFF";
    public static final String COLUMN_GROUP_IDENTIFIER = "GROUP_IDENTIFIER";
    public static final String COLUMN_GROUP_NAME = "GROUP_NAME";
    public static final String COLUMN_LOW = "LOW";
    public static final String COLUMN_HIGH = "HI";
    public static final String COLUMN_TRIGGER_IDENTIFIER = "TRIGGER_IDENTIFIER";
    public static final String COLUMN_ENABLED = "ENABLED";
    public static final String COLUMN_HELP_TEXT = "HELP_TEXT";

    public static DatabaseHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "OnCreate : If tables for this do not exists then create them");

        // Create the Settings Table SQL
        String sql = "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_KEY + " STRING, " +
                COLUMN_VALUE + " STRING );";

        Log.d(TAG, "Creating Settings table");
        db.execSQL(sql);

        // Create Default Settings
        SettingsFactory.insertSetting(db, Constants.SETTING_LAST_EFFECT, "Red Alert");
        SettingsFactory.insertSetting(db, Constants.SETTING_LAST_PLAY_SOUND, "true");
        SettingsFactory.insertSetting(db, Constants.SETTING_CUSTOM_COLOR_HELP, "true");
        SettingsFactory.insertSetting(db, Constants.SETTING_LAST_SONG_PLAYED, "-1");
        SettingsFactory.insertSetting(db, Constants.SETTING_MUSIC_SATURATION, "254");
        SettingsFactory.insertSetting(db, Constants.SETTING_MUSIC_BRIGHTNESS, "175");
        SettingsFactory.insertSetting(db, Constants.SETTING_MUSIC_TRANSITION, "true");
        SettingsFactory.insertSetting(db, Constants.SETTING_EFFECT_TIMES_TO_LOOP, "1");
        SettingsFactory.insertSetting(db, Constants.SETTING_LAST_GROUP, "0");
        SettingsFactory.insertSetting(db, Constants.SETTING_MEMORY_BETA, "false");

        // Create the Color Table SQL
        sql = "CREATE TABLE IF NOT EXISTS " + TABLE_COLORS + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_KEY + " STRING, " +
                COLUMN_NAME + " STRING, " +
                COLUMN_HUE + " INTEGER, " +
                COLUMN_SATURATION + " INTEGER, " +
                COLUMN_BRIGHTNESS + " INTEGER, " +
                COLUMN_IMAGE_ID + " INTEGER, " +
                COLUMN_FAVORITE + " INTEGER, " +
                COLUMN_TIMES_CLICKED + " INTEGER );";

        Log.d(TAG, "Creating Colors table");

        db.execSQL(sql);

        ColorFactory.insertDefaults(db);

        // Create the Effect Table SQL
        sql = "CREATE TABLE IF NOT EXISTS " + TABLE_EFFECTS + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_KEY + " STRING, " +
                COLUMN_NAME + " STRING, " +
                COLUMN_HUE + " STRING, " +
                COLUMN_SATURATION + " STRING, " +
                COLUMN_BRIGHTNESS + " STRING, " +
                COLUMN_SLEEP + " STRING, " +
                COLUMN_RANDOM_LIGHT + " STRING, " +
                COLUMN_TRANSTITION_TIME + " STRING, " +
                COLUMN_IMAGE_ID + " INTEGER, " +
                COLUMN_SOUND_ID + " INTEGER, " +
                COLUMN_FAVORITE + " INTEGER, " +
                COLUMN_TIMES_CLICKED + " INTEGER, " +
                COLUMN_DESCRIPTION + " STRING );";

        Log.d(TAG, "Creating EFFECTS table");

        db.execSQL(sql);

        EffectsFactory.insertDefaults(db);

        // Create the Trigger Table SQL
        sql = "CREATE TABLE IF NOT EXISTS " + TABLE_TRIGGERS + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TRIGGER_IDENTIFIER + " STRING, " +
                COLUMN_NAME + " STRING, " +
                COLUMN_HELP_TEXT + " STRING, " +
                COLUMN_ON_OFF + " INTEGER, " +
                COLUMN_HIGH + " INTEGER, " +
                COLUMN_LOW + " INTEGER, " +
                COLUMN_ENABLED + " INTEGER, " +
                COLUMN_ACTION + " STRING, " +
                COLUMN_COLOR + " STRING, " +
                COLUMN_GROUP_NAME + " STRING, " +
                COLUMN_GROUP_IDENTIFIER + " STRING );";

        Log.d(TAG, "Creating Triggers table");

        db.execSQL(sql);

        TriggerFactory.insertDefaults(db);
        Log.i(TAG, "Tables successfully created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade Called for Database " + oldVersion + " to " + newVersion);

        if(newVersion > oldVersion) {
            // Should update only if old code version was equal to 1 :)
            if(oldVersion < 2) {
                Log.d(TAG, "Updating From Version 1 ");
                // Things that have been added since previous version

                // Updated the Effects to load Images from String Rather than the resource ID.
                // Just remove all existing and re-insert them, yes this loses saved click info
                EffectsFactory.removeAll(db);
                EffectsFactory.insertDefaults(db);
                SettingsFactory.updateSetting(db, Constants.SETTING_EFFECTS_HELP, "false");

                // Remove all and re-insert for colors as well
                ColorFactory.removeAll(db);
                ColorFactory.insertDefaults(db);

                Log.i(TAG, "Successfully upgraded the database to version 2");
            }

            // Code version 1.2.2
            // Will update and add in the Triggers table
            if(oldVersion < 3) {
                Log.d(TAG, "Updating From Version 2 to version 3 of the database ");

                // Create the Trigger Table SQL
                String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_TRIGGERS + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_TRIGGER_IDENTIFIER + " STRING, " +
                        COLUMN_NAME + " STRING, " +
                        COLUMN_HELP_TEXT + " STRING, " +
                        COLUMN_ON_OFF + " INTEGER, " +
                        COLUMN_HIGH + " INTEGER, " +
                        COLUMN_LOW + " INTEGER, " +
                        COLUMN_ENABLED + " INTEGER, " +
                        COLUMN_ACTION + " STRING, " +
                        COLUMN_COLOR + " STRING, " +
                        COLUMN_GROUP_NAME + " STRING, " +
                        COLUMN_GROUP_IDENTIFIER + " STRING );";

                Log.d(TAG, "Creating Triggers table");

                db.execSQL(sql);

                // Insert Defaults into the Trigger table
                Log.i(TAG, "Inserting Default Triggers into database");
                TriggerFactory.insertDefaults(db);

                // Insert Countries into database
                Log.i(TAG, "Insert Countries into database");
                EffectsFactory.insertDefaultCountries(db);


                Log.i(TAG, "Successfully upgraded the database to version 3");
            }
        }
    }



}

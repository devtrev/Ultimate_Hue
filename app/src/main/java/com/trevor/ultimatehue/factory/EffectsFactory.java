package com.trevor.ultimatehue.factory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.Effect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nemo on 10/4/15.
 */
public class EffectsFactory {
    public static final String TAG = EffectsFactory.class.toString();

    private static String QUERY_EFFECT_LIST = "SELECT * FROM " + DatabaseHelper.TABLE_EFFECTS + " WHERE " + DatabaseHelper.COLUMN_KEY + " = ? ORDER BY " + DatabaseHelper.COLUMN_TIMES_CLICKED + " DESC";
    private static String QUERY_EFFECT = "SELECT * FROM " + DatabaseHelper.TABLE_EFFECTS + " WHERE " + DatabaseHelper.COLUMN_NAME + " = ? ";

    public static void insert(SQLiteDatabase db, Effect effect) {
        Log.d(TAG, "insert()");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_KEY, effect.getKey());
            values.put(DatabaseHelper.COLUMN_NAME, effect.getName());
            values.put(DatabaseHelper.COLUMN_HUE, effect.getHueString());
            values.put(DatabaseHelper.COLUMN_SATURATION, effect.getSaturationString());
            values.put(DatabaseHelper.COLUMN_BRIGHTNESS, effect.getBrightnessString());
            values.put(DatabaseHelper.COLUMN_SLEEP, effect.getSleepString());
            values.put(DatabaseHelper.COLUMN_RANDOM_LIGHT, effect.getRandomLightString());
            values.put(DatabaseHelper.COLUMN_TRANSTITION_TIME, effect.getTransitionTimeString());
            values.put(DatabaseHelper.COLUMN_IMAGE_ID, effect.getImageId());
            values.put(DatabaseHelper.COLUMN_SOUND_ID, effect.getSoundId());
            values.put(DatabaseHelper.COLUMN_FAVORITE, effect.getFavorite());
            values.put(DatabaseHelper.COLUMN_TIMES_CLICKED, effect.getTimesClicked());
            values.put(DatabaseHelper.COLUMN_DESCRIPTION, effect.getDescription());
            db.insert(DatabaseHelper.TABLE_EFFECTS, null, values);

        } catch(Exception e) {
            Log.e(TAG, "Error while inserting new color : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void update(SQLiteDatabase db, Effect effect) {
        Log.d(TAG, "update()");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_KEY, effect.getKey());
            values.put(DatabaseHelper.COLUMN_NAME, effect.getName());
            values.put(DatabaseHelper.COLUMN_HUE, effect.getHueString());
            values.put(DatabaseHelper.COLUMN_SATURATION, effect.getSaturationString());
            values.put(DatabaseHelper.COLUMN_BRIGHTNESS, effect.getBrightnessString());
            values.put(DatabaseHelper.COLUMN_SLEEP, effect.getSleepString());
            values.put(DatabaseHelper.COLUMN_RANDOM_LIGHT, effect.getRandomLightString());
            values.put(DatabaseHelper.COLUMN_TRANSTITION_TIME, effect.getTransitionTimeString());
            values.put(DatabaseHelper.COLUMN_IMAGE_ID, effect.getImageId());
            values.put(DatabaseHelper.COLUMN_SOUND_ID, effect.getSoundId());
            values.put(DatabaseHelper.COLUMN_FAVORITE, effect.getFavorite());
            values.put(DatabaseHelper.COLUMN_TIMES_CLICKED, effect.getTimesClicked());
            values.put(DatabaseHelper.COLUMN_DESCRIPTION, effect.getDescription());

            db.update(DatabaseHelper.TABLE_EFFECTS, values, DatabaseHelper._ID + "=?", new String[]{Long.toString(effect.get_id())});
        } catch(Exception e) {
            Log.e(TAG, "Error while updating new Setting : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void updateTimesClicked(SQLiteDatabase db, Effect effect) {
        Log.d(TAG, "updateTimesClicked()");

        try {
            String updateTimesClicked = "UPDATE " + DatabaseHelper.TABLE_EFFECTS +
                    " SET " + DatabaseHelper.COLUMN_TIMES_CLICKED + " = " + (effect.getTimesClicked() + 1) +
                    " WHERE " + DatabaseHelper.COLUMN_NAME + " = '" + effect.getName() + "'";

            db.execSQL(updateTimesClicked);
        } catch(Exception e) {
            Log.e(TAG, "Error while updating times clicked : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void delete(SQLiteDatabase db, Effect effect) {
        Log.d(TAG, "delete()");

        try {
            db.delete(DatabaseHelper.TABLE_EFFECTS, DatabaseHelper._ID + "=?",
                    new String[]{Long.toString(effect.get_id())});
        } catch(Exception e) {
            Log.e(TAG, "Error while deleting color : " + e.toString());
            e.printStackTrace();
        }
    }

    public static  Effect getEffect(SQLiteDatabase db, String effectName) {
        Log.d(TAG, "getEffect()");

        try {
            Cursor cursor = db.rawQuery(QUERY_EFFECT, new String[]{effectName});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());


            if (cursor.moveToFirst()) {

                do {
                    Effect effect = new Effect();
                    effect.set_id(cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID)));
                    effect.setKey(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_KEY)));
                    effect.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                    effect.setHue(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_HUE)));
                    effect.setSaturation(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SATURATION)));
                    effect.setBrightness(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BRIGHTNESS)));
                    effect.setSleep(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SLEEP)));
                    effect.setRandomLight(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RANDOM_LIGHT)));
                    effect.setTransitionTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TRANSTITION_TIME)));
                    effect.setImageId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_ID)));
                    effect.setSoundId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SOUND_ID)));
                    effect.setFavorite(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE)));
                    effect.setTimesClicked(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMES_CLICKED)));
                    effect.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));

                    return effect;
                } while (cursor.moveToNext());

            }

            Log.e(TAG, "Error in populating effect");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error while retrieving Effect List ");
            e.printStackTrace();

            return null;
        }
    }

    public static void removeAll(SQLiteDatabase db) {
        Log.w(TAG, "DELETE ALL");
        db.delete(DatabaseHelper.TABLE_EFFECTS, null, null);
    }

    public static List<Effect> getEffectListByKey(SQLiteDatabase db, String key) {
        Log.d(TAG, "getEffectListByKey()");

        // TODO - Make sure this is commented out for production
        //removeAll(db);
        //insertDefaults(db);

        try {
            List<Effect> effectList = new ArrayList<>();
            Cursor cursor = db.rawQuery(QUERY_EFFECT_LIST, new String[]{key});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());


            if (cursor.moveToFirst()) {

                do {
                    Effect effect = new Effect();
                    effect.set_id(cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID)));
                    effect.setKey(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_KEY)));
                    effect.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                    effect.setHue(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_HUE)));
                    effect.setSaturation(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SATURATION)));
                    effect.setBrightness(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BRIGHTNESS)));
                    effect.setSleep(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SLEEP)));
                    effect.setRandomLight(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RANDOM_LIGHT)));
                    effect.setTransitionTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TRANSTITION_TIME)));
                    effect.setImageId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_ID)));
                    effect.setSoundId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SOUND_ID)));
                    effect.setFavorite(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE)));
                    effect.setTimesClicked(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMES_CLICKED)));
                    effect.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));

                    effectList.add(effect);
                } while (cursor.moveToNext());

            }

            return effectList;
        } catch (Exception e) {
            Log.e(TAG, "Error while retrieving Effect List ");
            e.printStackTrace();

            return null;
        }
    }

    public static void insertDefaults(SQLiteDatabase db) {
        Log.d(TAG, "insertDefaults()");

        List<Effect> effectList = getDefaults();

        // Basic Effects
        for (Effect effect : effectList) {
            insert(db, effect);
        }

        insertDefaultCountries(db);
    }

    public static void insertDefaultCountries(SQLiteDatabase db) {
        Log.d(TAG, "insertDefaults()");

        List<Effect> effectList = getCountries();

        // Country Effects
        for (Effect effect : effectList) {
            insert(db, effect);
        }
    }



    public static void updateDefaults(SQLiteDatabase db) {
        Log.d(TAG, "updateDefaults()");

        List<Effect> effectList = getDefaults();

        // Basic Effects
        for (Effect effect : effectList) {
            Log.i(TAG, "Effect Key = " + effect.get_id());
            update(db, effect);
        }
    }

    private static List<Effect> getCountries() {
        Log.d(TAG, "getCountries");

        List<Effect> effectList = new ArrayList<>();

        // Brazil
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Brazil",
                new int[]{23420,16001},
                new int[]{254,254},
                new int[]{230,230},
                "cc_brazil", // Image name
                0, 0, // Favorite and times clicked
                "Green and Yellow"));

        // United States
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "United States",
                new int[]{2, 47000, 34533},
                new int[]{254, 254, 240},
                new int[]{200, 254, 150},
                "cc_united_states", // Image name
                0, 0, // Favorite and times clicked
                "Red, White and Blue"));

        // Germany
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Germany",
                new int[]{2,15001},
                new int[]{254,254},
                new int[]{200,210},
                "cc_germany", // Image name
                0, 0, // Favorite and times clicked
                "Red and Yellow"));

        // Great Britain
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Great Britain",
                new int[]{2,34533,47000},
                new int[]{254,254,254},
                new int[]{200,145,240},
                "cc_great_britain", // Image name
                0, 0, // Favorite and times clicked
                "Red White and Blue"));

        // Canada
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Canada",
                new int[]{2,34533},
                new int[]{254,254},
                new int[]{200,180},
                "cc_canada", // Image name
                0, 0, // Favorite and times clicked
                "Red and White"));

        // France
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "France",
                new int[]{47000,34533,2},
                new int[]{254,240,254},
                new int[]{230,150,225},
                "cc_france", // Image name
                0, 0, // Favorite and times clicked
                "Blue White and Red"));

        // Australia
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Australia",
                new int[]{23420,10000},
                new int[]{254,170},
                new int[]{230,254},
                "cc_australia", // Image name
                0, 0, // Favorite and times clicked
                "Green and Gold"));

        // Italy
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Italy",
                new int[]{23420,34533,2},
                new int[]{254,240,254},
                new int[]{230,150,225},
                "cc_italy", // Image name
                0, 0, // Favorite and times clicked
                "Green White and Red"));

        // Hungary
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Hungary",
                new int[]{2,34533,23420},
                new int[]{254,240,254},
                new int[]{230,150,225},
                "cc_hungary", // Image name
                0, 0, // Favorite and times clicked
                "Red White and Green"));

        // Netherlands
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Netherlands",
                new int[]{5500,2,34533,5500,47000},
                new int[]{254,254,240,254,254},
                new int[]{230,254,150,254,254},
                "cc_netherlands", // Image name
                0, 0, // Favorite and times clicked
                "Orange Red and White"));

        // Ukraine
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Ukraine",
                new int[]{47000,15001},
                new int[]{254,254},
                new int[]{230,230},
                "cc_ukraine", // Image name
                0, 0, // Favorite and times clicked
                "Blue and Yellow"));

        // New Zealand
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "New Zealand",
                new int[]{34533,2},
                new int[]{254,254},
                new int[]{230,230},
                "cc_new_zealand", // Image name
                0, 0, // Favorite and times clicked
                "White and Red"));

        // Jamaica
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Jamaica",
                new int[]{23420,16001},
                new int[]{254,254},
                new int[]{230,230},
                "cc_jamaica", // Image name
                0, 0, // Favorite and times clicked
                "Green and Yellow"));

        // Russia
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Russia",
                new int[]{34533,47000,2},
                new int[]{240,254,254},
                new int[]{150,225,225},
                "cc_russia", // Image name
                0, 0, // Favorite and times clicked
                "White Blue and Red"));

        // Ireland
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Ireland",
                new int[]{23420,47000,5500},
                new int[]{254,254,230},
                new int[]{200,180,200},
                "cc_ireland", // Image name
                0, 0, // Favorite and times clicked
                "Green and Blue"));

        // Argentina
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Argentina",
                new int[]{45608,34533},
                new int[]{254,254},
                new int[]{254,200},
                "cc_argentina", // Image name
                0, 0, // Favorite and times clicked
                "Light Blue and White"));

        // Finland
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Finland",
                new int[]{34533,47000},
                new int[]{230,254},
                new int[]{200,200},
                "cc_finland", // Image name
                0, 0, // Favorite and times clicked
                "White and Blue"));

        // Belgium
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "Belgium",
                new int[]{2,16001},
                new int[]{254,254},
                new int[]{200,225},
                "cc_belgium", // Image name
                0, 0, // Favorite and times clicked
                "Red and Yellow"));

        // China
        effectList.add(new Effect(Constants.COUNTRY_EFFECT_COLOR_TYPE,
                "China",
                new int[]{2,16001},
                new int[]{254,254},
                new int[]{200,145},
                "cc_china", // Image name
                0, 0, // Favorite and times clicked
                "Red and Yellow"));


        return effectList;
    }

    private static List<Effect> getDefaults() {
        Log.d(TAG, "getDefaults");

        List<Effect> effectList = new ArrayList<>();

        // Red Alert
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Red Alert",
                new int[]{0,0,0,0,0,0,0,0,0,0,0},
                new int[]{254,254,254,254,254,254,254,254,254,254,254},
                new int[]{254,1,254,1,254,1,254,1,254,15,200},
                new double[]{1,1,1,1,1,1,1,1,1,1,1},
                null,
                null,
                "ef_red_alert",
                "red_alert" , 0, 0,
                "Red Alert, Shields up and prepare for battle"));

        // Police - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Police",
                new int[]{0, 47000, 0, 47000, 0, 47000, 0, 47000, 0, 47000},
                new int[]{254, 254, 254, 254, 254, 254, 254, 254, 254, 254},
                new int[]{254, 254, 254, 254, 254, 254, 254, 254, 254, 254},
                new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                null,
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                "ef_police",
                "police", 0, 0,
                "Lights switch between red and blue every second"));


        // Thunderstorm - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Thunderstorm",
                new int[]{14098,14098,34533,34533,15500,14098,14098,14098,34533,34533,14098,14000,14399},  // Hue
                new int[]{181,160,240,240,158,142,160,145,240,210,181,181,160},                  // Saturation
                new int[]{19,6,131,254,10,21,15,7,159,254,4,19,4},                          // Brightness
                new double[]{2,2,.5,.5,3,4,3,3,.5,.5,4,3,2},                            // Interval   4 Seconds is is hit 1,  18 hit 2
                new int[] {Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_ALL,Effect.RANDOM_LIGHT_ALL,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_ALL,Effect.RANDOM_LIGHT_ALL,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE},
                new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0},
                "ef_lightning",
                "thunderstorm", 0, 0,
                "Calm night lighting with bright lightning flashes"));

        // Cloudy Day - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Cloudy Day",
                new int[]{30000, 31010, 30000, 30000,31010},
                new int[]{80,110,80,89,69},
                new int[]{116,160,80,177,120},
                new double[]{8,12,12,12,11},
                null,
                new int[] {15,60,60,60,60},
                "ef_clouds",
                "",  0, 0,
                "50% brightness with slightly increasing and decreasing brightness for a cloud like effect"));

        // Fire Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Fire",
                new int[]{6000,6000,6000,6000,6000,6000,6000,6000,6000,6000,6000},
                new int[]{254,254,254,254,254,254,254,254,254,254,254},
                new int[]{200, 158, 139,98,58,77,139,122,177,221,144},
                new double[]{2,1,1,1,3,1,1,1,3,1,1},
                null,
                null,
                "ef_fire",
                "fire" , 0, 0,
                "Orange fire burning with increasing and decreasing brightness"));


        // Rain Forest - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Rain Forest",
                new int[]{25020,25009,25500,25020,25000},
                new int[]{245,250,245,245,250},
                new int[]{8,20,97,60,27},
                new double[]{8,7,15,10,5},
                null,
                new int[] {9,20,40,15,13},
                "ef_rainforest",
                "rainforest", 0, 0,
                "Green tinted light that fluctuates brightness slightly"));

        // Aurora - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Aurora",
                new int[]{23420,200,23420,23420,23420,23420,200,23420,23420,200,23420,23420,23420,48000,200,23420,200},
                new int[]{254,230,254,254,254,254,230,254,254,230,254,254,254,254,230,254,240},
                new int[]{130,70,90,150,230, 79, 140,250, 170, 88,215, 110, 190,50,75,215,50},
                new double[]{3,2,1,1,1,3,3,3,3,3,3,3,3,1,3,3,3},
                new int[] {Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_ALL,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_ALL,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE},
                new int[] {4,6,4,4,4,14,8,14,12,15,6,12,10,4,12,12,10},
                "ef_aurora",
                "" , 0, 0,
                "Lights Transition to give an Aurora Borealis effect. Mostly Green with a little bit of red from time to time."));

        // Icarus - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Icarus",
                new int[]{13234,13234,13234},
                new int[]{215,215,215},
                new int[]{1, 254,1},
                new double[]{1,93,30},
                null,
                new int[] {8,850,280},
                "ef_icarus",
                "" , 0, 0,
                "Slowly fly closer and closer to the sun before falling back to earth"));

        // Paint Splatter - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Paint Splatter",
                new int[]{13234, 0, 23420, 15000, 47000,60001,5500,0,47000,23420,49500},
                new int[]{215, 254, 254, 254, 254,254,254,254,254,254,254},
                new int[]{100, 254, 150, 175, 200,150,175,83,190,215,215},
                new double[]{2, 3, 2, 3, 2,3,2,3,2,3,2},
                new int[] {Effect.RANDOM_LIGHT_NONE, Effect.RANDOM_LIGHT_SINGLE, Effect.RANDOM_LIGHT_SINGLE, Effect.RANDOM_LIGHT_SINGLE, Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE},
                new int[] {0,4,6,4,4,0,30,0,0,4,4},
                "ef_splatter",
                "splattering", 0, 0,
                "Paint Splatter - Random Colors getting flung on a canvas (Your room)"));

        // Candle - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Candle",
                new int[]{11000,10000,11000,11000,11000,11000,11000,10000,11000,11000,11000,11000},
                new int[]{200,190,215,200,200,200,200,190,215,200,200,200},
                new int[]{20, 100,30,90,110,55,20, 100,30,90,110,55},
                new double[]{3,5,5,3,4,5,3,5,5,3,4,5},
                null,
                new int[] {15,25,25,20,23,10,15,25,25,20,23,10},
                "ef_candle",
                "" , 0, 0,
                "Dimly lit, flickering candle"));

        // Quick Color Cycle - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Quick Color Cycle",
                new int[]{47000,0,15001,50000,25289,60001},
                new int[]{254,254,254,230,240,254},
                new int[]{200, 200, 200, 200, 210,254},
                new double[]{7,7,7,7,7,7},
                null,
                new int[] {50,50,50,50,50,50},
                "vibrant_colors",
                "",0, 0,
                "Quickly cycles through various colors of the rainbow"));

        // Slow Color Cycle - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Slow Color Cycle",
                new int[]{47000,0,15001,50000,25289,60001},
                new int[]{254,254,254,230,240,254},
                new int[]{200, 200, 200, 200, 210,254},
                new double[]{23,23,23,23,23,23},
                null,
                new int[] {200,200,200,200,200,200},
                "ef_color_cycle",
                "" , 0, 0,
                "Slowly cycles through various colors of the rainbow"));

        // Yoga - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Yoga",
                new int[]{23420,60001,16001,48000,23420,0,16001},
                new int[]{254,254,254,230,240,254,254},
                new int[]{130, 130, 130, 130, 130,130,130},
                new double[]{23,23,23,23,23,23,23},
                null,
                new int[] {200,200,200,200,200,200,200},
                "ef_yoga",
                "" , 0, 0,
                "Slowly transitions light colors to help relax"));

        // Packers - Good
        effectList.add(new Effect(Constants.COMMON_EFFECT_COLOR_TYPE,
                "Go Pack Go",
                new int[]{23420, 35000, 23420, 15000, 23420},
                new int[]{150, 150, 254, 254, 254},
                new int[]{254, 254, 175, 225, 175},
                new double[]{1, 1, .75, .75, 1},
                null,
                new int[] {0,0,0,0,0},
                "ef_packers",
                "go_pack_go", 0, 0,
                "Go Packers"));

        // Seasonal

        // Veterans Day - Good
        effectList.add(new Effect(Constants.SEASONAL_EFFECT_COLOR_TYPE,
                "Veterans Day",
                new int[]{47000,0,34533,47000,34533,0,34533,47000,34533,0,47000,34533,0,47000,34533,0,34533,0,47000},
                new int[]{254,254,240,254,240,254,240,254,240,254,254,240,254,254,240,254,240,254,254},
                new int[]{254,254,254,254,254,254,254,254,254,254,254,254,254,254,254,254,254,254,254},
                new double[]{1,4,4,4,2,3,2,2,2,3,3,3,2,3,2,2,3,3,3},
                new int [] {Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE},
                new int[] {0,30,30,30,15,21,15,15,15,25,25,25,15,20,15,15,25,25,25},
                "ef_veterans",
                "veterans" , 0, 0,
                "America! Starts with cycling lights red, white, and blue before randomly updating individual lights"));

        // Deck the Halls - Good
        effectList.add(new Effect(Constants.SEASONAL_EFFECT_COLOR_TYPE,
                "Deck The Halls" ,
                new int [] {15001,49500,49500,6000,60000,  0,25020,49500,25020,30000,6000,60000,0,25020,0,25020,30000,49500,6000,60000,0,25020,0,25020,30000,6000,60000},
                new int [] {254,80,254,254,254,  254,254,254,254,254,254,254,254,254,254,254,80,254,254,254,254,254,254,254,80,254,254} ,
                new int [] {225,200,140,240,83,  254,254,200,200,140,240,83,254,254,200,200,140,240,247,83,254,254,200,200,140,240,83} ,
                new double [] {1,2,1,1,1, 2,3,1,1,1,2,1,3,2,2,1,2,1,2,1,2,3,1,2,1,2,1} ,
                null,
                null,
                "ef_deck_the_halls",
                "deck_the_halls", 0, 0,
                "Fa la la la la, la la la la" ));

        // Christmas Tree - Good
        effectList.add(new Effect(Constants.SEASONAL_EFFECT_COLOR_TYPE,
                "Oh Christmas Tree",
                new int [] {0,23420,0,23420,0,23420,0,23420},
                new int [] {254,254,254,254,254,254,254,254} ,
                new int [] {200,204,204,204,204,204,204,204},
                new double [] {3,3,3,1,1,3,3,3} ,
                null,
                new int[] {0,0,0,0,0,0,0,0},
                "ef_christmas_tree",
                "xmas_tree", 0, 0,
                "Updates lights to help you catch that Christmas spirit" ));

        // Jingle Bells - Good
        effectList.add(new Effect(Constants.SEASONAL_EFFECT_COLOR_TYPE,
                "Jingle Bells" ,
                new int [] {23420,34533,47000,23420,0,47000, 15000,52000,47000,23420,50000,60824,34533,47000,23420,0,47000, 15000,52000,23420,47000},
                new int [] {254,220,254,254,254,254,254,254,254,254,175,150,220,254,254,254,254,254,254,254,254} ,
                new int [] {100,200,155,150,160,230,200,254,200,120,254,254,200,155,150,160,230,200,254,120,200},
                new double [] {3,2,1,2,1,2,2,2,1,2,1,1,2,1,2,1,2,1,1,1,1},
                null,
                null,
                "ef_jingle_bells",
                "jingle_bells", 0, 0,
                "HO HO HO, Merry Christmas" ));

        // Halloween - Good
        effectList.add(new Effect(Constants.SEASONAL_EFFECT_COLOR_TYPE,
                "Halloween",
                new int[]{6000,6000,6000,6000,34533,6000,34533,6000,34533,6000,34533,6000,34533,6000}, //13
                new int[]{254,254,254,200,240,254,240,254,240,130,240,254,240,254}, //13
                new int[]{188, 4, 245,155,160,221,240,100,240,200,7,25,139,190}, //13
                new double[]{2,11,6,5,1,1,1,1,4,5,3,8,6,6}, //13
                new int [] {Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE}, //13
                new int[] {0,100,55,40,0,30,4,10,50,25,40,65,55,60}, //13
                "ef_halloween",
                "" , 0, 0,
                "Lights will be a combination of Orange and white to attempt and create that Halloween feel"));

        // Valentines - Good
        effectList.add(new Effect(Constants.SEASONAL_EFFECT_COLOR_TYPE,
                "Valentines Day",
                new int[]{60001,48000,0,50000,60000,60824,48000/*,1500,49500,48000,0,49500,15001,60000,49500,48000,60000,0,5,48000,60000*/},
                new int[]{199,250,254,175,254,150,254/*,25,200,250,254 ,200,250,254,200,250,254,225,135,250,254*/},
                new int[]{100,50,150,175,156,149,50/*,1,11,50,83  ,11,50,83,11,50,83,103,31,50,83*/},
                new double[]{3,2,8,7,5,9,6/*3,7,7,7  ,11,12,11,11,2,1,11,1,2,3*/},
                new int [] {Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE/*,false,true,true,true  ,true,true,true,true,true,true,false,true,true,true*/},
                new int[] {10,18,75,65,45,80,40/*,28,60,60,65 , 90,90,90, 90,16,9, 89,9,15,24*/},
                "ef_valentines_dog",
                "" , 0, 0,
                "Set the mood with slowly updating red and pink colors"));

        // TODO Fireworks
        /*effectList.add(new Effect(Constants.SEASONAL_EFFECT_COLOR_TYPE,
                "Fireworks",
                new int[]{60001,60001,60001,48000,48000,48000,0,0,0},
                new int[]{254,254,254,254,254,254,254,254,254},
                new int[]{5,254,5,5,254,5,5,254,5},
                new double[]{.9,.5,5,.9,.5,5,.9,.5,5},
                new int [] {Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_NONE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE,Effect.RANDOM_LIGHT_SINGLE},
                new int[] {0,0,0,0,0,0,0,0,0},
                "ef_valentines_dog",
                "" , 0, 0,
                "Firework explosions"));*/

        return effectList;
    }
}

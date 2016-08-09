package com.trevor.ultimatehue.factory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trevor.ultimatehue.HueColor;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nemo on 9/28/15.
 */
public class ColorFactory {
    public static final String TAG = ColorFactory.class.toString();

    private static String QUERY_COLOR = "SELECT * FROM " + DatabaseHelper.TABLE_COLORS + " WHERE " + DatabaseHelper.COLUMN_NAME + " = ?";
    private static String QUERY_COLOR_LIST = "SELECT * FROM " + DatabaseHelper.TABLE_COLORS + " WHERE " + DatabaseHelper.COLUMN_KEY + " = ? ORDER BY " + DatabaseHelper.COLUMN_TIMES_CLICKED + " DESC";
    private static String QUERY_CLOSEST_IMAGE = "SELECT " + DatabaseHelper.COLUMN_IMAGE_ID + " FROM " + DatabaseHelper.TABLE_COLORS + " WHERE " + DatabaseHelper.COLUMN_HUE + " = ?";
    private static String QUERY_CLOSEST_COLOR = "SELECT " + DatabaseHelper.COLUMN_NAME + " FROM " + DatabaseHelper.TABLE_COLORS + " WHERE " + DatabaseHelper.COLUMN_HUE + " = ?";

    public static void insert(SQLiteDatabase db, HueColor hueColor) {
        Log.d(TAG, "insert()");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_KEY, hueColor.getKey());
            values.put(DatabaseHelper.COLUMN_NAME, hueColor.getName());
            values.put(DatabaseHelper.COLUMN_HUE, hueColor.getHue());
            values.put(DatabaseHelper.COLUMN_SATURATION, hueColor.getSaturation());
            values.put(DatabaseHelper.COLUMN_BRIGHTNESS, hueColor.getBrightness());
            values.put(DatabaseHelper.COLUMN_IMAGE_ID, hueColor.getImageId());
            values.put(DatabaseHelper.COLUMN_FAVORITE, hueColor.getFavorite());
            values.put(DatabaseHelper.COLUMN_TIMES_CLICKED, hueColor.getTimesClicked());
            db.insert(DatabaseHelper.TABLE_COLORS, null, values);

        } catch(Exception e) {
            Log.e(TAG, "Error while inserting new color : " + e.toString());
            e.printStackTrace();
        }
    }

    /*public static void insert(SQLiteDatabase db, CycleHueColor hueColor) {
        Log.d(TAG, "insert()");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_KEY, hueColor.getKey());
            values.put(DatabaseHelper.COLUMN_NAME, hueColor.getName());
            values.put(DatabaseHelper.COLUMN_HUE, hueColor.getHueString());
            values.put(DatabaseHelper.COLUMN_SATURATION, hueColor.getSaturationString());
            values.put(DatabaseHelper.COLUMN_BRIGHTNESS, hueColor.getBrightnessString());
            values.put(DatabaseHelper.COLUMN_IMAGE_ID, hueColor.getImageId());
            values.put(DatabaseHelper.COLUMN_FAVORITE, hueColor.getFavorite());
            values.put(DatabaseHelper.COLUMN_TIMES_CLICKED, hueColor.getTimesClicked());
            db.insert(DatabaseHelper.TABLE_COLORS, null, values);

        } catch(Exception e) {
            Log.e(TAG, "Error while inserting new color : " + e.toString());
            e.printStackTrace();
        }
    }*/

    public static void update(SQLiteDatabase db, HueColor hueColor) {
        Log.d(TAG, "update()");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_KEY, hueColor.getKey());
            values.put(DatabaseHelper.COLUMN_NAME, hueColor.getName());
            values.put(DatabaseHelper.COLUMN_HUE, hueColor.getHue());
            values.put(DatabaseHelper.COLUMN_SATURATION, hueColor.getSaturation());
            values.put(DatabaseHelper.COLUMN_BRIGHTNESS, hueColor.getBrightness());
            values.put(DatabaseHelper.COLUMN_IMAGE_ID, hueColor.getImageId());
            values.put(DatabaseHelper.COLUMN_FAVORITE, hueColor.getFavorite());
            values.put(DatabaseHelper.COLUMN_TIMES_CLICKED, hueColor.getTimesClicked());
            db.update(DatabaseHelper.TABLE_COLORS, values, DatabaseHelper._ID + "=?", new String[]{Long.toString(hueColor.get_id())});
        } catch(Exception e) {
            Log.e(TAG, "Error while updating new Setting : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void updateTimesClicked(SQLiteDatabase db, HueColor hueColor) {
        Log.d(TAG, "updateTimesClicked()");

        try {
            String updateTimesClicked = "UPDATE " + DatabaseHelper.TABLE_COLORS +
                    " SET " + DatabaseHelper.COLUMN_TIMES_CLICKED + " = " + (hueColor.getTimesClicked() + 1) +
                    " WHERE " + DatabaseHelper.COLUMN_NAME + " = '" + hueColor.getName() + "'";

            db.execSQL(updateTimesClicked);
        } catch(Exception e) {
            Log.e(TAG, "Error while updating times clicked : " + e.toString());
            e.printStackTrace();
        }
    }

    /*public static void updateTimesClicked(SQLiteDatabase db, CycleHueColor hueColor) {
        Log.d(TAG, "updateTimesClicked()");

        try {
            String updateTimesClicked = "UPDATE " + DatabaseHelper.TABLE_COLORS +
                    " SET " + DatabaseHelper.COLUMN_TIMES_CLICKED + " = " + (hueColor.getTimesClicked() + 1) +
                    " WHERE " + DatabaseHelper.COLUMN_NAME + " = '" + hueColor.getName() + "'";

            db.execSQL(updateTimesClicked);
        } catch(Exception e) {
            Log.e(TAG, "Error while updating times clicked : " + e.toString());
            e.printStackTrace();
        }
    }*/

    public static void delete(SQLiteDatabase db, HueColor hueColor) {
        Log.d(TAG, "delete()");

        try {
            db.delete(DatabaseHelper.TABLE_COLORS, DatabaseHelper._ID + "=?",
                    new String[]{Long.toString(hueColor.get_id())});
        } catch(Exception e) {
            Log.e(TAG, "Error while deleting color : " + e.toString());
            e.printStackTrace();
        }
    }

    public static HueColor getColorByName(SQLiteDatabase db, String name) {
        Log.d(TAG , "getColorByName()");

        try {
            Cursor cursor = db.rawQuery(QUERY_COLOR, new String[] {name});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());

            HueColor hueColor = new HueColor();
            if(cursor.moveToFirst()) {
                hueColor.set_id(cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID)));
                hueColor.setKey(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_KEY)));
                hueColor.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                hueColor.setHue(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_HUE)));
                hueColor.setSaturation(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SATURATION)));
                hueColor.setBrightness(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_BRIGHTNESS)));
                hueColor.setImageId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_ID)));
                hueColor.setFavorite(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE)));
                hueColor.setTimesClicked(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMES_CLICKED)));
                return hueColor;
            }

            return hueColor;
        } catch(Exception e) {
            Log.e(TAG, "Error while retrieving Setting ");
            e.printStackTrace();

            return null;
        }
    }

    public static String getColorNameByHue(SQLiteDatabase db, int hue) {
        Log.d(TAG , "getColorNameByHue()");

        try {
            Cursor cursor = db.rawQuery(QUERY_CLOSEST_COLOR, new String[] {String.valueOf(hue)});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());

            String name = "No Color Found";
            if(cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                return name;
            }

            return name;
        } catch(Exception e) {
            Log.e(TAG, "Error while retrieving Name for hue ");
            e.printStackTrace();

            return "No Color Found";
        }
    }

    public static List<HueColor> getColorListByKey(SQLiteDatabase db, String key) {
        Log.d(TAG , "getColorListByKey()");

        // TODO Make sure its removed for prod release
        //removeAll(db);
        //insertDefaults(db);

        try {
            List<HueColor> hueColorList = new ArrayList<>();
            Cursor cursor = db.rawQuery(QUERY_COLOR_LIST, new String[] {key});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());


            if(cursor.moveToFirst()) {

                do {
                    HueColor hueColor = new HueColor();
                    hueColor.set_id(cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID)));
                    hueColor.setKey(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_KEY)));
                    hueColor.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                    hueColor.setHue(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_HUE)));
                    hueColor.setSaturation(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SATURATION)));
                    hueColor.setBrightness(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_BRIGHTNESS)));
                    hueColor.setImageId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_ID)));
                    hueColor.setFavorite(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE)));
                    hueColor.setTimesClicked(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMES_CLICKED)));

                    hueColorList.add(hueColor);
                } while(cursor.moveToNext());

            }

            return hueColorList;
        } catch(Exception e) {
            Log.e(TAG, "Error while retrieving Setting ");
            e.printStackTrace();

            return null;
        }
    }

    /*public static List<CycleHueColor> getCycleColorListByKey(SQLiteDatabase db, String key) {
        Log.d(TAG , "getColorListByKey()");

        // Make sure its removed for prod release
        //removeAll(db);
        //insertDefaults(db);

        try {
            List<CycleHueColor> hueColorList = new ArrayList<>();
            Cursor cursor = db.rawQuery(QUERY_COLOR_LIST, new String[] {key});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());


            if(cursor.moveToFirst()) {

                do {
                    CycleHueColor hueColor = new CycleHueColor();
                    hueColor.set_id(cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID)));
                    hueColor.setKey(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_KEY)));
                    hueColor.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                    hueColor.setHue(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_HUE)));
                    hueColor.setSaturation(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SATURATION)));
                    hueColor.setBrightness(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BRIGHTNESS)));
                    hueColor.setImageId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_ID)));
                    hueColor.setFavorite(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE)));
                    hueColor.setTimesClicked(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMES_CLICKED)));

                    hueColorList.add(hueColor);
                } while(cursor.moveToNext());

            }

            return hueColorList;
        } catch(Exception e) {
            Log.e(TAG, "Error while retrieving Setting ");
            e.printStackTrace();

            return null;
        }
    }*/

    public static String getClosestLightImageByHue(SQLiteDatabase db, int hue) {
        Log.d(TAG, "getClosestLightImageByHue()");

        try {
            Cursor cursor = db.rawQuery(QUERY_CLOSEST_IMAGE, new String[] {String.valueOf(hue)});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());

            if(cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return "ic_unknown_color";
        } catch(Exception e) {
            Log.e(TAG, "Error while retrieving Setting ");
            e.printStackTrace();

            return "ic_unknown_color";
        }

    }

    public static void removeAll(SQLiteDatabase db) {
        Log.w(TAG, "DELETE ALL");
        db.delete(DatabaseHelper.TABLE_COLORS, null, null);
    }

    /*public static void insertCountries(SQLiteDatabase db) {
        Log.d(TAG, "insertCountries()");

        List<CycleHueColor> hueColorList = new ArrayList<>();

        // Basic Colors
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "United States", new int[] {2,47000,34533}, new int[] {254,254,240}, new int[] {200,254,150}, "cc_united_states", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Germany", new int[] {2,15001}, new int[] {254,254}, new int[] {200,210}, "cc_germany", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "China", new int[] {2,16001}, new int[] {254,254}, new int[] {200,145}, "cc_china", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Great Britain", new int[] {2,34533,47000}, new int[] {254,240,254}, new int[] {200,150,254}, "cc_great_britain", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Russia", new int[] {34533,47000,2}, new int[] {240,254,254}, new int[] {150,225,225}, "cc_russia", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "France", new int[] {47000,34533,2}, new int[] {254,240,254}, new int[] {230,150,225}, "cc_france", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Italy", new int[] {23420,34533,2}, new int[] {254,240,254}, new int[] {230,150,225}, "cc_italy", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Hungary", new int[] {2,34533,23420}, new int[] {254,240,254}, new int[] {230,150,225}, "cc_hungary", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Australia", new int[] {23420,10000}, new int[] {254,170}, new int[] {230,254}, "cc_australia", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Netherlands", new int[] {5500,2,5500,34533,5500,47000}, new int[] {254,254,254,240,254,254}, new int[] {230,254,254,150,254,254}, "cc_netherlands", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "Ukraine", new int[] {47000,15001}, new int[] {254,254}, new int[] {230,230}, "cc_ukraine", 0, 0));
        hueColorList.add(new CycleHueColor(Constants.COUNTRY_COLOR_TYPE, "New Zealand", new int[] {34533,2}, new int[] {254,254}, new int[] {230,230}, "cc_new_zealand", 0, 0));

        for (CycleHueColor hueColor : hueColorList) {
            insert(db , hueColor);
        }
    }*/

    public static void insertDefaults(SQLiteDatabase db) {
        Log.d(TAG , "insertDefaults()");

        List<HueColor> hueColorList = new ArrayList<>();

        // Basic Colors
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Standard", 14922,144,250, "ac_standard", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Nightlight", 14098,181,12, "ac_nightlight", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Red", 2,254,254, "bc_red", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Green", 23420,254,175, "bc_green", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Blue", 47000,254,254, "bc_blue", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Orange", 5500,254,254, "bc_orange", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Yellow", 15001,254,225, "bc_yellow", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Purple", 49500,254,254, "bc_purple", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Pink", 60001,254,254, "bc_pink", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Rose", 60824,150,254, "bc_rose", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Gold", 10000,170,254, "bc_gold", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Lemon", 16001,254,108, "bc_lemon", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Lime", 25289,195,240, "bc_lime", 0 , 0));
        hueColorList.add(new HueColor(Constants.BASIC_COLOR_TYPE, "Soft Pink", 1,135,20, "bc_soft_pink", 0 , 0));

        // Advanced Colors

        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Flourescent", 34533,240,254, "ac_flourescent", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Halogen", 16078,69,254, "ac_halogen", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Antique", 12510,226,120, "ac_antique", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Midnight Velvet", 48000,254,50, "ac_midnight_velvet", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Periwinkle Blue", 38000,225,254, "bc_periwinkle", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Lilac", 50000,175,254, "bc_lilac", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Red Moon", 0,254,83, "ac_red_moon", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Princess Pink", 60000,254,83, "ac_princess_pink", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Moonlight", 1500,25,1, "ac_moonlight", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Sun", 13234,215,254, "ac_sun_noon", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Sunrise", 8700,200,150, "ac_sunrise", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Sunset", 5700,200,150, "ac_sunset", 0 , 0));
        hueColorList.add(new HueColor(Constants.ADVANCED_COLOR_TYPE, "Cool Mint", 29001,254,254, "ac_cool_mint", 0 , 0));

        for (HueColor hueColor : hueColorList) {
            insert(db , hueColor);
        }

        // Insert the countries
        //insertCountries(db);
    }
}

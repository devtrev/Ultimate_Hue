package com.trevor.ultimatehue.factory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.triggers.Trigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nemo on 9/28/15.
 */
public class TriggerFactory {
    public static final String TAG = TriggerFactory.class.toString();

    public static final String TRIGGER_WIFI_CONNECTED = "WIFI CONNECTED";
    public static final String TRIGGER_BATTERY_LOW = "BATTERY LOW";
    public static final String TRIGGER_BATTERY_OKAY = "BATTERY OKAY";
    public static final String TRIGGER_SMS_RECEIVED = "SMS RECEIVED";

    private static String QUERY_TRIGGER = "SELECT * FROM " + DatabaseHelper.TABLE_TRIGGERS + " WHERE " + DatabaseHelper.COLUMN_TRIGGER_IDENTIFIER + " = ?";
    private static String QUERY_TRIGGER_LIST = "SELECT * FROM " + DatabaseHelper.TABLE_TRIGGERS;

    public static void insert(SQLiteDatabase db, Trigger trigger) {
        Log.d(TAG, "insert()");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_TRIGGER_IDENTIFIER, trigger.getIdentifier());
            values.put(DatabaseHelper.COLUMN_NAME, trigger.getName());
            values.put(DatabaseHelper.COLUMN_HELP_TEXT, trigger.getHelpText());
            values.put(DatabaseHelper.COLUMN_ON_OFF, trigger.getOnOff());
            values.put(DatabaseHelper.COLUMN_HIGH, trigger.getHigh());
            values.put(DatabaseHelper.COLUMN_LOW, trigger.getLow());
            values.put(DatabaseHelper.COLUMN_ACTION, trigger.getAction());
            values.put(DatabaseHelper.COLUMN_COLOR, trigger.getColor());
            values.put(DatabaseHelper.COLUMN_GROUP_IDENTIFIER, trigger.getLightGroupIdentifier());
            values.put(DatabaseHelper.COLUMN_GROUP_NAME, trigger.getLightGroupName());
            values.put(DatabaseHelper.COLUMN_ENABLED, trigger.isEnabled());
            db.insert(DatabaseHelper.TABLE_TRIGGERS, null, values);


        } catch (Exception e) {
            Log.e(TAG, "Error while inserting new color : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void update(SQLiteDatabase db, Trigger trigger) {
        Log.d(TAG, "update()");

        try {
            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.COLUMN_TRIGGER_IDENTIFIER, trigger.getIdentifier());
            values.put(DatabaseHelper.COLUMN_NAME, trigger.getName());
            values.put(DatabaseHelper.COLUMN_HELP_TEXT, trigger.getHelpText());
            values.put(DatabaseHelper.COLUMN_ON_OFF, trigger.getOnOff());
            values.put(DatabaseHelper.COLUMN_HIGH, trigger.getHigh());
            values.put(DatabaseHelper.COLUMN_LOW, trigger.getLow());
            values.put(DatabaseHelper.COLUMN_ACTION, trigger.getAction());
            values.put(DatabaseHelper.COLUMN_COLOR, trigger.getColor());
            values.put(DatabaseHelper.COLUMN_GROUP_IDENTIFIER, trigger.getLightGroupIdentifier());
            values.put(DatabaseHelper.COLUMN_GROUP_NAME, trigger.getLightGroupName());
            values.put(DatabaseHelper.COLUMN_ENABLED, trigger.isEnabled());
            db.update(DatabaseHelper.TABLE_TRIGGERS, values, DatabaseHelper._ID + "=?", new String[]{Long.toString(trigger.get_id())});
        } catch (Exception e) {
            Log.e(TAG, "Error while updating new Setting : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void delete(SQLiteDatabase db, Trigger trigger) {
        Log.d(TAG, "delete()");

        try {
            db.delete(DatabaseHelper.TABLE_TRIGGERS, DatabaseHelper._ID + "=?",
                    new String[]{Long.toString(trigger.get_id())});
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting Trigger : " + e.toString());
            e.printStackTrace();
        }
    }


    public static Trigger getTriggerByIdentifier(SQLiteDatabase db, String triggerIdentifier) {
        Log.d(TAG, "getTriggerByIdentifier()");

        try {
            Cursor cursor = db.rawQuery(QUERY_TRIGGER, new String[]{triggerIdentifier});

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());

            Trigger trigger = new Trigger();
            if (cursor.moveToFirst()) {
                trigger.set_id(cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID)));
                trigger.setIdentifier(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TRIGGER_IDENTIFIER)));
                trigger.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                trigger.setHelpText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_HELP_TEXT)));
                trigger.setOnOff(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ON_OFF)));
                trigger.setHigh(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_HIGH)));
                trigger.setLow(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOW)));
                trigger.setAction(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTION)));
                trigger.setColor(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COLOR)));
                trigger.setLightGroupIdentifier(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_IDENTIFIER)));
                trigger.setLightGroupName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_NAME)));
                trigger.setIsEnabled(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ENABLED)));

                return trigger;
            }

            return trigger;
        } catch (Exception e) {
            Log.e(TAG, "Error while retrieving Trigger  ");
            e.printStackTrace();

            return null;
        }
    }

    public static List<Trigger> getTriggerList(SQLiteDatabase db) {
        Log.d(TAG, "getTriggerListByKey()");

        // TODO Make sure its removed for prod release
        //removeAll(db);
        //insertDefaults(db);

        try {
            List<Trigger> triggerList = new ArrayList<>();
            Cursor cursor = db.rawQuery(QUERY_TRIGGER_LIST, null);

            Log.d(TAG, "Number of database rows returned=" + cursor.getCount());


            if (cursor.moveToFirst()) {

                do {
                    Trigger trigger = new Trigger();
                    trigger.set_id(cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID)));
                    trigger.setIdentifier(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TRIGGER_IDENTIFIER)));
                    trigger.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                    trigger.setHelpText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_HELP_TEXT)));
                    trigger.setOnOff(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ON_OFF)));
                    trigger.setHigh(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_HIGH)));
                    trigger.setLow(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOW)));
                    trigger.setAction(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTION)));
                    trigger.setColor(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COLOR)));
                    trigger.setLightGroupIdentifier(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_IDENTIFIER)));
                    trigger.setLightGroupName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_NAME)));
                    trigger.setIsEnabled(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ENABLED)));

                    triggerList.add(trigger);
                } while (cursor.moveToNext());

            }

            return triggerList;
        } catch (Exception e) {
            Log.e(TAG, "Error while retrieving Setting ");
            e.printStackTrace();

            return null;
        }
    }


    public static void removeAll(SQLiteDatabase db) {
        Log.w(TAG, "DELETE ALL");
        db.delete(DatabaseHelper.TABLE_TRIGGERS, null, null);
    }

    public static void insertDefaults(SQLiteDatabase db) {
        Log.d(TAG, "insertDefaults()");

        List<Trigger> triggerList = new ArrayList<>();

        String wifiHelp = "This event will trigger every time that a wifi connection is created. Mainly intended for turning on lights automatically" +
                " whenever you get home. \n\nSometimes this means that if your wifi router " +
                "is rebooting or is not the strongest signal that if your phone were to momentarily disconnect and then reconnect this event would be triggered";
        String lowBatHelp = "This event is triggered when your battery enters a low battery state. This can differ from phone to phone" +
                " depending on what your settings are. This event should be triggered whenever your battery signal shows as red. " +
                "\n\nEvent will not trigger if phone is charging";
        String okBatHelp = "This event is triggered when the battery enters an ok state. In order for this event to be triggered the " +
                "phone must be plugged in and charging. OK state differs from phone to phone but is typically when battery shows as the color yellow.";
        String smsReceivedHelp = "This event is triggered any time that an SMS is received to the phone";

        // Basic Colors
        triggerList.add(new Trigger(TRIGGER_WIFI_CONNECTED, TRIGGER_WIFI_CONNECTED, wifiHelp, "", "", "", "", -1, -1, 0));
        triggerList.add(new Trigger(TRIGGER_BATTERY_LOW, TRIGGER_BATTERY_LOW, lowBatHelp, "", "", "", "", -1, -1, 0));
        triggerList.add(new Trigger(TRIGGER_BATTERY_OKAY, TRIGGER_BATTERY_OKAY, okBatHelp, "", "", "", "", -1, -1, 0));
        triggerList.add(new Trigger(TRIGGER_SMS_RECEIVED, TRIGGER_SMS_RECEIVED, smsReceivedHelp, "", "", "", "", -1, -1, 0));

        for (Trigger trigger : triggerList) {
            insert(db, trigger);
        }
    }
}

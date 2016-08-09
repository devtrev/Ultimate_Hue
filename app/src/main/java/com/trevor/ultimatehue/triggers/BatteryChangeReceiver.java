package com.trevor.ultimatehue.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.HueColor;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.factory.TriggerFactory;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;

/**
 * Created by nemo on 4/4/16.
 */
public class BatteryChangeReceiver extends BroadcastReceiver {

    public static final String TAG = BatteryChangeReceiver.class.toString();

    private SQLiteDatabase db;

    /*
    1)telnet localhost 5554 //where 5554 is your emulator id, which is displayed top left   corner of ur emulator
    2)power capacity 10   //set the battery level to 10%
    3)power ac off    //turns off charging mode
     */

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "Battery Alert received - " + intent.getAction());

        if (intent.getAction() == Intent.ACTION_BATTERY_LOW) {
            Log.d(TAG, "Battery Low");

            try {
                openDatabase(context);

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, TriggerFactory.TRIGGER_BATTERY_LOW);
                trigger.performAction(TAG, context, db);

                closeDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Error while updating the lights for Low Battery : " + e.toString());
                e.printStackTrace();
            }
        } else if (intent.getAction() == Intent.ACTION_BATTERY_OKAY) {
            Log.d(TAG, "Battery Okay");

            try {
                openDatabase(context);

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, TriggerFactory.TRIGGER_BATTERY_OKAY);
                trigger.performAction(TAG, context, db);

                closeDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Error while updating the lights for OKAY Battery : " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private void closeDatabase() {
        Log.i(TAG, "closeDatabase()");
        // Check if Database is closed, if not then close
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    private void openDatabase(Context context) {
        Log.i(TAG, "openDatabase()");
        // Check if Database is open, if not then open
        if (db == null || !db.isOpen()) {
            db = (DatabaseHelper.getInstance(context)).getWritableDatabase();
        }
    }

}

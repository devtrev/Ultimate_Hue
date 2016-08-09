package com.trevor.ultimatehue.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.trevor.ultimatehue.factory.TriggerFactory;
import com.trevor.ultimatehue.helpers.DatabaseHelper;

/**
 * Created by nemo on 5/25/16.
 */
public class SmsReceiver extends BroadcastReceiver {

    public static final String TAG = SmsReceiver.class.toString();
    public static boolean isWifiConnected = true;

    private SQLiteDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {

        // If SMS Was received
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            // Check if WIFI is connected... otherwise can't update lights
            /*ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi.isConnected()) {
                Log.i(TAG, "SMS Received ");

                isWifiConnected = true;
            */
                try {
                    openDatabase(context);

                    Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, TriggerFactory.TRIGGER_SMS_RECEIVED);
                    trigger.performAction(TAG, context, db);

                    closeDatabase();
                } catch (Exception e) {
                    Log.e(TAG, "Error while updating the lights for SMS Received : " + e.toString());
                    e.printStackTrace();
                }

            /*} else {
                Log.i(TAG, "WIFI not connected - Don't even try to udpate the lights");
                isWifiConnected = false;
            }*/
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

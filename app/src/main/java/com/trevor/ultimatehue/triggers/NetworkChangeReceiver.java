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
 * Attempting to Add trigger for Wifi
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String TAG = NetworkChangeReceiver.class.toString();
    public static boolean isWifiConnected = true;

    private SQLiteDatabase db;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                String ssid = connectionInfo.getSSID();
            }
            isWifiConnected = true;
            Log.i(TAG, "WIFI connected");

            try {
                openDatabase(context);

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, TriggerFactory.TRIGGER_WIFI_CONNECTED);
                trigger.performAction(TAG, context, db);

                closeDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Error while updating the lights for WIFI change : " + e.toString());
                e.printStackTrace();
            }

        } else {
            Log.i(TAG, "WIFI not connected");
            isWifiConnected = false;
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
package com.trevor.ultimatehue.helpers;

import android.content.Context;
import android.util.Log;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.R;

import java.util.List;

/**
 * Created by nemo on 5/19/16.
 */
public class HueHelper {
    private static final String TAG = HueHelper.class.toString();

    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    public HueHelper (PHHueSDK phHueSDK, PHBridge bridge) {
        this.phHueSDK = phHueSDK;
        this.bridge = bridge;
    }

    public HueHelper(){};

    public void updateLightGroup(Context context, String lightIdentifier, PHLightState lightState) {
        Log.d(TAG, "updateLightGroup");

        try {
            if(phHueSDK == null) {
                Log.d(TAG, "phHueSDK is null");
                phHueSDK = PHHueSDK.create();

                // Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
                phHueSDK.setAppName(context.getString(R.string.app_name));
                phHueSDK.setDeviceName(android.os.Build.MODEL);
            }
            if(bridge == null) {
                Log.d(TAG, "bridge is null");
                if (phHueSDK.getSelectedBridge() == null) {
                    Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

                    reconnectHueBridge(context);
                    bridge = phHueSDK.getSelectedBridge();
                } else {
                    bridge = phHueSDK.getSelectedBridge();
                }
            }

            Log.d(TAG, "Light Identifier : " + lightIdentifier);
            Log.d(TAG, "Light State : " + lightState.getHue());

            bridge.setLightStateForGroup(lightIdentifier, lightState);
        } catch (Exception e) {
            Log.e(TAG, "Error while updating lights - Throwing Exception : " + e.toString());
            e.printStackTrace();
        }
    }

    public void updateLight(Context context, String lightIdentifier, PHLightState lightState) {
        Log.d(TAG, "updateLight");

        try {
            if(phHueSDK == null) {
                Log.d(TAG, "phHueSDK is null");
                phHueSDK = PHHueSDK.create();

                // Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
                phHueSDK.setAppName(context.getString(R.string.app_name));
                phHueSDK.setDeviceName(android.os.Build.MODEL);
            }
            if(bridge == null) {
                Log.d(TAG, "bridge is null");
                if (phHueSDK.getSelectedBridge() == null) {
                    Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

                    reconnectHueBridge(context);
                    bridge = phHueSDK.getSelectedBridge();
                } else {
                    bridge = phHueSDK.getSelectedBridge();
                }
            }

            Log.d(TAG, "Light Identifier : " + lightIdentifier);
            Log.d(TAG, "Light State : " + lightState.getHue());

            PHLight light = bridge.getResourceCache().getLights().get(lightIdentifier);
            bridge.updateLightState(light, lightState);
        } catch (Exception e) {
            Log.e(TAG, "Error while updating lights - Throwing Exception : " + e.toString());
            e.printStackTrace();
        }
    }

    // If Hue disconnected this should reonnect to the bridge before proceeding
    private void reconnectHueBridge(Context context) {
        Log.i(TAG, "reconnectHueBridge");

        // Try to automatically connect to the last known bridge.  For first time use this will be empty so a bridge search is automatically started.
        HueSharedPreferences prefs = HueSharedPreferences.getInstance(context.getApplicationContext());
        String lastIpAddress   = prefs.getLastConnectedIPAddress();
        String lastUsername    = prefs.getUsername();

        // Automatically try to connect to the last connected IP Address.  For multiple bridge support a different implementation is required.
        if (lastIpAddress !=null && !lastIpAddress.equals("")) {
            Log.i(TAG, "IP Found, resuming last connection");
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);

            if (!phHueSDK.isAccessPointConnected(lastAccessPoint)) {
                Log.i(TAG, "Access point not connected, attempting to establish connection");
                phHueSDK.connect(lastAccessPoint);

                // Sleep for 2 seconds hoping that provides enough time for app to reconnect
                sleep(TAG, 2);
                Log.d(TAG, "Hopefully connected to the bridge now");
            }
        }
    }

    // Get list of individual lights from group, So give it a group and return list of lights
    public List<String> getLightListFromGroup(String lightGroupIdentifier, Context context) {
        Log.d(TAG, "getLightListFromGroup");

        try {
            if(phHueSDK == null) {
                Log.d(TAG, "phHueSDK is null");
                phHueSDK = PHHueSDK.create();

                // Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
                phHueSDK.setAppName(context.getString(R.string.app_name));
                phHueSDK.setDeviceName(android.os.Build.MODEL);
            }
            if(bridge == null) {
                Log.d(TAG, "bridge is null");
                if (phHueSDK.getSelectedBridge() == null) {
                    Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

                    reconnectHueBridge(context);
                    bridge = phHueSDK.getSelectedBridge();
                } else {
                    bridge = phHueSDK.getSelectedBridge();
                }
            }

            return bridge.getResourceCache().getGroups().get(lightGroupIdentifier).getLightIdentifiers();
        } catch (Exception e) {
            Log.e(TAG, "Error while updating lights - Throwing Exception : " + e.toString());
            e.printStackTrace();

            return null;
        }
    }

    private void sleep(String TAG, double sleepy) {
        Log.d(TAG, "Sleeping");

        try {
            Thread.sleep((long) (sleepy * 1000));
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to sleep");
            e.printStackTrace();
        }
    }
}

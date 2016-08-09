package com.trevor.ultimatehue.triggers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.quickstart.PHWizardAlertDialog;
import com.trevor.ultimatehue.HueColor;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.factory.EffectsFactory;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.lights.LightGroup;

/**
 * Created by nemo on 4/6/16.
 */
public class Trigger {

    private long _id;
    private String identifier;
    private String name;
    private String action;
    private String color;
    private String lightGroupName;
    private String lightGroupIdentifier;
    private int low;
    private int high;
    private int onOff;
    private int isEnabled;
    private String helpText;

    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    public Trigger () {}

    public Trigger(String identifier, String name, String helpText, String action, String color, String lightGroupName, String lightGroupIdentifier, int low, int high, int isEnabled) {
        this.identifier = identifier;
        this.name = name;
        this.helpText = helpText;
        this.action = action;
        this.color = color;
        this.lightGroupName = lightGroupName;
        this.lightGroupIdentifier = lightGroupIdentifier;
        this.low = low;
        this.high = high;
        this.isEnabled = isEnabled;
    }

    public Trigger(String identifier, String name, String action, String color,String lightGroupName, String lightGroupIdentifier, int isEnabled) {
        this.identifier = identifier;
        this.name = name;
        this.action = action;
        this.color = color;
        this.lightGroupName = lightGroupName;
        this.lightGroupIdentifier = lightGroupIdentifier;
        this.low = 0;
        this.high = 100;
        this.isEnabled = isEnabled;
    }

    public String getDescription() {

        if(isEnabled == Constants.ENABLED)
            if(this.action.contains("Off"))
                return "When " + this.name + " then update group " + this.lightGroupName.toUpperCase() + " to " + this.action.toUpperCase();
            else
                return "When " + this.name + " then update group " + this.lightGroupName.toUpperCase() + " to " + this.action.toUpperCase() + " to color " + this.color.toUpperCase();
        else
            return "Do nothing - Click here to set an action";
    }

    public void performAction(String TAG, Context context, SQLiteDatabase db) {
        Log.i(TAG, "performAction");

        if(isEnabled() != Constants.ENABLED) {
            Log.d(TAG, "Trigger " + getIdentifier() + " is NOT enabled");

            // Do nothing as the trigger is disabled

        } else {
            Log.d(TAG, "Trigger " + getIdentifier() + " is enabled");

            // It's enabled so lets party!

            if(getAction().equals(context.getString(R.string.trigger_lights_off))) {
                Log.d(TAG, "Trigger is set to turn lights OFF");

                // Just set state to off and update the appropriate light grouping
                PHLightState state = new PHLightState();
                state.setOn(false);

                updateLightGroup(TAG, context, getLightGroupIdentifier(), state);
            } else if (getAction().equals(context.getString(R.string.trigger_lights_on))) {
                Log.d(TAG, "Trigger is set to turn lights ON");
                // Action is set to turn the lights on

                // Need to get the appropriate color that user wanted lights to update to
                HueColor color = ColorFactory.getColorByName(db, getColor());

                PHLightState state = new PHLightState();
                state.setOn(true);

                // If its null then just use current state of light color and turn the lights on
                if (color != null) {
                    // Now create the proper state

                    state.setHue(color.getHue());
                    state.setBrightness(color.getBrightness());
                    state.setSaturation(color.getSaturation());
                }

                // Update the light group user wanted updated
                updateLightGroup(TAG, context, getLightGroupIdentifier(), state);
            } else if (getAction().equals(context.getString(R.string.trigger_flash_lights))) {
                Log.d(TAG, "Trigger is set to turn lights FLASH");
                // Action is set to turn the lights on

                // Need to get the appropriate color that user wanted lights to update to
                HueColor flashColor = ColorFactory.getColorByName(db, getColor());

                // If its null then just use current state of light color and turn the lights on
                if (flashColor == null) {
                    // If Null then set to Red Flash
                    flashColor.setHue(2);
                    flashColor.setSaturation(254);
                    flashColor.setBrightness(200);
                }

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

                        reconnectHueBridge(TAG, context);
                        bridge = phHueSDK.getSelectedBridge();
                    } else {
                        bridge = phHueSDK.getSelectedBridge();
                    }
                }

                // This gets a single light in the group. We then get the state of this light to save off for the default of the group
                // Since groups don't return a light State
                String lightInGroup = bridge.getResourceCache().getGroups().get(getLightGroupIdentifier()).getLightIdentifiers().get(0);
                PHLightState currentState = bridge.getResourceCache().getLights().get(lightInGroup).getLastKnownLightState();

                HueColor currentColor = new HueColor();
                boolean isOn = true;
                // Set the state that lights are currently in so we can go back
                if(currentState == null) {
                    Log.d(TAG, "Current State was Null");

                    currentColor = flashColor;

                } else {
                    currentColor.setHue(currentState.getHue());
                    currentColor.setSaturation(currentState.getSaturation());
                    currentColor.setBrightness(currentState.getBrightness());
                    currentColor.setIsOn(currentState.isOn());
                }

                // Call Flash lights
                flashLights(TAG, context, flashColor, currentColor);
            } else if (getAction().equals(context.getString(R.string.trigger_effect))) {
                Log.d(TAG, "Trigger is set to turn lights FLASH");
                // Action is set to turn the lights on

                // Need to get the appropriate color that user wanted lights to update to
                Effect effect = EffectsFactory.getEffect(db, getColor());
                
                // Play the effect, play sound if high is set to 1 (Repurposed high as sound ID...)
                if(getHigh() == 1)
                    effect.performAction(context,getLightGroupIdentifier(),true);
                else
                    effect.performAction(context,getLightGroupIdentifier(),false);
            }
        }
    }

    private final void flashLights(String TAG, Context context, HueColor flashColor, HueColor currentColor) {
        Log.d(TAG, "flashLights");

        // Determine what the secondary brightness for flashing should be

        int secondaryBrightness;
        if(flashColor.getBrightness() > 100)
            secondaryBrightness = 1;
        else
            secondaryBrightness = 254;

        for (int i=0; i <= 3; i++) {
            if((i%2) == 0) {
                // This is even

                PHLightState state = new PHLightState();
                state.setOn(true);
                state.setTransitionTime(0);

                if(i==0) {
                    state.setHue(flashColor.getHue());
                    state.setSaturation(flashColor.getSaturation());
                    state.setBrightness(flashColor.getBrightness());
                } else
                    state.setBrightness(flashColor.getBrightness());

                // Update the light group user wanted updated
                updateLightGroup(TAG, context, getLightGroupIdentifier(), state);
            } else {
                // This is odd

                PHLightState state = new PHLightState();
                state.setTransitionTime(0);
                state.setBrightness(secondaryBrightness);

                // Update the light group user wanted updated
                updateLightGroup(TAG, context, getLightGroupIdentifier(), state);
            }

            // Sleep for 1.3 seconds (Helps with fluidity to be over 1 second)
            sleep(TAG, 1.3);

        } // End  for loop

        // Sleep another second for good measure :)
        sleep(TAG, 1);

        // Reset lights to where they were
        PHLightState state = new PHLightState();
        state.setTransitionTime(10);
        state.setHue(currentColor.getHue());
        state.setSaturation(currentColor.getSaturation());
        state.setBrightness(currentColor.getBrightness());
        state.setOn(currentColor.getIsOn());

        // Update the light group user wanted updated
        updateLightGroup(TAG, context, getLightGroupIdentifier(), state);
    }

    private void updateLightGroup(String TAG, Context context, String lightIdentifier, PHLightState lightState) {
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

                    reconnectHueBridge(TAG, context);
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

    private void sleep(String TAG, double sleepy) {
        Log.d(TAG, "Sleeping");

        try {
            Thread.sleep((long) (sleepy * 1000));
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to sleep");
            e.printStackTrace();
        }
    }

    // If Hue disconnected this should reonnect to the bridge before proceeding
    private void reconnectHueBridge(String TAG, Context context) {
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getOnOff() {
        return onOff;
    }

    public void setOnOff(int onOff) {
        this.onOff = onOff;
    }

    public long get_id() {
        return _id;
    }



    public void set_id(long _id) {
        this._id = _id;
    }


    public String getLightGroupName() {
        return lightGroupName;
    }

    public void setLightGroupName(String lightGroupName) {
        this.lightGroupName = lightGroupName;
    }

    public String getLightGroupIdentifier() {
        return lightGroupIdentifier;
    }

    public void setLightGroupIdentifier(String lightGroupIdentifier) {
        this.lightGroupIdentifier = lightGroupIdentifier;
    }

    public int isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(int isEnabled) {
        this.isEnabled = isEnabled;
    }


    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }
}

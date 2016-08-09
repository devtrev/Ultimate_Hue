package com.trevor.ultimatehue.lights;

import android.util.Log;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Map;

/**
 * Created by nemo on 9/19/15.
 */
public class LightGroup {
    public static final String tag = LightGroup.class.toString();

    private List<String> lightIdentifiers;

    private PHGroup phGroup;

    public LightGroup () {};

    public LightGroup (PHGroup phGroup) {
        this.phGroup = phGroup;
        this.lightIdentifiers = phGroup.getLightIdentifiers();
    }

    public void updateLights (PHBridge bridge, Boolean isOn) {
        Map<String, PHLight> allLights = bridge.getResourceCache().getLights();

        for (int i = 0; i < lightIdentifiers.size(); i++) {
            PHLight light = allLights.get(lightIdentifiers.get(i));
            Log.d(tag, "Updating light : " + light.getName());

            PHLightState lightState = new PHLightState();

            lightState.setOn(isOn);

            bridge.updateLightState(light, lightState);
        }
    }

    public void updateLights (PHBridge bridge, PHLightState lightState) {
        Map<String, PHLight> allLights = bridge.getResourceCache().getLights();

        for (int i = 0; i < lightIdentifiers.size(); i++) {
            PHLight light = allLights.get(lightIdentifiers.get(i));
            Log.d(tag, "Updating light : " + light.getName());

            bridge.updateLightState(light, lightState);
        }
    }

    public PHGroup getPhGroup() {
        return phGroup;
    }

    public void setPhGroup(PHGroup phGroup) {
        this.phGroup = phGroup;
    }

    /**
     * Returns boolean of whether light group is on or off
     * Will return false if any one light is turned off.
     * @return
     */
    public boolean isOn(PHBridge bridge) {
        Map<String, PHLight> allLights = bridge.getResourceCache().getLights();

        Log.d(tag, "Looping through all the lights : " + allLights.size());

        // This is looping through all the lights but should only be looking at the ones that we care about. It is
        // Doing a for loop on our list of light identifiers and pulling the ones we want from the list of All Lights
        for (int i = 0; i < lightIdentifiers.size(); i++) {
            Log.d(tag, "Getting Light state for : " + allLights.get(lightIdentifiers.get(i)).getName());

            // If any light is in the off state return false
            if (!allLights.get(lightIdentifiers.get(i)).getLastKnownLightState().isOn())
                return false;
        }

        // If we made it here the lights must all be on so return true
        return true;
    }

    public int getLastKnownHue(PHBridge bridge) {
        Log.d(tag, "getLastKnownHue");

        Map<String, PHLight> allLights = bridge.getResourceCache().getLights();

        int lastKnownHue = -1;

        // This is looping through all the lights but should only be looking at the ones that we care about. It is
        // Doing a for loop on our list of light identifiers and pulling the ones we want from the list of All Lights
        for (int i = 0; i < lightIdentifiers.size(); i++) {
            PHLight phLight = allLights.get(lightIdentifiers.get(i));

            Log.d(tag, "Getting Light state for : " + phLight.getName());

            if(phLight.supportsColor()) {
                PHLightState state = phLight.getLastKnownLightState();

                return state.getHue();
            } else {
                Log.d(tag, "Bulb does not support color, looping ot next bulb in group");
            }
        }

        return lastKnownHue;
    }

    public int getBrightness(PHBridge bridge) {
        PHLight light = bridge.getResourceCache().getLights().get(lightIdentifiers.get(0));
        if(light.supportsBrightness()) {
            PHLightState state = light.getLastKnownLightState();

            if(state!= null && state.getBrightness() != null)
                return state.getBrightness();
            else {
                Log.w(tag, "Light does not have last known state :(");
                return 254;
            }
        }
        else {
            Log.w(tag, "Light does not support brightness");
            return 254;
        }
    }
}

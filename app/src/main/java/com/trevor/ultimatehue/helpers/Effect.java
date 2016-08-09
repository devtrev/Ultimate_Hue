package com.trevor.ultimatehue.helpers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.music.SoundPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by nemo on 10/4/15.
 */
public class Effect {

    private static final String TAG = Effect.class.toString();

    public static final int RANDOM_LIGHT_NONE = 0;
    public static final int RANDOM_LIGHT_SINGLE = 1;
    public static final int RANDOM_LIGHT_ALL = 2;

    public boolean interrupted = false;

    private long _id;
    private String key;
    private String name;
    private int[] hue;
    private int[] saturation;
    private int[] brightness;
    private int[] randomLight;
    private int[] transitionTime;
    private double[] sleep;
    private String imageId;
    private String soundId;
    private int favorite;
    private int timesClicked;
    private boolean isRandomSupported;
    private boolean isCustomTransitions;
    private String description;

    public Effect() {
    }

    public Effect(String key, String name, int[] hue, int[] saturation, int[] brightness, double[] sleep, int[] randomLight, int[] transitionTime, String imageId, String soundId, int favorite, int timesClicked, String description) {
        this.key = key;
        this.name = name;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.sleep = sleep;
        this.randomLight = randomLight;
        this.transitionTime = transitionTime;
        this.imageId = imageId;
        this.soundId = soundId;
        this.favorite = favorite;
        this.timesClicked = timesClicked;
        this.description = description;

        if (randomLight != null)
            isRandomSupported = true;
        else
            isRandomSupported = false;

        if (transitionTime != null)
            isCustomTransitions = true;
        else
            isCustomTransitions = false;
    }

    // This is used for the countries effect (Could be re-used for any mood type lighting)
    public Effect(String key, String name, int[] hue, int[] saturation, int[] brightness, String imageId, int favorite, int timesClicked, String description) {
        this.key = key;
        this.name = name;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.sleep = new double[]{0}; // Sleep timer can't be null
        this.randomLight = null;
        this.transitionTime = null;
        this.imageId = imageId;
        this.soundId = "";
        this.favorite = favorite;
        this.timesClicked = timesClicked;
        this.description = description;

        if (randomLight != null)
            isRandomSupported = true;
        else
            isRandomSupported = false;

        if (transitionTime != null)
            isCustomTransitions = true;
        else
            isCustomTransitions = false;
    }

    public int getTotalEffectTime() {
        double totalEffectTime = 0.0;

        for (double sleep : this.sleep) {
            totalEffectTime += sleep;
        }

        // Just return as int to make things easier
        return (int) totalEffectTime;
    }

    public String getHueString() {
        StringBuilder hueString = new StringBuilder();

        for (int x : this.hue) {
            hueString.append(x);
            hueString.append(",");
        }

        return hueString.length() > 0 ? hueString.substring(0, hueString.length() - 1) : "";
    }

    public String getSaturationString() {
        StringBuilder hueString = new StringBuilder();

        for (int x : this.saturation) {
            hueString.append(x);
            hueString.append(",");
        }

        return hueString.length() > 0 ? hueString.substring(0, hueString.length() - 1) : "";
    }

    public String getBrightnessString() {
        StringBuilder brightnessString = new StringBuilder();

        for (int x : this.brightness) {
            brightnessString.append(x);
            brightnessString.append(",");
        }

        return brightnessString.length() > 0 ? brightnessString.substring(0, brightnessString.length() - 1) : "";
    }

    public String getSleepString() {
        StringBuilder sleepString = new StringBuilder();

        for (double x : this.sleep) {
            sleepString.append(x);
            sleepString.append(",");
        }

        return sleepString.length() > 0 ? sleepString.substring(0, sleepString.length() - 1) : "";
    }

    public String getTransitionTimeString() {
        if (this.transitionTime != null) {
            StringBuilder transitionTimeString = new StringBuilder();

            for (int x : this.transitionTime) {
                transitionTimeString.append(x);
                transitionTimeString.append(",");
            }

            return transitionTimeString.length() > 0 ? transitionTimeString.substring(0, transitionTimeString.length() - 1) : "";
        }

        return null;

    }

    public String getRandomLightString() {
        if (this.randomLight != null) {
            StringBuilder randomLightString = new StringBuilder();


            for (int x : this.randomLight) {
                randomLightString.append(String.valueOf(x));
                randomLightString.append(",");
            }

            return randomLightString.length() > 0 ? randomLightString.substring(0, randomLightString.length() - 1) : "";
        }

        return null;
    }

    public void performAction(Context context, String lightGroupIdentifier, boolean playSound) {
        Log.d(TAG, "performAction");

        if (getKey().equals(Constants.COUNTRY_EFFECT_COLOR_TYPE)) {
            Log.i(TAG, "Updating country Effect");

            countryEffect(context, lightGroupIdentifier);
        } else {
            Log.i(TAG, "Updating normal Effect");

            int prevRandom = -1;

            // HueHelper to be used whenever calling the bridge to update lights
            HueHelper hueHelper = new HueHelper();

            if (playSound) {
                Log.i(TAG, "Playing sound - Looking up from file name");

                //SoundPlayer.playSound(context, effect.getSoundId());
                // Removed the above line and added in file name lookup from strings -- Version 1.2.1
                int raw = context.getResources().getIdentifier(getSoundId(), "raw", context.getPackageName());
                SoundPlayer.playSound(context, raw);
            }

            // Loop through the Effect and update as appropriate
            for (int i = 0; i < getHue().length; i++) {
                Log.d(TAG, "Current Hue is " + getHue()[i]);
                Log.d(TAG, "Current Saturation is " + getSaturation()[i]);
                Log.d(TAG, "Current Brightness is " + getBrightness()[i]);
                Log.d(TAG, "Current Sleep is " + getSleep()[i]);

                // Update the lights to the current color
                PHLightState newLightState = new PHLightState();
                newLightState.setHue(getHue()[i]);
                newLightState.setSaturation(getSaturation()[i]);
                newLightState.setBrightness(getBrightness()[i]);

                // Only set if custom is supported, otherwise use teh default
                if (isCustomTransitions())
                    newLightState.setTransitionTime(getTransitionTime()[i]);

                // if random light then need to just update one light, otherwise update all
                if (isRandomSupported()) {

                    // Check if this particular loop should update individual light
                    if (getRandomLight()[i] != Effect.RANDOM_LIGHT_NONE) {

                        // Get the list of the lights from the group
                        List<String> lights = hueHelper.getLightListFromGroup(lightGroupIdentifier, context);

                        // Set Max to light size
                        int max = lights.size();

                        // If Light ALL then add one to end so that all lights could be updated some of the times
                        if (getRandomLight()[i] == Effect.RANDOM_LIGHT_ALL)
                            max++;

                        // Will update 1 light for every five lights (plus 1 for less than five)
                        // So if user has 16 lights 3 will get updated, 10 lights 3 will get updated.
                        int lightsToUpdate = (max / 5) + 1;
                        Log.d(TAG, "Number of lights to update " + lightsToUpdate);

                        for (int ii = 0; ii < lightsToUpdate; ii++) {
                            int randomLight;

                            if (prevRandom != -1) {
                                randomLight = prevRandom;

                                // Reset previous to -1
                                prevRandom = -1;
                            } else {
                                Random rand = new Random();
                                randomLight = rand.nextInt(max);

                                try {
                                    if (getSleep()[i + 1] < 1)
                                        prevRandom = randomLight;
                                } catch (Exception e) {
                                    Log.e(TAG, "Error while looking up sleep time for next value " + e.toString());
                                }
                            }

                            Log.i(TAG, "Random Light : " + randomLight + " of max " + max);

                        /*// If equals max then we update all teh lights in the group
                        if(bridge == null) {
                            if (phHueSDK == null) phHueSDK = PHHueSDK.create();
                            bridge = phHueSDK.getSelectedBridge();
                        }*/

                            // If this is ALL Lights possible update and Max is reached, then update all Lights
                            // Otherwise just update individual
                            if (getRandomLight()[i] == Effect.RANDOM_LIGHT_ALL && randomLight >= (max - 1)) {
                                Log.d(TAG, "Updating whole group");
                                //bridge.setLightStateForGroup(lightGroupIdentifier, newLightState);
                                hueHelper.updateLightGroup(context, lightGroupIdentifier, newLightState);
                                break;
                            } else {
                                //PHLight light = bridge.getResourceCache().getLights().get(lights.get(randomLight));
                                //bridge.updateLightState(light, newLightState);

                                // Update individual Light
                                hueHelper.updateLight(context, lights.get(randomLight), newLightState);
                            }
                        }
                    } else {
                    /*if(bridge == null) {
                        if (phHueSDK == null) phHueSDK = PHHueSDK.create();
                        bridge = phHueSDK.getSelectedBridge();
                    }

                    bridge.setLightStateForGroup(lightGroupIdentifier, newLightState);*/

                        hueHelper.updateLightGroup(context, lightGroupIdentifier, newLightState);
                    }
                } else {
                /*if(bridge == null) {
                    if (phHueSDK == null) phHueSDK = PHHueSDK.create();
                    bridge = phHueSDK.getSelectedBridge();
                }

                bridge.setLightStateForGroup(lightGroupIdentifier, newLightState);*/

                    hueHelper.updateLightGroup(context, lightGroupIdentifier, newLightState);
                }


                // Sleep the thread for specified time as per effect specification
                // Then will cycle through the next iteration of colors
                double sleepy = getSleep()[i];
                Log.d(TAG, "Sleep Time " + sleepy);
                try {
                    if (sleepy >= 1) {
                        for (int ii = 0; ii < sleepy; ii++) {
                            Thread.sleep(1000);

                        /*if (!isPlaying) {
                            Log.d(TAG, "Breaking out of sleep as effect was stopped");
                            break;
                        }*/
                        }
                    } else {
                        Thread.sleep((long) (sleepy * 1000));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error while trying to sleep");
                    e.printStackTrace();
                }

                // Exit the thread if no longer playing
                if (interrupted) {
                    Log.d(TAG, "Stopping thread");
                    SoundPlayer.stopSound();
                    //Thread.interrupted();

                /*try {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                btnEffectPlayStop.setText("Play");
                                txtLoopNumber.setVisibility(View.INVISIBLE);
                            } catch (Exception e) {
                                Log.e(TAG, "Error while generating effect");
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error while generating effect");
                    e.printStackTrace();
                }*/

                    break;
                }
            }
        }
    }

    private void countryEffect(Context context, String lightGroupId) {
        Log.i(TAG, "countryEffect");

        HueHelper hueHelper = new HueHelper();

        // Get list of all the lights that will need to be updated from within this group.
        List <String> lightList = hueHelper.getLightListFromGroup(lightGroupId, context);

        Log.i(TAG, "Total Lights to update - " + lightList.size());

        // Shuffle the light list so it is always different lights that update
        Log.d(TAG, "Shuffling the lights");
        Collections.shuffle(lightList);

        int totalColors = hue.length; // This get the total number of colors that were in the int [] for updating
        int count = 0;
        for(String light : lightList) {

            PHLightState newLightState = new PHLightState();
            newLightState.setHue(hue[count]);
            newLightState.setSaturation(saturation[count]);
            newLightState.setBrightness(brightness[count]);

            hueHelper.updateLight(context,light,newLightState);

            // Update count, if it is over the total for number of lights then we need to reset to 0 and restart
            count++;
            if(count >= totalColors) {count = 0;}
        }

    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getHue() {
        return hue;
    }

    public void setHue(int[] hue) {
        this.hue = hue;
    }

    public void setHue(String hue) {
        this.hue = new int[hue.split(",").length];
        int i = 0;
        for (String x : hue.split(",")) {
            this.hue[i++] = Integer.parseInt(x);
        }
    }

    public int[] getSaturation() {
        return saturation;
    }

    public void setSaturation(int[] saturation) {
        this.saturation = saturation;
    }

    public void setSaturation(String saturation) {
        this.saturation = new int[saturation.split(",").length];
        int i = 0;
        for (String x : saturation.split(",")) {
            this.saturation[i++] = Integer.parseInt(x);
        }
    }

    public int[] getBrightness() {
        return brightness;
    }

    public void setBrightness(int[] brightness) {
        this.brightness = brightness;
    }

    public void setBrightness(String brightness) {
        this.brightness = new int[brightness.split(",").length];
        int i = 0;
        for (String x : brightness.split(",")) {
            this.brightness[i++] = Integer.parseInt(x);
        }
    }

    public double[] getSleep() {
        return sleep;
    }

    public void setSleep(double[] sleep) {
        this.sleep = sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = new double[sleep.split(",").length];
        int i = 0;
        for (String x : sleep.split(",")) {
            this.sleep[i++] = Double.parseDouble(x);
        }
    }

    public void setTransitionTime(String transitionTime) {

        if (transitionTime != null) {
            isCustomTransitions = true;

            this.transitionTime = new int[transitionTime.split(",").length];
            int i = 0;
            for (String x : transitionTime.split(",")) {
                this.transitionTime[i++] = Integer.parseInt(x);
            }
        } else {
            isCustomTransitions = false;
            this.transitionTime = null;
        }

    }

    public void setRandomLight(String randomLight) {

        if (randomLight != null) {
            isRandomSupported = true;

            this.randomLight = new int[randomLight.split(",").length];
            int i = 0;
            for (String x : randomLight.split(",")) {
                this.randomLight[i++] = Integer.parseInt(x);
            }
        } else {
            isRandomSupported = false;
            this.randomLight = null;
        }
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getTimesClicked() {
        return timesClicked;
    }

    public void setTimesClicked(int timesClicked) {
        this.timesClicked = timesClicked;
    }

    public String getSoundId() {
        return soundId;
    }

    public void setSoundId(String soundId) {
        this.soundId = soundId;
    }


    public int[] getRandomLight() {
        return randomLight;
    }

    public void setRandomLight(int[] randomLight) {
        this.randomLight = randomLight;

        if (randomLight != null)
            isRandomSupported = true;
        else
            isRandomSupported = false;
    }

    public int[] getTransitionTime() {
        return transitionTime;
    }

    public void setTransitionTime(int[] transitionTime) {
        this.transitionTime = transitionTime;

        if (transitionTime != null)
            isCustomTransitions = true;
        else
            isCustomTransitions = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRandomSupported() {
        return isRandomSupported;
    }

    public boolean isCustomTransitions() {
        return isCustomTransitions;
    }
}

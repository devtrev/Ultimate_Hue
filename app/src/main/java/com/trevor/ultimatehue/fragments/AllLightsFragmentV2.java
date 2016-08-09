package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.colorPicker.ColorPickerActivityVListActivity;
import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;

import java.util.List;

public class AllLightsFragmentV2 extends Fragment {

    public static final String TAG = AllLightsFragmentV2.class.toString();

    public static final int ALL_LIGHTS_ACTIVITY_RESULT = 99;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private LayoutInflater inflater;
    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private Context context;
    private SQLiteDatabase db;

    private TableLayout tableLayout;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AllLightsFragmentV2 newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        AllLightsFragmentV2 fragment = new AllLightsFragmentV2();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AllLightsFragmentV2() {
    }

    //http://www.porn.com/videos/two-teens-are-bored-so-they-decide-to-have-lesbo-sex-2092277
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_all_lights, container, false);

        // Setup variable
        context = container.getContext();
        this.inflater = inflater;
        tableLayout = (TableLayout) rootView.findViewById(R.id.all_lights_table);
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        // Load the table
        populateAllLights();

        showFirstTimeHelpMessage();

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    /**
     * Get the Color Result and process
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ALL_LIGHTS_ACTIVITY_RESULT) {
            Log.i(TAG, "Color Result Returned for Light");

            try {
                int[] hueColors = new int[3];
                hueColors[0] = data.getExtras().getInt(Constants.HUE);
                hueColors[1] = data.getExtras().getInt(Constants.SATURATION);
                hueColors[2] = data.getExtras().getInt(Constants.BRIGHTNESS);
                String lightIdentifier = data.getExtras().getString(Constants.LIGHT_IDENTIFIER);

                Log.i(TAG, "Color Returned = " + hueColors[0] +
                        "\nLight Returned = " + lightIdentifier);

                updateIndividualLight(lightIdentifier, hueColors);

                // Update the entire table
                refreshTable();

            } catch (Exception e) {
                Log.e(TAG, "Error while process activity result for updating light");
                e.printStackTrace();
            }
        } else if (resultCode == EffectsFragment.EFFECT_PICKER_RESULT) { // Country Effect Returned
            Log.i(TAG, "Effect Result Returned for Light Group");

            String effectType = data.getExtras().getString(Constants.COLOR_TYPE);
            int [] hue = data.getExtras().getIntArray(Constants.HUE);
            int [] saturation = data.getExtras().getIntArray(Constants.SATURATION);
            int [] brightness = data.getExtras().getIntArray(Constants.BRIGHTNESS);
            String lightIdentifier = data.getExtras().getString(Constants.LIGHT_IDENTIFIER);

            if (effectType.equals(Constants.COUNTRY_EFFECT_COLOR_TYPE)) {
                Log.d(TAG, "Type of Effect : " + Constants.COUNTRY_EFFECT_COLOR_TYPE);

                // Update lights for countryEffect
                countryEffect(hue[0], saturation[0], brightness[0], lightIdentifier);
            }

        }
    }

    public void populateAllLights() {
        Log.d(TAG, "populateAllLights()");

        View v = inflater.inflate(R.layout.light_row_header, null);
        tableLayout.addView(v);

        // Make sure that bridge is not null -- Fix in version 1.2.2
        if(phHueSDK == null)
            phHueSDK = PHHueSDK.create();
        if(bridge == null)
            bridge = phHueSDK.getSelectedBridge();
        // End Fix for bridge being null

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        for (final PHLight light : allLights) {
            try {
                Log.i(TAG, "Looping through the Lights");

                // Create list of all groups
                final View row = inflater.inflate(R.layout.light_row, null);

                Switch lightSwitch = ((Switch) row.findViewById(R.id.group_switch));
                ImageButton chooseColor = (ImageButton) row.findViewById(R.id.group_color);
                SeekBar brightness = (SeekBar) row.findViewById(R.id.group_brightness);
                PHLightState lightState = light.getLastKnownLightState();

                if (light.supportsColor()) {
                    chooseColor.setImageResource(getImageResourceByCurrentHue(lightState.getHue()));
                } else {
                    Log.i(TAG, "Bulb does not support Color");
                    chooseColor.setImageResource(R.mipmap.ac_standard);
                }

                lightSwitch.setText(light.getName());
                if(lightState != null) {
                    lightSwitch.setChecked(lightState.isOn());
                    Log.d(TAG, "Last state = " + lightState.isOn());

                    if (lightState.getBrightness() == null)
                        brightness.setProgress(254);
                    else
                        brightness.setProgress(lightState.getBrightness());
                } else {
                    Log.w(TAG, "AHH BOGART... for some reason state is messed up");
                    brightness.setProgress(254);
                }
                tableLayout.addView(row);

                // Set the listener for if on/off switch is hit. Not sure if this is in fact the
                // Best way to do it but it seems to be working.
                lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        Log.i(TAG, "Light on/off switch hit");

                        PHLightState state = new PHLightState();
                        state.setOn(isChecked);

                        bridge.updateLightState(light, state);
                    }
                });

                chooseColor.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Update Color : onClick - Starting new Activity for Result");

                        if(light.supportsColor()) {

                            // Launch ColorPicker Activity for selecting a new Color
                            Intent intent = new Intent(row.getContext(), ColorPickerActivityVListActivity.class);
                            intent.putExtra(Constants.LIGHT_IDENTIFIER, light.getIdentifier());
                            intent.putExtra(Constants.IS_GROUP, false);
                            startActivityForResult(intent, ALL_LIGHTS_ACTIVITY_RESULT);

                        } else {
                            Log.d(TAG, "Change Color for Non color supported bulb clicked");

                            Toast.makeText(getContext(), "This Bulb does not support Color", Toast.LENGTH_SHORT).show();
                        }
                    }

                });

                brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d(TAG, "Brightness onStopTrackingTouch - Progress = " + seekBar.getProgress());

                        PHLightState state = new PHLightState();
                        state.setBrightness(seekBar.getProgress());

                        updateIndividualLight(light.getIdentifier(), state);

                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }

    }

    private void updateIndividualLight(String lightIdentifier, PHLightState lightState) {
        Log.d(TAG, "Updating the lights to Brightness");

        try {
            if(phHueSDK == null)
                phHueSDK = PHHueSDK.create();
            if(bridge == null)
                bridge = phHueSDK.getSelectedBridge();

            PHLight light = bridge.getResourceCache().getLights().get(lightIdentifier);

            if (!light.getLastKnownLightState().isOn()) {
                lightState.setOn(true);
            }

            bridge.updateLightState(light, lightState);
        } catch (Exception e) {
            Log.e(TAG, "Error while updating lights - Throwing Exception : " + e.toString());
            e.printStackTrace();
        }
    }

    private void updateIndividualLight(String lightIdentifier, int[] hueColors) throws Exception {
        Log.d(TAG, "Updating the lights to new Hue : " + hueColors[0] +
                "\nLight Identifier : " + lightIdentifier);

        try {
            if(phHueSDK == null)
                phHueSDK = PHHueSDK.create();
            if(bridge == null)
                bridge = phHueSDK.getSelectedBridge();

            PHLight light = bridge.getResourceCache().getLights().get(lightIdentifier);

            PHLightState state = new PHLightState();
            state.setHue(hueColors[0]);
            state.setSaturation(hueColors[1]);
            state.setBrightness(hueColors[2]);
            state.setOn(true);

            bridge.updateLightState(light, state);
        } catch (Exception e) {
            Log.e(TAG, "Error while updating lights - Throwing Exception : " + e.toString());
            throw e;
        }
    }

    private void countryEffect(int hue, int saturation, int brightness, String lightIdentifier) {
        Log.i(TAG, "countryEffect");

        PHLightState newLightState = new PHLightState();
        newLightState.setHue(hue);
        newLightState.setSaturation(saturation);
        newLightState.setBrightness(brightness);

        updateIndividualLight(lightIdentifier, newLightState);


        refreshTable();
    }

    private int getImageResourceByCurrentHue(int currentHue) {
        Log.d(TAG, "getImageResourceByCurrentHue");

        db = (DatabaseHelper.getInstance(context)).getWritableDatabase();
        String imageId =  ColorFactory.getClosestLightImageByHue(db, currentHue);
        db.close();

        return getResources().getIdentifier(imageId, "mipmap", getContext().getPackageName());
        //return imageId;
    }

    private void refreshTable() {
        Log.i(TAG, "refreshTable()");

        try {
            tableLayout.removeAllViews();

            // Have to sleep temporarily due to loading to fast
            Thread.sleep(100);

            populateAllLights();
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing the table");
            e.printStackTrace();
        }
    }

    public void startMainActivity() {
        Log.d(TAG, "startMainActivity");
        Intent intent = new Intent(getContext(), com.philips.lighting.quickstart.PHHomeActivity.class);
        startActivity(intent);
    }

    private void showFirstTimeHelpMessage() {
        openDatabase();

        String showHelpMessage = SettingsFactory.getSetting(db, Constants.SETTING_INDIVIDUAL_HELP);
        if(showHelpMessage.equalsIgnoreCase("false") || showHelpMessage.equalsIgnoreCase("-1")) {
            showHelpDialog();

            SettingsFactory.updateOrInsert(db, Constants.SETTING_INDIVIDUAL_HELP, "true");
        }

        closeDatabase();
    }

    private void showHelpDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Ultimate Hue");
        alert.setMessage("On this screen you can :" +
                "\nUpdate Light colors - Simply click the image button to show list of colors" +
                "\nTurn Lights on/off by clicking the on off switch" +
                "\nAdjust brightness");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    private void closeDatabase() {
        Log.i(TAG, "closeDatabase()");
        // Check if Database is closed, if not then close
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    private void openDatabase() {
        Log.i(TAG, "openDatabase()");
        // Check if Database is open, if not then open
        if (db == null || !db.isOpen()) {
            db = (DatabaseHelper.getInstance(getContext())).getWritableDatabase();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(db != null)
            db.releaseReference();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (phHueSDK == null) {
            phHueSDK = PHHueSDK.create();
        }

        if (phHueSDK.getSelectedBridge() == null) {
            Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

            startMainActivity();
        } else {
            bridge = phHueSDK.getSelectedBridge();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (bridge != null) {

            if (phHueSDK != null) {
                if (phHueSDK.isHeartbeatEnabled(bridge)) {
                    phHueSDK.disableHeartbeat(bridge);
                }

                phHueSDK.disconnect(bridge);
            }
        }
    }
}

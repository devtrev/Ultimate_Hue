package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.R;

import java.util.List;

public class GetHueFragment extends Fragment {

    private static final String TAG = GetHueFragment.class.toString();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    private EditText hueSetting;
    private TextView hueSettingText;
    private EditText saturationSetting;
    private TextView saturationText;
    private EditText brightnessSetting;
    private TextView brightnessSettingText;
    private Button startButton;
    private Button stopButton;

    private Boolean isKilled;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static GetHueFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        GetHueFragment fragment = new GetHueFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public GetHueFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_get_hue, container, false);

        // Get HUE SDK
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        hueSetting = (EditText) rootView.findViewById(R.id.hueEditText);
        hueSettingText = (TextView) rootView.findViewById(R.id.hueText);
        saturationSetting = (EditText) rootView.findViewById(R.id.saturationEditText);
        saturationText = (TextView) rootView.findViewById(R.id.saturationText);
        brightnessSetting = (EditText) rootView.findViewById(R.id.brightnessEditText);
        brightnessSettingText = (TextView) rootView.findViewById(R.id.brightnessText);
        startButton = (Button) rootView.findViewById(R.id.startButton);
        stopButton = (Button) rootView.findViewById(R.id.stopButton);

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getHue(v);

            }

        });

        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setHue(v);

            }

        });

        return rootView;
    }

    public void getHue(View v) {
        Log.i(TAG, "getHue()");

        for (PHLight light : bridge.getResourceCache().getAllLights()) {
            PHLightState state = light.getLastKnownLightState();

            if(state != null && light.supportsColor()) {
                hueSetting.setText(state.getHue().toString());
                saturationSetting.setText(state.getSaturation().toString());
                brightnessSetting.setText(state.getBrightness().toString());

                break;
            }


        }
    }

    public void setHue(View v) {
        Log.i(TAG, "setHue()");

        try {
            int [] huesettings = new int[3];
            huesettings[0] = Integer.parseInt(hueSetting.getText().toString());
            huesettings[1] = Integer.parseInt(saturationSetting.getText().toString());
            huesettings[2] = Integer.parseInt(brightnessSetting.getText().toString());

            List<PHLight> allLights = bridge.getResourceCache().getAllLights();

            updateLights(allLights.get(1).getIdentifier() , huesettings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLights(String lightIdentifier, int[] hueColors) throws Exception {
        Log.d(TAG, "Updating the lights to new Hue : " + hueColors[0] +
                "\nLight Identifier : " + lightIdentifier);

        try {
            for (PHLight light : bridge.getResourceCache().getAllLights()) {
                // Since this is for an individual light only update the one light that matches this Identifier
                if (light.getIdentifier().trim().equals(lightIdentifier.trim())) {
                    Log.d(TAG, "Found lightIdentifier " + lightIdentifier + " and updating the light");

                    PHLightState state = new PHLightState();
                    state.setHue(hueColors[0]);
                    state.setSaturation(hueColors[1]);
                    state.setBrightness(hueColors[2]);

                    // If light is not turned on Already then turn on :)
                    if (!light.getLastKnownLightState().isOn()) {
                        state.setOn(true);
                    }

                    bridge.updateLightState(light, state);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while updating lights - Throwing Exception : " + e.toString());
            throw e;
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }


}

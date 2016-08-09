package com.trevor.ultimatehue;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.fragments.AllLightsFragment;
import com.trevor.ultimatehue.fragments.CustomColorFragment;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.ColorPickerView;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.OnColorSelectedListener;

public class CustomColorActivity extends AppCompatActivity {

    private static final String TAG = CustomColorActivity.class.toString();

    private ColorPickerView colorPicker;
    private Button button;
    private SeekBar seekBarSaturation;
    private SeekBar seekBarBrightness;
    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    private boolean isGroup;
    private String lightIdentifier;
    private boolean updateLights;
    private boolean isUpdate;
    private int timesClicked;
    private int favorite;
    private long id;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_color);

        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            lightIdentifier = extras.getString(Constants.LIGHT_IDENTIFIER, "-1");
            isGroup = extras.getBoolean(Constants.IS_GROUP);
            isUpdate = extras.getBoolean(Constants.IS_UPDATE);
            timesClicked = extras.getInt(Constants.TIMES_CLICKED);
            favorite = extras.getInt(Constants.FAVORITE);
            id = extras.getLong(Constants.ID);

            if(isUpdate) {

                PHLightState lightState = new PHLightState();
                lightState.setHue(extras.getInt(Constants.HUE));
                lightState.setSaturation(extras.getInt(Constants.SATURATION));
                lightState.setBrightness(extras.getInt(Constants.BRIGHTNESS));

                name = extras.getString(Constants.CUSTOM_COLOR_NAME);

                if(isGroup) {
                    groupLightUpdate(lightState);
                } else {
                    soloLightUpdate(lightState);
                }
            }
        } else {
            lightIdentifier = "-1";
            isGroup = false;
            isUpdate = false;
            timesClicked = 0;
            favorite = 0;
            id = -1;
            name = "";
        }

        updateLights = true;
        if(lightIdentifier.equals("-1"))
            updateLights = false;

        Log.i(TAG, "lightIdentifier=" + lightIdentifier + " isGroup=" + isGroup + " updateLights=" + updateLights);

        findViewsById();
        setOnClickListeners();
        setDefaults();

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getApplication(), getClass().getSimpleName());
    }

    private void findViewsById() {
        Log.d(TAG, "findViewsById()");

        colorPicker = (ColorPickerView) findViewById(R.id.colorPicker);
        button = (Button) findViewById(R.id.button);
        seekBarSaturation = (SeekBar) findViewById(R.id.seekBarSaturation);
        seekBarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners()");

        colorPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch");

                int hue = colorPicker.getHue();
                int saturation = seekBarSaturation.getProgress();
                int brightness = seekBarBrightness.getProgress();

                PHLightState lightState = new PHLightState();
                lightState.setHue(hue);
                lightState.setSaturation(saturation);
                lightState.setBrightness(brightness);

                if(updateLights) {
                    if(isGroup) {
                        groupLightUpdate(lightState);
                    } else {
                        soloLightUpdate(lightState);
                    }
                }

                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int hue = colorPicker.getHue();
                int saturation = seekBarSaturation.getProgress();
                int brightness = seekBarBrightness.getProgress();

                Log.d(TAG, "Setting new color - hue=" + hue + " saturation=" + saturation + " brightness=" + brightness);
                setColor(hue, saturation, brightness);
            }
        });

        seekBarSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Saturation onStopTrackingTouch - Progress = " + seekBar.getProgress());

                int hue = colorPicker.getHue();
                int saturation = seekBarSaturation.getProgress();
                int brightness = seekBarBrightness.getProgress();

                PHLightState lightState = new PHLightState();
                lightState.setHue(hue);
                lightState.setSaturation(saturation);
                lightState.setBrightness(brightness);

                if(updateLights) {
                    if(isGroup) {
                        groupLightUpdate(lightState);
                    } else {
                        soloLightUpdate(lightState);
                    }
                }

            }
        });

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Brightness onStopTrackingTouch - Progress = " + seekBar.getProgress());

                int hue = colorPicker.getHue();
                int saturation = seekBarSaturation.getProgress();
                int brightness = seekBarBrightness.getProgress();

                PHLightState lightState = new PHLightState();
                lightState.setHue(hue);
                lightState.setSaturation(saturation);
                lightState.setBrightness(brightness);

                if(updateLights) {
                    if(isGroup) {
                        groupLightUpdate(lightState);
                    } else {
                        soloLightUpdate(lightState);
                    }
                }
            }
        });
    }

    private void setDefaults() {
        if(isGroup) {
            PHLightState lightState = bridge.getResourceCache().getLights().get(bridge.getResourceCache().getGroups().get(lightIdentifier).getLightIdentifiers().get(0)).getLastKnownLightState();

            colorPicker.setInitialColor(lightState.getHue());
            seekBarSaturation.setProgress(lightState.getSaturation());
            seekBarBrightness.setProgress(lightState.getBrightness());
        } else {
            PHLightState lightState = bridge.getResourceCache().getLights().get(lightIdentifier).getLastKnownLightState();

            colorPicker.setInitialColor(lightState.getHue());
            seekBarSaturation.setProgress(lightState.getSaturation());
            seekBarBrightness.setProgress(lightState.getBrightness());
        }
    }

    private void groupLightUpdate(PHLightState lightState) {
        Log.i(TAG, "groupLightUpdate ");

        bridge.setLightStateForGroup(lightIdentifier , lightState);
    }

    private void soloLightUpdate(PHLightState lightState) {
        Log.i(TAG, "soloLightUpdate ");

        PHLight light = bridge.getResourceCache().getLights().get(lightIdentifier);
        bridge.updateLightState(light, lightState);
    }

    public void setColor(final int hue, final int sat, final int brightness) {
        Log.i(TAG, "setColor ");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Light Name");
        alert.setMessage("Enter a name for the light");

        // Set an EditText view to get user lightName
        final EditText lightName = new EditText(this);
        lightName.setText(name);
        alert.setView(lightName);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                Intent intent = new Intent(getApplicationContext(), CustomColorFragment.class);
                intent.putExtra(Constants.HUE, hue);
                intent.putExtra(Constants.SATURATION, sat);
                intent.putExtra(Constants.BRIGHTNESS, brightness);
                intent.putExtra(Constants.HUE_IMAGE, -1);
                intent.putExtra(Constants.LIGHT_IDENTIFIER, lightIdentifier);
                intent.putExtra(Constants.CUSTOM_COLOR_NAME, lightName.getText().toString().trim());
                intent.putExtra(Constants.IS_UPDATE, isUpdate);

                if(isUpdate) {
                    intent.putExtra(Constants.ID, id);
                    intent.putExtra(Constants.TIMES_CLICKED, timesClicked);
                    intent.putExtra(Constants.FAVORITE, favorite);
                } else {

                    intent.putExtra(Constants.TIMES_CLICKED, 0);
                    intent.putExtra(Constants.FAVORITE, 0);
                }

                setResult(Constants.CUSTOM_COLOR_ACTIVITY, intent);

                AnalyticsHelper application = (AnalyticsHelper) getApplication();
                String category = "Custom Color";
                String action = "New Color";
                String label = lightName.getText().toString().trim() + " HUE=" + hue;

                Log.i(TAG, "Custom Color Activity Analytics");
                AnalyticsHelper.analyticsEvent(application, category,action,label);

                finish();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_custom_color, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

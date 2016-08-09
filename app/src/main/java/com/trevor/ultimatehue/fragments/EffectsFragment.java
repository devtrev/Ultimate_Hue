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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.EffectPickerActivity;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.NewGroupActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.EffectsFactory;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.lights.LightGroup;
import com.trevor.ultimatehue.music.SoundPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EffectsFragment extends Fragment {

    private static final String TAG = EffectsFragment.class.toString();
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final int NEW_GROUP_ACTIVITY = 151;
    public static final int EFFECT_PICKER_RESULT = 50;

    private boolean isPlaying;
    private int timesToLoop;
    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private SQLiteDatabase db;
    private Context context;
    private LayoutInflater inflater;
    private List<LightGroup> allGroups;
    private int position;
    private Effect effect;
    private String lightGroupIdentifier;
    private View rootView;
    private int loopCounter;

    private ImageButton btnEffectPicked;
    private CheckBox chkLoopIndefinately;
    private CheckBox chkPlayWithSound;
    private EditText txtTimesToLoop;
    private Button btnEffectPlayStop;
    private Spinner spnLightGroups;
    private TextView txtLoopNumber;
    private TextView txtEffectLength;
    private TextView txtEffectDescription;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static EffectsFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        EffectsFragment fragment = new EffectsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public EffectsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = container.getContext();
        this.inflater = inflater;
        rootView = this.inflater.inflate(R.layout.fragment_effects, container, false);

        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();
        isPlaying = false;
        position = 0;
        allGroups = new ArrayList<>();

        openDatabase();

        findViewsById();
        populateView();
        setupListeners();

        showFirstTimeHelpMessage();

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        return rootView;
    }

    private void findViewsById() {
        Log.d(TAG, "findViewsById");

        btnEffectPicked = (ImageButton) rootView.findViewById(R.id.btnEffectPicked);
        chkLoopIndefinately = (CheckBox) rootView.findViewById(R.id.chkLoopIndefinately);
        chkPlayWithSound = (CheckBox) rootView.findViewById(R.id.chkPlayWithSound);
        txtTimesToLoop = (EditText) rootView.findViewById(R.id.txtTimesToLoop);
        btnEffectPlayStop = (Button) rootView.findViewById(R.id.btnEffectPlayStop);
        spnLightGroups = (Spinner) rootView.findViewById(R.id.spnLightGroups);
        txtLoopNumber = (TextView) rootView.findViewById(R.id.txtLoopNumber);
        txtEffectLength = (TextView) rootView.findViewById(R.id.txtEffectLength);
        txtEffectDescription = (TextView) rootView.findViewById(R.id.txtEffectDescription);
    }

    private void populateView() {
        Log.d(TAG, "populateView");

        List<PHGroup> phGroups = bridge.getResourceCache().getAllGroups();
        for (PHGroup phGroup : phGroups) {
            allGroups.add(new LightGroup(phGroup));
        }

        if(allGroups.size() > 0) {
            allGroups = sort(allGroups);

            Log.d(TAG, "There are " + allGroups.size() + " groups");
            String[] groupStringArray = new String[allGroups.size()];
            int i = 0;
            for (final LightGroup group : allGroups) {
                groupStringArray[i] = group.getPhGroup().getName();
                i++;
            }
            openDatabase();

            int lastGroup = Integer.parseInt(SettingsFactory.getSetting(db, Constants.SETTING_LAST_GROUP));

            // This catches scenario where previous group may have been deleted, then we just make it 0 to be safe
            if (lastGroup >= allGroups.size())
                lastGroup = 0;

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, groupStringArray);
            spnLightGroups.setAdapter(adapter);
            spnLightGroups.setSelection(lastGroup);

        } else {
            Log.w(TAG, "No groups exist - ask user to create one");
            showCreateGroupDialog();
        }

        String lastEffect = SettingsFactory.getSetting(db, Constants.SETTING_LAST_EFFECT);

        effect = EffectsFactory.getEffect(db, lastEffect);
        txtTimesToLoop.setText(SettingsFactory.getSetting(db, Constants.SETTING_EFFECT_TIMES_TO_LOOP));

        if(effect != null) {
            if (effect.getSoundId().trim().length() > 0) {
                chkPlayWithSound.setChecked(Boolean.valueOf(SettingsFactory.getSetting(db, Constants.SETTING_LAST_PLAY_SOUND)));
                chkPlayWithSound.setEnabled(true);
            } else {
                chkPlayWithSound.setChecked(false);
                chkPlayWithSound.setEnabled(false);
            }

            // Set the ImageButton
            btnEffectPicked.setImageResource(getContext().getResources().getIdentifier(effect.getImageId(), "mipmap" , getContext().getPackageName()));

            txtLoopNumber.setVisibility(View.INVISIBLE);
            txtEffectLength.setText("Effect is " + effect.getTotalEffectTime() + " seconds per loop");
            txtEffectDescription.setText(effect.getDescription());
        }

        closeDatabase();
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners");

        // Pick Effect Button Listener
        btnEffectPicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnEffectPicked onClick");

                // load the Open a tabbed EffectPickerActivity
                pickEffect();
            }
        });

        // Play / Stop effect Listener
        btnEffectPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnEffectPlayStop onClick");

                // Play or stop the current effect
                playOrStop();
            }
        });

        chkLoopIndefinately.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                           @Override
                                                           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                               Log.d(TAG, "chkLoopIndefinately onCheckedChanged");

                                                               // Check state changed, process changes in the fragment
                                                               checkStateUpdate(isChecked);
                                                           }
                                                       }
        );

    }

    private void pickEffect() {
        Log.d(TAG, "pickEffect");

        Intent intent = new Intent(context, EffectPickerActivity.class);
        startActivityForResult(intent, EFFECT_PICKER_RESULT);
    }

    private void playOrStop() {
        Log.d(TAG, "playOrStop");

        if(allGroups.size() <= 0) {
            Log.w(TAG, "No groups exist - ask user to create one");

            showCreateGroupDialog();
        } else { // We are good to proceed and play the sound
            try {
                isPlaying = !isPlaying;

                if (isPlaying) {
                    btnEffectPlayStop.setText("Stop");

                    if (chkLoopIndefinately.isChecked())
                        timesToLoop = Integer.MAX_VALUE;
                    else {
                        timesToLoop = Integer.parseInt(txtTimesToLoop.getText().toString().trim());
                        openDatabase();
                        SettingsFactory.updateSetting(db, Constants.SETTING_EFFECT_TIMES_TO_LOOP, String.valueOf(timesToLoop));
                        closeDatabase();
                    }

                    position = spnLightGroups.getSelectedItemPosition();
                    txtLoopNumber.setVisibility(View.VISIBLE);

                    Log.d(TAG, "Position = " + position + " : Number of groups = " + allGroups.size());
                    if (allGroups.get(position) != null && timesToLoop > 0) {
                        lightGroupIdentifier = allGroups.get(position).getPhGroup().getIdentifier();
                        start();
                    } else {
                        Log.e(TAG, "Not starting due to error with inputs");
                    }
                } else {
                    btnEffectPlayStop.setText("Play");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Error while attempting to play effect : " + e.toString());
                e.printStackTrace();


            }
        }
    }

    private void checkStateUpdate(boolean isChecked) {
        Log.d(TAG, "checkStateUpdate");

        txtTimesToLoop.setEnabled(!isChecked);
    }

    private void start() {
        Log.d(TAG, "start");

        Log.i(TAG, "Creating an Effect : " + effect.getName());

        boolean playSound;

        if (effect.getSoundId().trim().length() > 0) {
            playSound = chkPlayWithSound.isChecked();

            openDatabase();
            SettingsFactory.updateSetting(db, Constants.SETTING_LAST_PLAY_SOUND, String.valueOf(chkPlayWithSound.isChecked()));
            SettingsFactory.updateSetting(db, Constants.SETTING_LAST_GROUP, String.valueOf(position));
            closeDatabase();
        } else {
            playSound = false;
        }

        playEffectThread(playSound);

        AnalyticsHelper application = (AnalyticsHelper) getActivity().getApplication();
        String category = "Effect Player";
        String action = "Playing Effect";
        String label = effect.getName();

        AnalyticsHelper.analyticsEvent(application, category, action, label);

    }

    /**
     * Get the Color Result and process
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == EFFECT_PICKER_RESULT) {
            Log.i(TAG, "Effect result returned");

            effect = new Effect();

            try {
                // Load the effect that was passed back from the Activity
                effect.setName(data.getExtras().getString(Constants.EFFECT_NAME));
                effect.setHue(data.getExtras().getIntArray(Constants.HUE));
                effect.setSaturation(data.getExtras().getIntArray(Constants.SATURATION));
                effect.setBrightness(data.getExtras().getIntArray(Constants.BRIGHTNESS));
                effect.setSleep(data.getExtras().getDoubleArray(Constants.SLEEP));
                effect.setRandomLight(data.getExtras().getIntArray(Constants.RANDOM_LIGHT));
                effect.setTransitionTime(data.getExtras().getIntArray(Constants.TRANSITION_TIME));
                effect.setImageId(data.getExtras().getString(Constants.HUE_IMAGE));
                effect.setSoundId(data.getExtras().getString(Constants.EFFECT_SOUND));
                effect.setDescription(data.getExtras().getString(Constants.EFFECT_DESCRIPTION));

                btnEffectPicked.setImageResource(getContext().getResources().getIdentifier(effect.getImageId(), "mipmap" , getContext().getPackageName()));
                txtEffectLength.setText("Effect is " + effect.getTotalEffectTime() + " seconds");
                txtEffectDescription.setText(effect.getDescription());

                openDatabase();
                SettingsFactory.updateOrInsert(db, Constants.SETTING_LAST_EFFECT, effect.getName());
                closeDatabase();

                if (effect.getSoundId().trim().length() > 0) {
                    Log.i(TAG, "Sound found for effect");

                    chkPlayWithSound.setEnabled(true);
                    chkPlayWithSound.setText("Play with sound");

                    openDatabase();
                    chkPlayWithSound.setChecked(Boolean.parseBoolean(SettingsFactory.getSetting(db, Constants.SETTING_LAST_PLAY_SOUND)));
                    closeDatabase();
                } else {
                    Log.i(TAG, "No Sound effect found");

                    chkPlayWithSound.setEnabled(false);
                    chkPlayWithSound.setChecked(false);
                    chkPlayWithSound.setText("No sound for effect");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error while process activity result for updating light");

                effect.setSoundId("");
                effect.setImageId("ic_unknown_effect");
                e.printStackTrace();
            }
        } else if (resultCode == NEW_GROUP_ACTIVITY) {
            Log.i(TAG, "New Group created, need to refresh view");

            try {
                Thread.sleep(200);
                populateView();
            } catch (Exception e) {
                Log.e(TAG, "Error repopulating view");
                e.printStackTrace();
            }

        }
    }

    private void playEffectThread(final boolean playSound) {

        new Thread() {
            public void run() {
                loopCounter = 0;

                // Repeat this pattern until told to break
                while (loopCounter < timesToLoop) {

                    Log.d(TAG, "Looping through Effect : " + loopCounter);

                    try {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    txtLoopNumber.setText("Loop " + (loopCounter + 1) + " of " + timesToLoop);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error while generating effect");
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error while generating effect");
                        e.printStackTrace();
                    }

                    if (playSound) {
                        Log.i(TAG, "Playing sound - Looking up from file name");

                        //SoundPlayer.playSound(context, effect.getSoundId());
                        // Removed the above line and added in file name lookup from strings -- Version 1.2.1
                        int raw = getResources().getIdentifier(effect.getSoundId(), "raw", context.getPackageName());
                        SoundPlayer.playSound(context, raw);
                    }

                    int prevRandom = -1;

                    for (int i = 0; i < effect.getHue().length; i++) {
                        Log.d(TAG, "Current Hue is " + effect.getHue()[i]);
                        Log.d(TAG, "Current Saturation is " + effect.getSaturation()[i]);
                        Log.d(TAG, "Current Brightness is " + effect.getBrightness()[i]);
                        Log.d(TAG, "Current Sleep is " + effect.getSleep()[i]);

                        // Update the lights to the current color
                        PHLightState newLightState = new PHLightState();
                        newLightState.setHue(effect.getHue()[i]);
                        newLightState.setSaturation(effect.getSaturation()[i]);
                        newLightState.setBrightness(effect.getBrightness()[i]);

                        // Only set if custom is supported, otherwise use teh default
                        if(effect.isCustomTransitions())
                            newLightState.setTransitionTime(effect.getTransitionTime()[i]);


                        // if random light then need to just update one light, otherwise update all
                        if(effect.isRandomSupported()) {

                            // Check if this particular loop should update individual light
                            if(effect.getRandomLight()[i] != Effect.RANDOM_LIGHT_NONE) {

                                if(bridge == null) {
                                    if (phHueSDK == null) phHueSDK = PHHueSDK.create();
                                    bridge = phHueSDK.getSelectedBridge();
                                }

                                List<String> lights = bridge.getResourceCache().getGroups().get(lightGroupIdentifier).getLightIdentifiers();

                                // Set Max to light size
                                int max = lights.size();

                                // If Light ALL then add one to end so that all lights could be updated some of the times
                                if(effect.getRandomLight()[i] == Effect.RANDOM_LIGHT_ALL)
                                    max++;

                                // Will update 1 light for every five lights (plus 1 for less than five)
                                // So if user has 16 lights 3 will get updated, 10 lights 3 will get updated.
                                int lightsToUpdate = (max / 5) + 1;
                                Log.d(TAG, "Number of lights to update " + lightsToUpdate);

                                for(int ii = 0; ii < lightsToUpdate; ii++) {
                                    int randomLight;

                                    if(prevRandom != -1) {
                                        randomLight = prevRandom;

                                        // Reset previous to -1
                                        prevRandom = -1;
                                    } else {
                                        Random rand = new Random();
                                        randomLight = rand.nextInt(max);

                                        try {
                                            if (effect.getSleep()[i + 1] < 1)
                                                prevRandom = randomLight;
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error while looking up sleep time for next value " + e.toString());
                                        }
                                    }

                                    Log.i(TAG, "Random Light : " + randomLight + " of max " + max);


                                    // If equals max then we update all teh lights in the group
                                    if(bridge == null) {
                                        if (phHueSDK == null) phHueSDK = PHHueSDK.create();
                                        bridge = phHueSDK.getSelectedBridge();
                                    }

                                    // If this is ALL Lights possible update and Max is reached, then update all Lights
                                    // Otherwise just update individual
                                    if (effect.getRandomLight()[i] == Effect.RANDOM_LIGHT_ALL && randomLight >= (max - 1)) {
                                        Log.d(TAG, "Updating whole group");
                                        bridge.setLightStateForGroup(lightGroupIdentifier, newLightState);
                                        break;
                                    } else {
                                        PHLight light = bridge.getResourceCache().getLights().get(lights.get(randomLight));
                                        bridge.updateLightState(light, newLightState);
                                    }
                                }
                            }
                            else {
                                if(bridge == null) {
                                    if (phHueSDK == null) phHueSDK = PHHueSDK.create();
                                    bridge = phHueSDK.getSelectedBridge();
                                }

                                bridge.setLightStateForGroup(lightGroupIdentifier, newLightState);
                            }
                        } else {
                            if(bridge == null) {
                                if (phHueSDK == null) phHueSDK = PHHueSDK.create();
                                bridge = phHueSDK.getSelectedBridge();
                            }

                            bridge.setLightStateForGroup(lightGroupIdentifier, newLightState);
                        }


                        // Sleep the thread for specified time as per effect specification
                        // Then will cycle through the next iteration of colors
                        double sleepy = effect.getSleep()[i];
                        Log.d(TAG, "Sleep Time " + sleepy);
                        try {
                            if (sleepy >= 1) {
                                for (int ii = 0; ii < sleepy; ii++) {
                                    Thread.sleep(1000);

                                    if (!isPlaying) {
                                        Log.d(TAG, "Breaking out of sleep as effect was stopped");
                                        break;
                                    }
                                }
                            } else {
                                Thread.sleep((long) (sleepy * 1000));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error while trying to sleep");
                            e.printStackTrace();
                        }

                        // Exit the thread if no longer playing
                        if (!isPlaying) {
                            Log.d(TAG, "Stopping thread");
                            SoundPlayer.stopSound();
                            //Thread.interrupted();

                            try {
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
                            }

                            break;
                        }
                    }

                    // Stop the larger while loop
                    if(!isPlaying)
                        break;

                    loopCounter++;

                    if (loopCounter == timesToLoop) {
                        isPlaying = !isPlaying;
                        try {
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
                        }
                    }
                }

            }

        }.start();
    }

    private void showCreateGroupDialog() {
            Log.i(TAG, "showCreateGroupDialog");
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

            alert.setTitle("Warning - No Groups");
            alert.setMessage("There are currently no groups created. You will need to create a group before you can use this feature. You can either create one now or later by going to the Light Groups tab and creating a group there. ");

            alert.setPositiveButton("Create Group", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "User is creating new group - Load New Group Activity");

                    Intent intent = new Intent(getActivity().getApplicationContext(), NewGroupActivity.class);
                    intent.putExtra(Constants.IS_UPDATE, false);
                    startActivityForResult(intent, NEW_GROUP_ACTIVITY);
                }
            });
            alert.setNegativeButton("Maybe Later", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "User chose not to create new group, do nothing.");
                }
            });

            alert.show();
    }

    public void startMainActivity() {
        Log.d(TAG, "startMainActivity");
        Intent intent = new Intent(getContext(), com.philips.lighting.quickstart.PHHomeActivity.class);
        startActivity(intent);
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
            db = (DatabaseHelper.getInstance(context)).getWritableDatabase();
        }
    }

    private void showFirstTimeHelpMessage() {
        openDatabase();

        String showHelpMessage = SettingsFactory.getSetting(db, Constants.SETTING_EFFECTS_HELP);
        if(showHelpMessage.equalsIgnoreCase("false") || showHelpMessage.equalsIgnoreCase("-1")) {
            showHelpDialog();

            // If first time then update the image to show click here instead of Red Alert
            btnEffectPicked.setImageResource(getContext().getResources().getIdentifier("ic_unknown_effect", "mipmap", getContext().getPackageName()));
            txtEffectDescription.setText("Click button to the left to Select effect");

            SettingsFactory.updateOrInsert(db, Constants.SETTING_EFFECTS_HELP, "true");
        }

        closeDatabase();
    }

    private void showHelpDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Ultimate Hue");
        alert.setMessage("To change the effects simply click the Button that currently says \"Pick Effect\" " +
                "\nIn order to play the effect simply select play");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
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

        // This will make sure it will at most loop 10 more times, seems legit, better than nothing.
        if(loopCounter + 10 < timesToLoop) {
            Log.w(TAG, "onDestroy - Changing Loop counter to be only 10 more max");
            loopCounter = timesToLoop - 10;
        }

        if (bridge != null) {

            if (phHueSDK != null) {
                if (phHueSDK.isHeartbeatEnabled(bridge)) {
                    phHueSDK.disableHeartbeat(bridge);
                }

                phHueSDK.disconnect(bridge);
            }
        }
    }

    private List<LightGroup> sort(List<LightGroup> groupList) {
        Log.d(TAG, "sort()");
        int lightCount;
        int tempLightCount;

        // Sorts from most lights in group to the fewest
        for(int i = 0; i < groupList.size() - 1; i++) {
            lightCount = groupList.get(i).getPhGroup().getLightIdentifiers().size();
            tempLightCount = groupList.get(i + 1).getPhGroup().getLightIdentifiers().size();

            if (tempLightCount > lightCount) {
                Log.d(TAG, "Attempting to swap");
                LightGroup temp = groupList.get(i);
                groupList.remove(i);
                groupList.add(i + 1, temp);
            }

            for(int ii = 0; ii < groupList.size() - 1; ii++) {
                lightCount = groupList.get(ii).getPhGroup().getLightIdentifiers().size();
                tempLightCount = groupList.get(ii + 1).getPhGroup().getLightIdentifiers().size();

                if (tempLightCount > lightCount) {
                    Log.d(TAG, "Attempting to swap");
                    LightGroup temp = groupList.get(ii);
                    groupList.remove(ii);
                    groupList.add(ii + 1, temp);
                }

            }
        }

        return groupList;
    }
}

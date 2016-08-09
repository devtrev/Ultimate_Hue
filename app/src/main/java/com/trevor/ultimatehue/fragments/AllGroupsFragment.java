package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;


import com.trevor.ultimatehue.ColorPickerActivityGroup;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.NewGroupActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.lights.LightGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllGroupsFragment extends Fragment implements View.OnClickListener {
    public static final int NEW_GROUP_ACTIVITY = 151;
    public static final int UPDATE_GROUP_ACTIVITY = 152;
    public static final int ALL_LIGHTS_ACTIVITY_RESULT = 99;

    private static final String TAG = AllGroupsFragment.class.toString();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PHHueSDK phHueSDK;

    private View [] allRows;
    private LayoutInflater inflater;
    private TableLayout tableLayout;
    //private Button btnAddGroup;
    private PHBridge bridge;

    private SQLiteDatabase db;
    private Context context;
    private List<LightGroup> allGroups;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AllGroupsFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        AllGroupsFragment fragment = new AllGroupsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AllGroupsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "AllGroupsFragment : OnCreateView");


        setHasOptionsMenu(true);
        context = container.getContext();

        this.inflater = inflater;
        View rootView = this.inflater.inflate(R.layout.fragment_all_groups, container, false);

        tableLayout = (TableLayout) rootView.findViewById(R.id.all_groups_table);
        //btnAddGroup = (Button) rootView.findViewById(R.id.btnAddGroup);

        phHueSDK = PHHueSDK.create();

        db = (DatabaseHelper.getInstance(context)).getWritableDatabase();

        // Populate the table with all proper lights
        populateAllGroups();

        db.close();

        showFirstTimeHelpMessage();

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        return rootView;
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

    public void populateAllGroups() {
        Log.d(TAG, "populateAllGroups()");

        if(phHueSDK == null)
            phHueSDK = PHHueSDK.create();

        if (phHueSDK.getSelectedBridge() == null) {
            Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

            startMainActivity();
        } else {

            if (bridge == null)
                bridge = phHueSDK.getSelectedBridge();

            View v = inflater.inflate(R.layout.light_row_group_header, null);
            tableLayout.addView(v);

            allGroups = new ArrayList<>();
            for (PHGroup phGroup : bridge.getResourceCache().getAllGroups()) {
                Log.d(TAG, "Group Name " + phGroup.getName() + "Identifier " + phGroup.getIdentifier());

                allGroups.add(new LightGroup(phGroup));
            }

            if (allGroups.size() > 0) {
                allGroups = sort(allGroups);

                Log.d(TAG, "There are " + allGroups.size() + " groups");

                allRows = new View[allGroups.size()];
                int i = 0;
                for (final LightGroup group : allGroups) {
                    try {
                        Log.i(TAG, "Looping through the Groups - " + group.getPhGroup().getName());

                        // Create list of all groups
                        //final View row = inflater.inflate(R.layout.light_row, null);
                        allRows[i] = inflater.inflate(R.layout.group_light_row, null);

                        final Switch lightGroup = ((Switch) allRows[i].findViewById(R.id.group_switch));
                        final ImageButton chooseColor = (ImageButton) allRows[i].findViewById(R.id.group_color);
                        final SeekBar brightness = (SeekBar) allRows[i].findViewById(R.id.group_brightness);
                        final ImageView editGroup = (ImageView) allRows[i].findViewById(R.id.btnEditGroup);

                        final int lastKnownHue = group.getLastKnownHue(bridge);

                        if (lastKnownHue != -1) {
                            chooseColor.setImageResource(getImageResourceByCurrentHue(lastKnownHue));

                            chooseColor.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Update Group Color : onClick - Starting new Activity for Result");


                                    Log.d(TAG, "Group Light Identifier = " + group.getPhGroup().getIdentifier());
                                    // Launch ColorPicker Activity for selecting a new Color
                                    //Intent intent = new Intent(getContext(), ColorPickerActivityGroup.class);
                                    Intent intent = new Intent(getContext(), ColorPickerActivityGroup.class);
                                    intent.putExtra(Constants.LIGHT_IDENTIFIER, group.getPhGroup().getIdentifier());
                                    startActivityForResult(intent, ALL_LIGHTS_ACTIVITY_RESULT);
                                }

                            });
                        } else {
                            // If it equals -1 then this does not support color
                            chooseColor.setImageResource(R.mipmap.ac_standard);

                            chooseColor.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Change Color for Non color supported group clicked");

                                    Toast.makeText(getContext(), "This group does not support Color", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }

                        lightGroup.setText(group.getPhGroup().getName());
                        lightGroup.setChecked(group.isOn(bridge));

                        brightness.setProgress(group.getBrightness(bridge));

                        tableLayout.addView(allRows[i]);

                        // Set the listener for if on/off switch is hit. Not sure if this is in fact the
                        // Best way to do it but it seems to be working.
                        lightGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,
                                                         boolean isChecked) {
                                Log.i(TAG, "Light Group on/off switch hit");

                                group.updateLights(bridge, isChecked);
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

                                PHLightState lightState = new PHLightState();
                                lightState.setBrightness(seekBar.getProgress());

                                bridge.setLightStateForGroup(group.getPhGroup().getIdentifier(), lightState);
                            }
                        });

                        editGroup.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "Edit Group clicked : " + lightGroup.getText());

                                updateLightGroup(group.getPhGroup().getIdentifier());
                            }
                        });


                    } catch (Exception e) {
                        Log.e(TAG, "Error While initializing AllGroups : " + e.toString(), e);
                    }

                    i++;
                }
            } else {
                Log.w(TAG, "No groups, ask user to create one");

                showCreateGroupDialog();
            }
        }
    }

    /**
     * Get the Color Result and process
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ALL_LIGHTS_ACTIVITY_RESULT) {
            Log.i(TAG, "Color Result Returned for Light Group");

            try {
                // If not Cycle Color Type
                int hue = data.getExtras().getInt(Constants.HUE);
                int saturation = data.getExtras().getInt(Constants.SATURATION);
                int brightness = data.getExtras().getInt(Constants.BRIGHTNESS);
                String lightGroupId = data.getExtras().getString(Constants.LIGHT_IDENTIFIER);

                singleHueColor(hue, saturation, brightness, lightGroupId);

            } catch (Exception e) {
                Log.e(TAG, "Error while process activity result for updating light Group : " + e.toString(), e);
                e.printStackTrace();
            }

        } else if (resultCode == EffectsFragment.EFFECT_PICKER_RESULT) { // Country Effect Returned
            Log.i(TAG, "Effect Result Returned for Light Group");

            Effect effect = new Effect();

            // Setup the effect
            effect.setKey(data.getExtras().getString(Constants.COLOR_TYPE));
            effect.setHue(data.getExtras().getIntArray(Constants.HUE));
            effect.setSaturation(data.getExtras().getIntArray(Constants.SATURATION));
            effect.setBrightness(data.getExtras().getIntArray(Constants.BRIGHTNESS));

            // Get the lightGroupId to update
            String lightGroupId = data.getExtras().getString(Constants.LIGHT_IDENTIFIER);


            effect.performAction(getContext(),lightGroupId,false);

            refreshTable();

        } else if (resultCode == NEW_GROUP_ACTIVITY) {
            Log.i(TAG, "Adding New Group Result Returned");

            Boolean success = data.getExtras().getBoolean(Constants.NEW_GROUP_ADDED);

            // If success was returned then a new Group was added and we need to refresh
            // THe group page
            if (success != null && success == true) {
                try {
                    Thread.sleep(200);
                    refreshTable();
                } catch (Exception e) {
                    Log.e(TAG, "Error repopulating view");
                    e.printStackTrace();
                }
            }

        } else if (resultCode == UPDATE_GROUP_ACTIVITY) {
            Log.i(TAG, "Updating Group Result Returned");

            Boolean success = data.getExtras().getBoolean(Constants.NEW_GROUP_ADDED);

            // If success was returned then a new Group was added and we need to refresh
            // THe group page
            if (success != null && success == true) {
                refreshTable();
            }

        }
    }

    private void singleHueColor (int hue, int saturation, int brightness, String lightGroupId) {
        Log.i(TAG, "singleHueColor");

        PHLightState newLightState = new PHLightState();
        newLightState.setHue(hue);
        newLightState.setSaturation(saturation);
        newLightState.setBrightness(brightness);

        Log.i(TAG, "Color Returned = " + newLightState.getHue() +
                "\nLightGroup Returned = " + lightGroupId);

        if (phHueSDK == null)
            phHueSDK = PHHueSDK.create();
        if (bridge == null)
            bridge = phHueSDK.getSelectedBridge();

        bridge.setLightStateForGroup(lightGroupId, newLightState);

        refreshTable();
    }

    private void countryEffect(int [] hue, int [] saturation, int [] brightness, String lightGroupId) {
        Log.i(TAG, "countryEffect");

        if (phHueSDK == null)
            phHueSDK = PHHueSDK.create();
        if (bridge == null)
            bridge = phHueSDK.getSelectedBridge();

        // Get list of all the lights that will need to be updated from within this group.
        List <String> lightList = bridge.getResourceCache().getGroups().get(lightGroupId).getLightIdentifiers();

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

            updateIndividualLight(light, newLightState);

            // Update count, if it is over the total for number of lights then we need to reset to 0 and restart
            count++;
            if(count >= totalColors) {count = 0;}
        }

        refreshTable();
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

    private void refreshTable() {
        Log.i(TAG, "refreshTable");

        try {
            tableLayout.removeAllViews();
            db = (DatabaseHelper.getInstance(context)).getWritableDatabase();

            // Have to sleep temporarily due to loading to fast
            Thread.sleep(100);

            populateAllGroups();

            db.close();
        } catch (Exception e) {

        }
    }
    private int getImageResourceByCurrentHue(int currentHue) {
        Log.d(TAG, "getImageResourceByCurrentHue : looping to find closest current hue setting");

        db = (DatabaseHelper.getInstance(context)).getWritableDatabase();
        String imageId =  ColorFactory.getClosestLightImageByHue(db, currentHue);
        db.close();

        return getResources().getIdentifier(imageId, "mipmap", getContext().getPackageName());
        //return imageId;
    }

    @Override
    public void onClick(View v) {
        // Launch New Group Activity for creating a new Group
        Intent intent = new Intent(this.getActivity().getApplicationContext(), NewGroupActivity.class);
        intent.putExtra(Constants.IS_UPDATE, false);
        startActivityForResult(intent, NEW_GROUP_ACTIVITY);
    }

    private void updateLightGroup(String identifier) {
        Log.d(TAG, "updateLightGroup");

        //LightGroup lightGroup = allGroups.get(position);
        Log.i(TAG, "Group Name = " + identifier);
        // Launch New Group Activity for creating a new Group
        Intent intent = new Intent(this.getActivity().getApplicationContext(), NewGroupActivity.class);
        intent.putExtra(Constants.IS_UPDATE, true);
        intent.putExtra(Constants.LIGHT_GROUP_ID, identifier);
        startActivityForResult(intent, UPDATE_GROUP_ACTIVITY);
    }

    private void showCreateGroupDialog() {
        Log.i(TAG, "showCreateGroupDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("No Groups Exist");
        alert.setMessage("There are currently no groups created. You can either create a new group now or create one at a later time. You will need to create groups at some point in order to use all features in this app");

        alert.setPositiveButton("Create Group", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "User is creating new group - Load New Group Activity");

                if(getActivity() != null) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), NewGroupActivity.class);
                    intent.putExtra(Constants.IS_UPDATE, false);
                    startActivityForResult(intent, NEW_GROUP_ACTIVITY);
                } else {
                    Intent intent = new Intent(getContext().getApplicationContext(), NewGroupActivity.class);
                    intent.putExtra(Constants.IS_UPDATE, false);
                    startActivityForResult(intent, NEW_GROUP_ACTIVITY);
                }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_all_groups, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.add_new_group) {
            Log.d(TAG, "Adding new Group");

            Intent intent = new Intent(this.getActivity().getApplicationContext(), NewGroupActivity.class);
            intent.putExtra(Constants.IS_UPDATE, false);
            startActivityForResult(intent, NEW_GROUP_ACTIVITY);
        } else if (id == R.id.edit_group){
            Log.d(TAG, "Edit Group");

            for(View row : allRows) {
                ImageView editImage = (ImageView)row.findViewById(R.id.btnEditGroup);
                Switch aSwitch = (Switch) row.findViewById(R.id.group_switch);

                if(editImage.getVisibility() == View.VISIBLE) {
                    editImage.setVisibility(View.GONE);
                    aSwitch.setTypeface(null, Typeface.NORMAL);
                }
                else {
                    editImage.setVisibility(View.VISIBLE);
                    aSwitch.setTypeface(null, Typeface.ITALIC);
                }
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void showFirstTimeHelpMessage() {
        openDatabase();

        String showHelpMessage = SettingsFactory.getSetting(db, Constants.SETTING_GROUP_HELP);
        if(showHelpMessage.equalsIgnoreCase("false") || showHelpMessage.equalsIgnoreCase("-1")) {
            showHelpDialog();

            SettingsFactory.updateOrInsert(db, Constants.SETTING_GROUP_HELP, "true");
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
                "\nCreate new groups - Click the \"+\" in the upper right corner" +
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

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        if(db != null)
            db.releaseReference();

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
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
        Log.w(TAG, "onDestroy");
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

package com.trevor.ultimatehue;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.trevor.ultimatehue.fragments.AllGroupsFragment;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewGroupActivity extends AppCompatActivity {
    private static final String tag = NewGroupActivity.class.toString();

    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    private EditText groupName;
    private ListView listView;
    private Button addGroup;
    private Button btnDeleteGroup;

    private String newGroupName = null;
    private List<String> lightIdentifiers = null;

    private List<String> allLightIdentifiers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        try {
            Log.i(tag, "OnCreate");

            String passedInLightIdentifier = "-1";
            boolean isUpdate = false;

            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                isUpdate = extras.getBoolean(Constants.IS_UPDATE);
                passedInLightIdentifier = extras.getString(Constants.LIGHT_GROUP_ID);
            }
            // Setup the ids from layout
            findViewsByIds();

            // Setup the page
            setupList(isUpdate, passedInLightIdentifier);
        } catch (Exception e) {
            Log.e(tag, "Error setting up NewGroupActivity " , e);
        }

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getApplication(), getClass().getSimpleName());
    }

    private void findViewsByIds() {
        groupName = (EditText) findViewById(R.id.lightGroupName);
        listView = (ListView) findViewById(R.id.checkedTextLights);
        addGroup = (Button) findViewById(R.id.btnAddingGroup);
        btnDeleteGroup = (Button) findViewById(R.id.btnDeleteGroup);
    }

    private void setupList(boolean isUpdate, String identifier) {
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        String [] lightList = new String [allLights.size()];
        allLightIdentifiers = new ArrayList<String>();

        Log.d(tag, "Looping through the lights");
        int count = 0;
        for (PHLight light : allLights) {
            lightList[count] = light.getName();
            allLightIdentifiers.add(light.getIdentifier());
            count++;
        }

        Log.d(tag, "Light List size = " + lightList.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.checked_list_item, lightList);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(false);
        // we want multiple clicks
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        if(isUpdate) {
            Log.d(tag, "Setting for update");

            PHGroup currentGroup = bridge.getResourceCache().getGroups().get(identifier);

            groupName.setText(currentGroup.getName());
            addGroup.setText("Update");

            // Loop through all the lights and determine if the item should be checked or not
            int position = 0;
            for(String tempIdentifier : allLightIdentifiers) {

                if(currentGroup.getLightIdentifiers().contains(tempIdentifier))
                    listView.setItemChecked(position, true);

                position++;
            }

            editGroupOnClick(currentGroup);
        } else {
            // Setup the Add Button
            Log.d(tag, "Setting for Adding new group");
            addGroupOnClick();
        }

    }

    private void addGroupOnClick() {
        addGroup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(tag, "Add Group Button Clicked");

                // Get the variables and make sure they are good
                if(captureUserInput()) {
                    createNewGroup();

                    // Return Result
                    Intent intent = new Intent(v.getContext(), AllGroupsFragment.class);
                    intent.putExtra(Constants.NEW_GROUP_ADDED, true);
                    setResult(AllGroupsFragment.NEW_GROUP_ACTIVITY, intent);

                    // Close the Color Picker View
                    finish();
                } else {
                    // TODO
                    Log.w(tag, "Error while getting user input, need to resolve");
                }
            }

        });
    }

    private void editGroupOnClick(final PHGroup group) {
        addGroup.setText("Update");
        btnDeleteGroup.setVisibility(View.VISIBLE);

        addGroup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(tag, "Update Group Button Clicked");

                // Get the variables and make sure they are good
                if (captureUserInput()) {
                    updateGroup(group);

                    // Return Result
                    Intent intent = new Intent(v.getContext(), AllGroupsFragment.class);
                    intent.putExtra(Constants.NEW_GROUP_ADDED, true);
                    setResult(AllGroupsFragment.UPDATE_GROUP_ACTIVITY, intent);

                    // Close the Color Picker View
                    finish();
                } else {
                    // TODO
                    Log.w(tag, "Error while getting user input, need to resolve");
                }
            }

        });

        btnDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "Delete Group clicked");

                deleteGroup(group);

                // Return Result
                Intent intent = new Intent(v.getContext(), AllGroupsFragment.class);
                intent.putExtra(Constants.NEW_GROUP_ADDED, true);
                setResult(AllGroupsFragment.UPDATE_GROUP_ACTIVITY, intent);

                // Close the Color Picker View
                finish();
            }
        });
    }
    private boolean captureUserInput() {
        try {
            Log.d(tag, "Capturing User Input");
            newGroupName = groupName.getText().toString().trim();

            SparseBooleanArray checked = listView.getCheckedItemPositions();

            lightIdentifiers = new ArrayList<>();

            for (int i = 0; i < allLightIdentifiers.size(); i++){
                if (checked.get(i)){
                    lightIdentifiers.add(allLightIdentifiers.get(i));
                }
            }

            return true;

        } catch (Exception e) {
            Log.e(tag, "Error getting User Input" , e);
            return false;
        }
    }

    private void deleteGroup(PHGroup group) {
        Log.i(tag, "deleteGroup");

        bridge.deleteGroup(group.getIdentifier(), new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {

            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {

            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> list) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });
    }

    private void createNewGroup() {
        Log.i(tag, "createNewGroup()");

        PHGroup phGroup = new PHGroup(newGroupName , newGroupName);
        phGroup.setLightIdentifiers(lightIdentifiers);
        bridge.createGroup(phGroup, new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {

            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {

            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> list) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });
    }

    private void updateGroup(PHGroup group) {
        Log.i(tag, "updateGroup()");

        group.setLightIdentifiers(lightIdentifiers);
        group.setName(newGroupName);

        bridge.updateGroup(group, new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {

            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {

            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> list) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });

    }
}

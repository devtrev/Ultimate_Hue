package com.trevor.ultimatehue.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.NewGroupActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.helpers.ResizableButton;
import com.trevor.ultimatehue.lights.LightGroup;
import com.trevor.ultimatehue.music.SoundPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.android.gms.internal.zzid.runOnUiThread;

public class MemoryFragment extends Fragment {

    private static final String TAG = MemoryFragment.class.toString();
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final int NEW_GROUP_ACTIVITY = 151;

    private View rootview;
    private Button btnMemoryStart;
    private ResizableButton btnMemoryOne;
    private ResizableButton btnMemoryTwo;
    private ResizableButton btnMemoryThree;
    private ResizableButton btnMemoryFour;
    private Spinner spnMemoryLightGroups;
    private ProgressBar progressBarMemoryProgress;
    private TextView txtMemoryProgress;

    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private SQLiteDatabase db;
    private List<LightGroup> allGroups;
    private static int MAX = 4;
    private int[] colorOrder;
    private int count;
    private int totalToRemeber;
    private boolean isPlaying;
    private Map<Integer, Integer> colorMap;

    public MemoryFragment() {
        // Required empty public constructor
    }

    public static MemoryFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        MemoryFragment fragment = new MemoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_memory, container, false);
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();
        allGroups = new ArrayList<>();
        colorMap = new HashMap<>();

        totalToRemeber = 2;
        isPlaying = false;
        count = 0;

        findViewsById();
        setupListeners();
        populateSpinner();
        createLightMap();

        /* -- Removed Version 1.2.2 as this is no longer BETA
        openDatabase();
        String showBetaMessage = SettingsFactory.getSetting(db, Constants.SETTING_MEMORY_BETA);
        if(showBetaMessage.equalsIgnoreCase("false") || showBetaMessage.equalsIgnoreCase("-1")) {
            showBetaDialog();

            SettingsFactory.updateOrInsert(db, Constants.SETTING_MEMORY_BETA, "true");
        } else {
            showStartGameDialog();
        }

        closeDatabase();
        */

        showStartGameDialog();



        setHasOptionsMenu(true);

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        return rootview;
    }

    private void findViewsById() {
        Log.i(TAG, "findViewsById");

        btnMemoryStart = (Button) rootview.findViewById(R.id.btnMemoryStart);
        btnMemoryOne = (ResizableButton) rootview.findViewById(R.id.btnMemoryOne);
        btnMemoryTwo = (ResizableButton) rootview.findViewById(R.id.btnMemoryTwo);
        btnMemoryThree = (ResizableButton) rootview.findViewById(R.id.btnMemoryThree);
        btnMemoryFour = (ResizableButton) rootview.findViewById(R.id.btnMemoryFour);
        spnMemoryLightGroups = (Spinner) rootview.findViewById(R.id.spnMemoryLightGroups);
        progressBarMemoryProgress = (ProgressBar) rootview.findViewById(R.id.progressBarMemoryProgress);
        txtMemoryProgress = (TextView) rootview.findViewById(R.id.txtMemoryProgress);
    }

    private void setupListeners() {
        Log.i(TAG, "setupListeners");

        btnMemoryStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartGameDialog();
            }
        });

        btnMemoryOne.setOnClickListener(buttonClickedListener);
        btnMemoryTwo.setOnClickListener(buttonClickedListener);
        btnMemoryThree.setOnClickListener(buttonClickedListener);
        btnMemoryFour.setOnClickListener(buttonClickedListener);
    }

    private void populateSpinner() {
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

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, groupStringArray);
            spnMemoryLightGroups.setAdapter(adapter);
            spnMemoryLightGroups.setSelection(lastGroup);

            closeDatabase();
        } else {
            Log.w(TAG, "No groups exist - ask user to create one");
            showCreateGroupDialog();
        }
    }

    private void createLightMap() {
        Log.i(TAG, "createLightMap");

        // Red
        colorMap.put(0, 0);

        // Blue
        colorMap.put(1, 47000);

        // Green
        colorMap.put(2, 23420);

        // Purple
        colorMap.put(3, 49500);

        // Normal
        colorMap.put(4, 14092);
    }

    private void colorPicked(View v) {
        Log.i(TAG, "colorPicked");

        int buttonTag = Integer.valueOf((String) v.getTag());

        Log.i(TAG, "button tag - " + buttonTag);

        if (isPlaying) {
            if (buttonTag == colorOrder[count]) {
                count++;

                // Update the progressBar to show progress was made
                progressBarMemoryProgress.setProgress(count);
                txtMemoryProgress.setText(count + " of " + totalToRemeber);

                if (count >= colorOrder.length) {
                    //Toast.makeText(getContext(), "Congrats - You win", Toast.LENGTH_LONG).show();
                    isPlaying = false;
                    totalToRemeber++;
                    btnMemoryStart.setText("Start Next round");

                    showNextRoundDialog();
                } else {
                    SoundPlayer.playSound(getContext(), R.raw.success);
                    //Toast.makeText(getContext(), "You right", Toast.LENGTH_SHORT).show();
                }

            } else {
                SoundPlayer.playSound(getContext(), R.raw.fail);
                btnMemoryStart.setText("Start new");
                count = 0;
                isPlaying = false;

                showGameOverDialog();

                totalToRemeber = 2;
            }
        } else {
            Log.w(TAG, "No Game currently being played, button locked");
            //Toast.makeText(getContext(), "Game not started yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void playLights() {
        Log.i(TAG, "playLights");

        // Prevent user from clicking while lights happen
        isPlaying = false;

        int groupPosition = spnMemoryLightGroups.getSelectedItemPosition();
        String groupIdentifier = allGroups.get(groupPosition).getPhGroup().getIdentifier();

        openDatabase();
        SettingsFactory.updateSetting(db, Constants.SETTING_LAST_GROUP, String.valueOf(groupPosition));
        closeDatabase();

        try {
            // Loop through the updating of all the lights
            for (int i = 0; i < colorOrder.length; i++) {

                PHLightState state = new PHLightState();
                state.setHue(colorMap.get(colorOrder[i]));
                state.setBrightness(254);
                state.setSaturation(254);
                state.setTransitionTime(0);

                bridge.setLightStateForGroup(
                        groupIdentifier,
                        state);

                Thread.sleep(1000);

                // Check to see if this was the last of the actual color updates,
                // if so we don't sleep as long when we reset the lights to indicte its done
                // Not 100% sure why i did this, but leaving it in -- version 1.2.1
                if(i < (colorOrder.length - 1)) {
                    state.setBrightness(1);
                    state.setTransitionTime(4);
                    bridge.setLightStateForGroup(
                            groupIdentifier,
                            state);

                    Thread.sleep(500);
                } else {
                    Log.d(TAG, "Last iteration, just sleep for 500 ms and don't update the lights");
                    Thread.sleep(500);
                }
            }

            // Update to default signaling done
            PHLightState state = new PHLightState();
            state.setHue(colorMap.get(4));
            state.setBrightness(180);
            state.setSaturation(200);
            state.setTransitionTime(4);
            bridge.setLightStateForGroup(
                    groupIdentifier,
                    state);

        } catch (Exception e) {
            Log.e(TAG, "Sleep Error " + e.toString());
            e.printStackTrace();
        }

        //lightsUpdatingDialog.dismiss();
        isPlaying = true;
    }

    private void start() {
        Log.i(TAG, "start");

        if(allGroups.size() > 0) {
            randomize();
            count = 0;

            // Set progressBar max to be number of lights that we are remembering
            progressBarMemoryProgress.setMax(totalToRemeber);
            progressBarMemoryProgress.setProgress(0); // Reset progress to 0
            txtMemoryProgress.setText("0 of " + totalToRemeber);

            BackgroundTask task = new BackgroundTask((MainActivity)getActivity());
            task.execute();

            AnalyticsHelper application = (AnalyticsHelper) getActivity().getApplication();
            String category = "Memory";
            String action = "Start Button ";
            String label = "Number of lights to remmeber = " + totalToRemeber;

            AnalyticsHelper.analyticsEvent(application, category, action, label);
        } else {
            Log.w(TAG, "No groups exist - ask user to create one");
            showCreateGroupDialog();
        }
    }

    private void randomize() {
        Random rand = new Random();

        colorOrder = new int[totalToRemeber];
        for (int i = 0; i < totalToRemeber; i++) {
            colorOrder[i] = rand.nextInt(MAX);

            Log.d(TAG, "" + i + "=" + colorOrder[i]);
        }
    }

    private View.OnClickListener buttonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            colorPicked(v);
        }
    };

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

    private void showHelpDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Memory Help");
        alert.setMessage("Test your memory skills. Simply start a game and see how many lights you can remember. " +
                "Each level you pass will increase the lights count that you need to remember." +
                "\n\nHow To Play" +
                "\n* Click Start and watch the lights update" +
                "\n* Once the lights finish updating, Click the Color in the order the lights updated" +
                "\n* Repeat until there are too many lights to remember");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
            }
        });

        alert.show();
    }

    private void showBetaDialog() {
        Log.i(TAG, "showBetaDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Memory - Beta");
        alert.setMessage("Please note that this is still in Beta. I have been unable to work out everything to look as clean as we would like it to be" +
                "\nThat said it should work for most people and don't see any reason why it still shouldn't be enjoyable. Feel free to report any issues you may have with it and I will continue to work to make it a better experience");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showStartGameDialog();
                // Do something with value!
            }
        });

        alert.show();
    }

    private void showStartGameDialog() {
        Log.i(TAG, "showStartGameDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Memory");
        alert.setMessage("Click start and watch carefully as the lights update. Then try to select the colors on your device in the order they updated.");

        alert.setPositiveButton("Start Game", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                start();
                // Do something with value!
            }
        });

        alert.show();
    }

    private void showNextRoundDialog() {
        Log.i(TAG, "showNextRoundDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Next Round");
        alert.setMessage("You won! Ready for the next round? Next round will have " + totalToRemeber + " lights to remember ");

        alert.setPositiveButton("Start Next Round", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                start();
                // Do something with value!
            }
        });

        alert.show();
    }

    private void showGameOverDialog() {
        Log.i(TAG, "showGameOverDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Game Over");
        alert.setMessage("You lost :( You were able to guess " + (totalToRemeber - 1) + " lights before losing.");

        alert.setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                start();
                // Do something with value!
            }
        });
        alert.setNegativeButton("I'm Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do something with value!
            }
        });

        alert.show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEW_GROUP_ACTIVITY) {
            Log.i(TAG, "New Group created, need to refresh view");

            try {
                Thread.sleep(200);
                populateSpinner();
            } catch (Exception e) {
                Log.e(TAG, "Error repopulating view");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_memory, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.memoryHelp) {
            Log.d(TAG, "Memory Help");

            showHelpDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((MainActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

        if(bridge == null) {
            bridge = phHueSDK.getSelectedBridge();
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

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public BackgroundTask(MainActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            //dialog.setContentView(R.layout.memory_light_update_dialog);
            dialog.setTitle("Lights updating");
            dialog.setMessage("Lights are currently updating. Once finished try to select the colors in the order they updated.");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Give user 1 second to look up at lights before updating :)
                Thread.sleep(1000);

                isPlaying = false;
                playLights();

                while(isPlaying == false)
                    Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}

package com.trevor.ultimatehue;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.trevor.ultimatehue.factory.TriggerFactory;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.triggers.Trigger;

import java.util.Arrays;
import java.util.List;

public class TriggerActivity extends AppCompatActivity {

    public static final String TAG = TriggerActivity.class.toString();
    public static final int COLOR_PICKER_ACTIVITY = 99;
    public static final int EFFECT_PICKER_ACTIVITY = 50;
    public static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 1;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 2;

    private TextView txtTrigger;
    private Spinner spnAction;
    private TextView txtTriggerLightList;
    private TextView txtColorChooser;
    private ListView lightListView;
    private Button btnChooseColor;
    private Button btnSubmit;
    private CheckBox chkPlayWithSound;

    private String passedInTriggerIdentifier;


    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private SQLiteDatabase db;
    private Trigger trigger;

    private String [] allLightIdentifiers;
    private String [] allLightNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            passedInTriggerIdentifier = extras.getString(Constants.TRIGGER_ID);
        }

        // Set the title to the Trigger Type
        setTitle(passedInTriggerIdentifier);

        if(!checkPermissions()) {
            errorMsg("This feature will not work if proper permission was not granted");
        }

            // Create Hue bridge resource
            phHueSDK = PHHueSDK.create();
            bridge = phHueSDK.getSelectedBridge();

            // Setup the resources on this page
            findViewsByIds();

            // Populate the resources that will be displayed in the view
            populateView();


    }

    private void populateView() {
        Log.d(TAG, "populateView");

        // Init the listeners to be used
        initListeners();

        // Set the light list to have all currently created groups
        populateLightList();

        // Get the current saved configuration of the trigger
        openDatabase();
        trigger = TriggerFactory.getTriggerByIdentifier(db, passedInTriggerIdentifier);
        closeDatabase();

        // Setup The text for the IF ... THEN with appropriate trigger name
        txtTrigger.setText("IF " + passedInTriggerIdentifier + " THEN ");

        // Loop through the light list and try to find the saved one. If nothing is found nothing gets checked
        int counter = 0;
        for(String identifier : allLightIdentifiers) {
            if(trigger.getLightGroupIdentifier() != null && trigger.getLightGroupIdentifier().equals(identifier)) {
                Log.d(TAG, "FOUND POSITION " + counter);
                lightListView.setItemChecked(counter, true);
                break;
            }

            counter++;
        }

        // Set the color saved by user
        if(trigger.getColor() != null && trigger.getColor().trim().length() > 0) {
            btnChooseColor.setText(trigger.getColor());
        }

        // Set the Action to be taken
        if (trigger.isEnabled() == Constants.ENABLED) {
            List<String> list = Arrays.asList(getResources().getStringArray(R.array.trigger_action_arrays));
            spnAction.setSelection(list.indexOf(trigger.getAction()));
        } else {
            spnAction.setSelection(0);
        }
    }

    private void findViewsByIds() {
        Log.d(TAG, "findViewsByIds");

        txtTrigger = (TextView) findViewById(R.id.txtTrigger);
        txtColorChooser = (TextView) findViewById(R.id.txtColorChooser);
        spnAction = (Spinner) findViewById(R.id.spinner1);
        txtTriggerLightList = (TextView) findViewById(R.id.txtTriggerLightList);
        lightListView = (ListView) findViewById(R.id.checkedTextLights);
        btnChooseColor = (Button)findViewById(R.id.btnChooseColor);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        chkPlayWithSound = (CheckBox) findViewById(R.id.chkPlayWithSound);
    }

    // get the selected dropdown list value
    public void initListeners() {
        Log.d(TAG, "initListeners");

        spnAction.setOnItemSelectedListener(new CustomOnItemSelectedListener());

    }

    private boolean checkPermissions() {

        boolean allPermissionsGood = true;

        // Only check permissions if this is SMS received, make sure that permissions are granted for SMS
        if(passedInTriggerIdentifier.equals(TriggerFactory.TRIGGER_SMS_RECEIVED)) {

            // Check to see if Permissions exist for reading external storage
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                allPermissionsGood = false;

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECEIVE_SMS)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECEIVE_SMS},
                            MY_PERMISSIONS_REQUEST_RECEIVE_SMS);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

            // Return individually since this is just for SMS we don't care if WIFI access was off
            return allPermissionsGood;
        }

        // Only check permissions if this is SMS received, make sure that permissions are granted for SMS
        if(passedInTriggerIdentifier.equals(TriggerFactory.TRIGGER_WIFI_CONNECTED)) {

            // Check to see if Permissions exist for reading external storage
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED) {

                allPermissionsGood = false;

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_WIFI_STATE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                            MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

            // Return individually since this is just for SMS we don't care if WIFI access was off
            return allPermissionsGood;
        }

        return allPermissionsGood;
    }

    private boolean validateInput() {
        Log.d(TAG, "validateInput");

        if(!(String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_nothing)))) {
            if(lightListView.getCheckedItemCount() == 0) {
                Log.w(TAG, "No lights checked");

                errorMsg("Please select a light group to be updated - If nothing is showing in the list make sure you have a group created");
                lightListView.setBackgroundColor(Color.RED);

                return false;
            }
        }

        if (btnChooseColor.getVisibility() == View.VISIBLE) {

            if(btnChooseColor.getText().equals(getString(R.string.choose_color)) ||
                    btnChooseColor.getText().equals(getString(R.string.choose_effect))) {
                Log.w(TAG, "No Effect / Color picked");

                errorMsg("Please select a valid Color/Effect");
                btnChooseColor.setBackgroundColor(Color.RED);

                return false;
            }
        }

        return true;
    }

    public void submit(View v) {
        Log.d(TAG, "submit");

        if(validateInput()) {

            // Loops through and figures out what group was selected
            SparseBooleanArray checked = lightListView.getCheckedItemPositions();
            String groupIdentifier = "";
            String groupName = "";

            for (int i = 0; i < lightListView.getAdapter().getCount(); i++) {
                if (checked.get(i)) {
                    groupIdentifier = allLightIdentifiers[i];
                    groupName = allLightNames[i];
                    break;
                }
            }

            if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_lights_off))) {
                Log.d(TAG, "Lights Off when " + passedInTriggerIdentifier);

                openDatabase();

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, passedInTriggerIdentifier);
                trigger.setIsEnabled(Constants.ENABLED);
                trigger.setAction(spnAction.getSelectedItem().toString());
                trigger.setLightGroupIdentifier(groupIdentifier);
                trigger.setLightGroupName(groupName);

                TriggerFactory.update(db, trigger);

                closeDatabase();

            } else if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_lights_on))) {
                Log.d(TAG, "Lights On when " + passedInTriggerIdentifier);

                openDatabase();

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, passedInTriggerIdentifier);
                trigger.setIsEnabled(Constants.ENABLED);
                trigger.setAction(spnAction.getSelectedItem().toString());
                trigger.setColor(btnChooseColor.getText().toString().trim());
                trigger.setLightGroupIdentifier(groupIdentifier);
                trigger.setLightGroupName(groupName);

                TriggerFactory.update(db, trigger);

                closeDatabase();
            } else if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_flash_lights))) {
                Log.d(TAG, "Lights Flash on when " + passedInTriggerIdentifier);

                openDatabase();

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, passedInTriggerIdentifier);
                trigger.setIsEnabled(Constants.ENABLED);
                trigger.setAction(spnAction.getSelectedItem().toString());
                trigger.setColor(btnChooseColor.getText().toString().trim());
                trigger.setLightGroupIdentifier(groupIdentifier);
                trigger.setLightGroupName(groupName);

                TriggerFactory.update(db, trigger);

                closeDatabase();
            } else if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_effect))) {
                Log.d(TAG, "Lights Play Effect on when " + passedInTriggerIdentifier);

                openDatabase();

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, passedInTriggerIdentifier);
                trigger.setIsEnabled(Constants.ENABLED);
                trigger.setAction(spnAction.getSelectedItem().toString());
                trigger.setColor(btnChooseColor.getText().toString().trim());
                trigger.setLightGroupIdentifier(groupIdentifier);
                trigger.setLightGroupName(groupName);

                if (chkPlayWithSound.isChecked())
                    trigger.setHigh(1);
                else
                    trigger.setHigh(0);


                TriggerFactory.update(db, trigger);

                closeDatabase();
            } else if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_nothing))) {
                Log.d(TAG, "Do nothing selected - disabling the trigger");

                openDatabase();

                Trigger trigger = TriggerFactory.getTriggerByIdentifier(db, passedInTriggerIdentifier);
                trigger.setIsEnabled(Constants.DISABLED);

                TriggerFactory.update(db, trigger);

                closeDatabase();
            }

            // This will close the activity
            finish();
        }
    }

    private void populateLightList() {
        Log.d(TAG, "populateLightList");

        List<PHGroup> allLights = bridge.getResourceCache().getAllGroups();
        String [] lightList = new String [allLights.size()];
        allLightIdentifiers = new String [allLights.size()];
        allLightNames = new String [allLights.size()];

        Log.d(TAG, "Looping through the light groups : " + allLights.size());
        int count = 0;
        for (PHGroup light : allLights) {
            Log.d(TAG, "Group Name : " + light.getName());

            // Sets up list of names to be used in the list Adapter
            lightList[count] = light.getName();

            // Sets up the Identifier that we will use to update the light as well as the name which we will use later
            // These get loaded into Database for use later with the trigger
            allLightIdentifiers[count] = light.getIdentifier();
            allLightNames[count] = light.getName();

            // Update the count
            count++;
        }

        Log.d(TAG, "Light List size = " + lightList.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.checked_list_item, lightList);
        lightListView.setAdapter(adapter);
        lightListView.setItemsCanFocus(false);
        // we want single clicks
        lightListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    public void chooseColor(View view) {
        Log.i(TAG, "chooseColor");

        if(String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_effect))) {
            Intent intent = new Intent(this, EffectPickerActivity.class);
            startActivityForResult(intent, EFFECT_PICKER_ACTIVITY);
        } else {
            //Intent intent = new Intent(this, ColorPickerActivityGroup.class);
            Intent intent = new Intent(this, ColorPickerActivityIndividual.class);
            startActivityForResult(intent, COLOR_PICKER_ACTIVITY);
        }
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
            db = (DatabaseHelper.getInstance(this)).getWritableDatabase();
        }
    }

    class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_nothing))) {
                // Do Nothing selected - Make all options invisible as you are doing nothing

                lightListView.setVisibility(View.INVISIBLE);
                btnChooseColor.setVisibility(View.INVISIBLE);
                txtTriggerLightList.setVisibility(View.INVISIBLE);
                txtColorChooser.setVisibility(View.INVISIBLE);
                chkPlayWithSound.setVisibility(View.INVISIBLE);
            } else if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_lights_off))) {
                // Lights Off selected, this means that choosing color is not needed

                btnChooseColor.setVisibility(View.INVISIBLE);
                txtColorChooser.setVisibility(View.INVISIBLE);
                lightListView.setVisibility(View.VISIBLE);
                txtTriggerLightList.setVisibility(View.VISIBLE);
                chkPlayWithSound.setVisibility(View.INVISIBLE);
            } else if (String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_lights_on))) {
                // Turn Lights On - Make all options visible as user will need to select something

                lightListView.setVisibility(View.VISIBLE);
                btnChooseColor.setVisibility(View.VISIBLE);
                btnChooseColor.setText(getResources().getString(R.string.choose_color));
                txtTriggerLightList.setVisibility(View.VISIBLE);
                txtColorChooser.setVisibility(View.VISIBLE);
                chkPlayWithSound.setVisibility(View.INVISIBLE);
            } else if(String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_flash_lights))) {
                // Turn Lights On - Make all options visible as user will need to select something

                lightListView.setVisibility(View.VISIBLE);
                btnChooseColor.setVisibility(View.VISIBLE);
                btnChooseColor.setText(getResources().getString(R.string.choose_color));
                txtTriggerLightList.setVisibility(View.VISIBLE);
                txtColorChooser.setVisibility(View.VISIBLE);
                chkPlayWithSound.setVisibility(View.INVISIBLE);
            } else if(String.valueOf(spnAction.getSelectedItem()).equals(getString(R.string.trigger_effect))) {
                // Turn Lights On - Make all options visible as user will need to select something

                lightListView.setVisibility(View.VISIBLE);
                btnChooseColor.setVisibility(View.VISIBLE);
                btnChooseColor.setText(getResources().getString(R.string.choose_effect));
                txtTriggerLightList.setVisibility(View.VISIBLE);
                txtColorChooser.setVisibility(View.VISIBLE);
                chkPlayWithSound.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // Auto-generated method stub
        }

    }

    /**
     * Get the Color Result and process
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == COLOR_PICKER_ACTIVITY) {
            Log.i(TAG, "Color Result Returned");

            try {
                String name = data.getExtras().getString(Constants.NAME);
                Log.i(TAG, "Color Returned = " + name);

                btnChooseColor.setText(name);
            } catch (Exception e) {
                Log.e(TAG, "Error while process activity result : " + e.toString(), e);
                e.printStackTrace();
            }
        } else if(resultCode == EFFECT_PICKER_ACTIVITY) {
            Log.i(TAG, "Color Result Returned");

            try {
                String name = data.getExtras().getString(Constants.EFFECT_NAME);
                Log.i(TAG, "Color Returned = " + name);

                btnChooseColor.setText(name);
            } catch (Exception e) {
                Log.e(TAG, "Error while process activity result : " + e.toString(), e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trigger_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_more_info){
            Log.d(TAG, "More Info");

            showHelpDialog(trigger.getHelpText());
        }

        return super.onOptionsItemSelected(item);
    }

    private void showHelpDialog(String helpText) {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("More Info");
        alert.setMessage(helpText);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    private void errorMsg(String errMsg) {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Input Error");
        alert.setMessage(errMsg);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }
}

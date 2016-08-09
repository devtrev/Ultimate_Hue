package com.trevor.ultimatehue;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;
import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.fragments.AlarmFragment;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.philips.lighting.model.PHSchedule.RecurringDay.RECURRING_ALL_DAY;
import static com.philips.lighting.model.PHSchedule.RecurringDay.RECURRING_WEEKDAYS;
import static com.philips.lighting.model.PHSchedule.RecurringDay.RECURRING_WEEKEND;

public class AlarmActivity extends AppCompatActivity {

    private static final String TAG = AlarmActivity.class.toString();

    public static final int COLOR_PICKER_ACTIVITY = 99;

    private Button btnChooseColor;
    private Button btnCreateAlarm;
    private EditText txtAlarmActivityName;
    private RadioButton radioEveryday;
    private RadioButton radioWeekends;
    private RadioButton radioWeekdays;
    private ListView lightListView;
    private Spinner spnAlarmHour;
    private Spinner spnAlarmMinute;
    private Spinner spnAlarmAMPM;
    private ToggleButton toggleAlarmOnOff;
    private Spinner spnAlarmFade;

    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private SQLiteDatabase db;
    private PHSchedule updateSchedule;
    private boolean isUpdate;

    private String [] allLightIdentifiers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        String passedInAlarmIdentifier = "-1";
        isUpdate = false;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            isUpdate = extras.getBoolean(Constants.IS_UPDATE);

            if(isUpdate)
                passedInAlarmIdentifier = extras.getString(Constants.ALARM_ID);
        }

        Log.d(TAG, "isUpdate = " + isUpdate);
        Log.d(TAG, "passedInAlarmIdentifier = " + passedInAlarmIdentifier);

        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        if(isUpdate)
            updateSchedule = bridge.getResourceCache().getSchedules().get(passedInAlarmIdentifier);

        findViewsById();
        populateView();
    }

    private void findViewsById() {
        Log.d(TAG, "findViewsById");

        btnChooseColor = (Button)findViewById(R.id.btnChooseColor);
        txtAlarmActivityName = (EditText)findViewById(R.id.txtAlarmActivityName);
        radioEveryday = (RadioButton)findViewById(R.id.radioEveryday);
        radioWeekdays = (RadioButton)findViewById(R.id.radioWeekdays);
        radioWeekends = (RadioButton)findViewById(R.id.radioWeekends);
        lightListView = (ListView) findViewById(R.id.checkedTextLights);
        spnAlarmHour = (Spinner) findViewById(R.id.spnAlarmHour);
        spnAlarmMinute = (Spinner) findViewById(R.id.spnAlarmMinute);
        spnAlarmAMPM = (Spinner) findViewById(R.id.spnAlarmAMPM);
        btnCreateAlarm = (Button) findViewById(R.id.btnCreateAlarm);
        toggleAlarmOnOff = (ToggleButton) findViewById(R.id.toggleAlarmOnOff);
        spnAlarmFade = (Spinner) findViewById(R.id.spnAlarmFade);
    }

    private void populateView() {
        Log.d(TAG, "populateView");

        populateLightList();
        setupSpinners();

        toggleAlarmOnOff.setChecked(true);

        if(isUpdate) {
            Log.i(TAG, "Loading Alarm for updating");

            btnCreateAlarm.setText("Update Alarm");
            txtAlarmActivityName.setText(updateSchedule.getName());

            if(android.text.format.DateFormat.is24HourFormat(this)) {
                SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
                String alarmTime = sdf.format(updateSchedule.getDate());

                int selected = Integer.parseInt(alarmTime.substring(0,2));
                spnAlarmHour.setSelection(selected);

                selected = Integer.parseInt(alarmTime.substring(3,5));
                spnAlarmMinute.setSelection(selected - 1);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                String alarmTime = sdf.format(updateSchedule.getDate());

                int selected = Integer.parseInt(alarmTime.substring(0,2));
                spnAlarmHour.setSelection(selected - 1);

                selected = Integer.parseInt(alarmTime.substring(3,5));
                spnAlarmMinute.setSelection(selected);

                String ampm = alarmTime.substring(6);
                if(ampm.equalsIgnoreCase(getString(R.string.PM)))
                    spnAlarmAMPM.setSelection(1);
                else
                    spnAlarmAMPM.setSelection(0);
            }

            if(updateSchedule.getRecurringDays() == RECURRING_ALL_DAY.getValue()) {
                radioEveryday.setChecked(true);
            } else if (updateSchedule.getRecurringDays() == RECURRING_WEEKDAYS.getValue()) {
                radioWeekdays.setChecked(true);
            } else if (updateSchedule.getRecurringDays() == RECURRING_WEEKEND.getValue()) {
                radioWeekends.setChecked(true);
            }

            int counter = 0;
            for(String identifier : allLightIdentifiers) {
                if(updateSchedule.getGroupIdentifier().equals(identifier)) {
                    Log.d(TAG, "FOUND POSITION " + counter);
                    lightListView.setItemChecked(counter, true);
                    break;
                }

                counter++;
            }

            String colorName = null;
            if(updateSchedule.getLightState() != null) {
                if(updateSchedule.getLightState().isOn()) {
                    openDatabase();
                    colorName = ColorFactory.getColorNameByHue(db, updateSchedule.getLightState().getHue());
                    closeDatabase();
                }

                //Log.d(TAG, "Transition Time : " + updateSchedule.getLightState().getTransitionTime());
            } else {
                colorName = getString(R.string.alarm_scene);
            }

            if(colorName != null) {
                btnChooseColor.setText(colorName);
                toggleAlarmOnOff.setChecked(true);
            }
            else {
                btnChooseColor.setVisibility(View.INVISIBLE);
                toggleAlarmOnOff.setChecked(false);
            }


        }
    }

    private void populateLightList() {
        Log.d(TAG, "populateLightList");

        List<PHGroup> allLights = bridge.getResourceCache().getAllGroups();
        String [] lightList = new String [allLights.size()];
        allLightIdentifiers = new String [allLights.size()];

        Log.d(TAG, "Looping through the lights");
        int count = 0;
        for (PHGroup light : allLights) {
            lightList[count] = light.getName();
            allLightIdentifiers[count] = light.getIdentifier();
            count++;
        }

        Log.d(TAG, "Light List size = " + lightList.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.checked_list_item, lightList);
        lightListView.setAdapter(adapter);
        lightListView.setItemsCanFocus(false);
        // we want multiple clicks
        lightListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


    }

    private void setupSpinners(){
        Log.d(TAG, "setupSpinners");

        // Create the hour Drop down
        ArrayAdapter<String> hourAdapter = null;

        if(android.text.format.DateFormat.is24HourFormat(this)) {
            hourAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.alarm_hours_24));

            spnAlarmAMPM.setVisibility(View.INVISIBLE);
        } else {
            hourAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.alarm_hours_12));

            // Create AM PM adapter
            ArrayAdapter<String> ampmAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.alarm_ampm));
            ampmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnAlarmAMPM.setAdapter(ampmAdapter);
        }

        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAlarmHour.setAdapter(hourAdapter);

        // Create the Minute drop down
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.alarm_minutes));
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAlarmMinute.setAdapter(minuteAdapter);

        ArrayAdapter<String> fadeAdapter= new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.alarm_fade));
        fadeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAlarmFade.setAdapter(fadeAdapter);
    }

    public void chooseColor(View view) {
        Log.i(TAG, "chooseColor");

        Intent intent = new Intent(this, ColorPickerActivityIndividual.class);
        startActivityForResult(intent, COLOR_PICKER_ACTIVITY);
    }

    public void alarmOnOff(View view) {
        Log.i(TAG, "alarmOnOff");

        // Only show the button to choose color if lights are going on
        if(toggleAlarmOnOff.isChecked())
            btnChooseColor.setVisibility(View.VISIBLE);
        else
            btnChooseColor.setVisibility(View.INVISIBLE);
    }

    public void createAlarm(View view) {
        Log.i(TAG, "createAlarm");

        if(createAlarmCheck()) {
            Log.d(TAG, "Alarm passed checks, creating new alarm");

            PHSchedule schedule = new PHSchedule(txtAlarmActivityName.getText().toString().trim());

            if(!btnChooseColor.getText().equals(getString(R.string.alarm_scene))) {
                // If this is a custom scene don't do anything and hope that the lack of update to the Scene will keep it the same...
                openDatabase();
                HueColor hueColor = ColorFactory.getColorByName(db, btnChooseColor.getText().toString());
                closeDatabase();

                // Set the light state
                PHLightState lightState = new PHLightState();

                if(toggleAlarmOnOff.isChecked()) {
                    lightState.setOn(true);
                    lightState.setHue(hueColor.getHue());
                    lightState.setBrightness(hueColor.getBrightness());
                    lightState.setSaturation(hueColor.getSaturation());
                    lightState.setTransitionTime(Integer.parseInt(spnAlarmFade.getSelectedItem().toString()) * 600);
                } else {
                    lightState.setOn(false);
                    lightState.setTransitionTime(Integer.parseInt(spnAlarmFade.getSelectedItem().toString()) * 600);
                }

                schedule.setLightState(lightState);
            }

            int minute = Integer.parseInt(spnAlarmMinute.getSelectedItem().toString());
            int hour = Integer.parseInt(spnAlarmHour.getSelectedItem().toString());

            // if not 24 hour format then make it so
            if(!android.text.format.DateFormat.is24HourFormat(this)) {
                String ampm = spnAlarmAMPM.getSelectedItem().toString();

                // if PM was selected add 12 hours
                if (ampm.equals(getString(R.string.PM))) {
                    hour += 12;
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.HOUR_OF_DAY, hour);

            if(radioEveryday.isChecked())
                schedule.setRecurringDays(PHSchedule.RecurringDay.RECURRING_ALL_DAY.getValue());
            else if(radioWeekdays.isChecked())
                schedule.setRecurringDays(PHSchedule.RecurringDay.RECURRING_WEEKDAYS.getValue());
            else if(radioWeekends.isChecked())
                schedule.setRecurringDays(PHSchedule.RecurringDay.RECURRING_WEEKEND.getValue());

            SparseBooleanArray checked = lightListView.getCheckedItemPositions();

            for (int i = 0; i < lightListView.getAdapter().getCount(); i++) {
                if (checked.get(i)) {
                    schedule.setGroupIdentifier(allLightIdentifiers[i]);
                }
            }

            schedule.setLocalTime(true);
            schedule.setDate(cal.getTime());

            if(isUpdate) {
                Log.i(TAG, "Updating Alarm");

                // Set the alarm to update
                schedule.setIdentifier(updateSchedule.getIdentifier());

                bridge.updateSchedule(schedule, new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule phSchedule) {
                        Log.d(TAG, "onCreated");
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess");
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.d(TAG, "onError : " + s);
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
                        Log.d(TAG, "onStateUpdate");
                    }
                });
            } else {
                Log.i(TAG, "Creating Alarm");
                bridge.createSchedule(schedule, new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule phSchedule) {
                        Log.d(TAG, "onCreated");
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess");
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.d(TAG, "onError : " + s);
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
                        Log.d(TAG, "onStateUpdate");
                    }
                });
            }

            setResult(AlarmFragment.NEW_ALARM_ACTIVITY);
            finish();
        } else
            Log.w(TAG, "Failed Alarm checks");
    }

    private boolean createAlarmCheck() {
        Log.i(TAG, "createAlarmCheck");

        boolean isGood = true;

        if(txtAlarmActivityName.getText().toString().trim().length() == 0) {
            txtAlarmActivityName.setBackgroundColor(Color.YELLOW);
            isGood = false;
        } else
            txtAlarmActivityName.setBackgroundColor(Color.WHITE);

        if (toggleAlarmOnOff.isChecked() && btnChooseColor.getText().toString().equals(getString(R.string.alarm_color))) {
            btnChooseColor.setBackgroundColor(Color.YELLOW);
            isGood = false;
        } else
            btnChooseColor.setBackgroundColor(Color.LTGRAY);

        if(!radioEveryday.isChecked() && !radioWeekdays.isChecked() && !radioWeekends.isChecked()) {
            isGood = false;
        }

        if(lightListView.getCheckedItemCount() == 0) {
            isGood = false;
        }

        return isGood;
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
        }
    }
}

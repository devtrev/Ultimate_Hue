package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHSchedule;
import com.trevor.ultimatehue.Alarm;
import com.trevor.ultimatehue.AlarmActivity;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.helpers.AlarmAdapter;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.philips.lighting.model.PHSchedule.RecurringDay.*;

public class AlarmFragment extends Fragment {

    public static final String TAG = AlarmFragment.class.toString();
    public static final int NEW_ALARM_ACTIVITY = 477;


    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private View rootView;
    private ListView lvAlarm;
    private TextView txtAlarmNone;

    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private SQLiteDatabase db;
    private AlarmAdapter adapter;

    private List<Alarm> alarmList;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AlarmFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AlarmFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        rootView = inflater.inflate(R.layout.fragment_alarms, container, false);

        // Setup variable
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();
        phHueSDK.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);

        // This is needed to populate the Menu at the top
        setHasOptionsMenu(true);
        findViewsById();

        alarmList = getAlarms();

        adapter = new AlarmAdapter(getActivity(), alarmList);

        lvAlarm.setOnItemClickListener(listListener);
        lvAlarm.setAdapter(adapter);

        showFirstTimeHelpMessage();

        if(alarmList.size() == 0)
            txtAlarmNone.setVisibility(View.VISIBLE);
        else
            txtAlarmNone.setVisibility(View.INVISIBLE);

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        return rootView;
    }

    private void findViewsById() {
        lvAlarm = (ListView) rootView.findViewById(R.id.lvAlarms);
        txtAlarmNone = (TextView) rootView.findViewById(R.id.txtAlarmNone);

        lvAlarm.setClickable(true);
    }

    private List<Alarm> getAlarms() {
        Log.d(TAG, "getAlarms");

        List<Alarm> alarmList = new ArrayList<Alarm>();

        for(PHSchedule schedule : bridge.getResourceCache().getAllSchedules(true)) {
            String alarmTime;
            boolean enabled;

            if(DateFormat.is24HourFormat(getContext())) {
                Log.d(TAG, "24 Hour date format");

                SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
                alarmTime = sdf.format(schedule.getDate());
            } else {
                Log.d(TAG, "12 Hour date format");

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                alarmTime = sdf.format(schedule.getDate());
            }

            if(schedule.getStatus() == PHSchedule.PHScheduleStatus.DISABLED)
                enabled = false;
            else
                enabled = true;

            String repeats = "No repeat";
            if(schedule.getRecurringDays() == RECURRING_ALL_DAY.getValue()) {
                repeats = "Every Day";
            } else if (schedule.getRecurringDays() == RECURRING_WEEKDAYS.getValue()) {
                    repeats = "Weekdays";
            } else if (schedule.getRecurringDays() == RECURRING_WEEKEND.getValue()) {
                repeats = "Weekend";
            }

            alarmList.add(new Alarm(schedule.getIdentifier(), schedule.getName(), schedule.getDescription(), alarmTime, repeats, enabled));
        }

        for(PHSchedule schedule : bridge.getResourceCache().getAllSchedules(false)) {
            String alarmTime;
            boolean enabled;

            if(DateFormat.is24HourFormat(getContext())) {
                Log.d(TAG, "24 Hour date format");

                SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
                alarmTime = sdf.format(schedule.getDate());
            } else {
                Log.d(TAG, "12 Hour date format");

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                alarmTime = sdf.format(schedule.getDate());
            }

            if(schedule.getStatus() == PHSchedule.PHScheduleStatus.DISABLED)
                enabled = false;
            else
                enabled = true;

            String repeats = "No repeat";
            if(schedule.getRecurringDays() == RECURRING_ALL_DAY.getValue()) {
                repeats = "Every Day";
            } else if (schedule.getRecurringDays() == RECURRING_WEEKDAYS.getValue()) {
                repeats = "Weekdays";
            } else if (schedule.getRecurringDays() == RECURRING_WEEKEND.getValue()) {
                repeats = "Weekends";
            }

            alarmList.add(new Alarm(schedule.getIdentifier(), schedule.getName(), schedule.getDescription(), alarmTime, repeats, enabled));
        }

        return alarmList;
    }

    private AdapterView.OnItemClickListener  listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
            startAlarmActivity(true, alarmList.get(position).getIdentifier());
        }
    };

    private void startAlarmActivity(boolean isUpdate, String alarmIdentifier) {
        Log.d(TAG, "startAlarmActivity");
        Intent intent = new Intent(getContext(), AlarmActivity.class);
        intent.putExtra(Constants.IS_UPDATE, isUpdate);
        intent.putExtra(Constants.ALARM_ID, alarmIdentifier);
        startActivityForResult(intent, NEW_ALARM_ACTIVITY);
    }

    private void showFirstTimeHelpMessage() {
        openDatabase();

        String showHelpMessage = SettingsFactory.getSetting(db, Constants.SETTING_ALARM_HELP);
        if(showHelpMessage.equalsIgnoreCase("false") || showHelpMessage.equalsIgnoreCase("-1")) {
            showHelpDialog();

            SettingsFactory.updateOrInsert(db, Constants.SETTING_ALARM_HELP, "true");
        }

        closeDatabase();
    }

    private void showHelpDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Alarm Help");
        alert.setMessage("Create simple alarms with your lights! To create a new alarm simply click the \"+\" button in the top right of your screen. To edit an existing alarm just click on the alarm you wish to edit." +
                "\n\nAll alarms are created on your Hue bridge, so alarms will continue to work even if your phone is not on. ");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEW_ALARM_ACTIVITY) {
            Log.i(TAG, "New or Update Alarm returned ");

            try {
                Log.i(TAG, "refreshing the alarm list");

                // Sleep to give time to allow for bridge to update
                Thread.sleep(1000);

                phHueSDK = PHHueSDK.create();
                bridge = phHueSDK.getSelectedBridge();
                phHueSDK.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);

                alarmList = getAlarms();
                adapter = new AlarmAdapter(getActivity(), alarmList);
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                Log.e(TAG, "Error while process activity result : " + e.toString(), e);
                e.printStackTrace();
            }
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
            db = (DatabaseHelper.getInstance(getContext())).getWritableDatabase();
        }
    }

    public void startMainActivity() {
        Log.d(TAG, "startMainActivity");
        Intent intent = new Intent(getContext(), com.philips.lighting.quickstart.PHHomeActivity.class);
        startActivity(intent);
    }

    @Override
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

        if (bridge != null) {

            if (phHueSDK != null) {
                if (phHueSDK.isHeartbeatEnabled(bridge)) {
                    phHueSDK.disableHeartbeat(bridge);
                }

                phHueSDK.disconnect(bridge);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_alarm_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.menu_add_new_alarm) {
            Log.d(TAG, "Adding new alarm");

            startAlarmActivity(false, "-1");
        } else if (id == R.id.menu_help){
            Log.d(TAG, "Help");

            showHelpDialog();
        }

        return super.onOptionsItemSelected(item);
    }
}

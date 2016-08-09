package com.trevor.ultimatehue.helpers;


import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHSchedule;
import com.trevor.ultimatehue.Alarm;
import com.trevor.ultimatehue.R;

public class AlarmAdapter extends BaseAdapter {
    private static final String TAG = AlarmAdapter.class.toString();

    private Context context;
    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    private List<Alarm> listAlarm;

    public AlarmAdapter(Context context, List<Alarm> listAlarm) {
        this.context = context;
        this.listAlarm = listAlarm;
    }

    public int getCount() {
        return listAlarm.size();
    }

    public Object getItem(int position) {
        return listAlarm.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Alarm entry = listAlarm.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.alarm_list_item, null);
        }
        TextView txtAlarmName = (TextView) convertView.findViewById(R.id.txtAlarmName);
        txtAlarmName.setText(entry.getName());

        //TextView txtAlarmDescription = (TextView) convertView.findViewById(R.id.txtAlarmDescription);
        //txtAlarmDescription.setText(entry.getDescription());

        TextView txtAlarmTime = (TextView) convertView.findViewById(R.id.txtAlarmTime);
        txtAlarmTime.setText(entry.getRepeats() + " @ " + entry.getTime());

        // Set the onClick Listener on this Switch
        Switch switchAlarm = (Switch) convertView.findViewById(R.id.switchAlarm);
        switchAlarm.setFocusableInTouchMode(false);
        switchAlarm.setFocusable(false);
        switchAlarm.setOnClickListener(onSwitchClickListener);
        // Set the entry, so that you can capture which item was clicked and
        // then remove it
        // As an alternative, you can use the id/position of the item to capture
        // the item
        // that was clicked.
        switchAlarm.setTag(entry);

        switchAlarm.setChecked(entry.isEnabled());

        // Set the onClick Listener on this button
        Button btnDeleteAlarm = (Button) convertView.findViewById(R.id.btnDeleteAlarm);
        btnDeleteAlarm.setFocusableInTouchMode(false);
        btnDeleteAlarm.setFocusable(false);
        btnDeleteAlarm.setOnClickListener(onDeleteClickListener);
        // Set the entry, so that you can capture which item was clicked and
        // then remove it
        // As an alternative, you can use the id/position of the item to capture
        // the item
        // that was clicked.
        btnDeleteAlarm.setTag(entry);

        return convertView;
    }

    private OnClickListener onDeleteClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            final Alarm entry = (Alarm) view.getTag();

            Log.i(TAG, "Delete alarm entry : " + entry.getName());

            if (phHueSDK == null) {
                phHueSDK = PHHueSDK.create();
            }

            if (phHueSDK.getSelectedBridge() == null) {
                Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

                startMainActivity();
            } else {
                if(bridge == null)
                    bridge = phHueSDK.getSelectedBridge();
            }

            boolean found = false;
            PHSchedule phSchedule = null;

            for(PHSchedule schedule : bridge.getResourceCache().getAllSchedules(true)) {
                if(schedule.getName().equals(entry.getName())) {
                    found = true;
                    phSchedule = schedule;
                }
            }

            if(!found) {
                for(PHSchedule schedule : bridge.getResourceCache().getAllSchedules(false)) {
                    if(schedule.getName().equals(entry.getName())) {
                        found = true;
                        phSchedule = schedule;
                    }
                }
            }

            // Only do something if the alarm was found
            if(found) {
                Log.i(TAG, "Deleting the alarm from bridge");

                // Added this in or else it didn't work...
                //phSchedule.setAutoDelete(null);
                bridge.removeSchedule(phSchedule.getIdentifier(), new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule phSchedule) {
                        Log.i(TAG, "onCreated");
                    }

                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess");
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.i(TAG, "onError : " + s);
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
                        Log.i(TAG, "onStateUpdate");
                    }
                });
            } else {
                Toast.makeText(context, "Error occurred while updating the Alarm", Toast.LENGTH_SHORT);
            }

            listAlarm.remove(entry);
            // listPhonebook.remove(view.getId());
            notifyDataSetChanged();
        }
    };

    private OnClickListener onSwitchClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Alarm entry = (Alarm) view.getTag();

            Log.i(TAG, "on/off switch for alarm entry : " + entry.getName());

            if (phHueSDK == null) {
                phHueSDK = PHHueSDK.create();
            }

            if (phHueSDK.getSelectedBridge() == null) {
                Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

                startMainActivity();
            } else {
                if(bridge == null)
                    bridge = phHueSDK.getSelectedBridge();
            }

            boolean found = false;
            PHSchedule phSchedule = null;

            for(PHSchedule schedule : bridge.getResourceCache().getAllSchedules(true)) {
                if(schedule.getName().equals(entry.getName())) {
                    found = true;
                    phSchedule = schedule;
                }
            }

            if(!found) {
                for(PHSchedule schedule : bridge.getResourceCache().getAllSchedules(false)) {
                    if(schedule.getName().equals(entry.getName())) {
                        found = true;
                        phSchedule = schedule;
                    }
                }
            }

            // Only do something if the alarm was found
            if(found) {
                Log.i(TAG, "Updating the alarm status on bridge");

                if(phSchedule.getStatus() == PHSchedule.PHScheduleStatus.DISABLED)
                    phSchedule.setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                else if(phSchedule.getStatus() == PHSchedule.PHScheduleStatus.ENABLED)
                    phSchedule.setStatus(PHSchedule.PHScheduleStatus.DISABLED);

                // Added this in or else it didn't work...
                phSchedule.setAutoDelete(null);
                bridge.updateSchedule(phSchedule, new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule phSchedule) {
                        Log.i(TAG, "onCreated");
                    }

                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess");
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.i(TAG, "onError : " + s);
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
                        Log.i(TAG, "onStateUpdate");
                    }
                });
            } else {
                Toast.makeText(context, "Error occurred while updating the Alarm", Toast.LENGTH_SHORT);
            }

        }
    };

    public void startMainActivity() {
        Log.d(TAG, "startMainActivity");
        Intent intent = new Intent(context, com.philips.lighting.quickstart.PHHomeActivity.class);
        context.startActivity(intent);
    }

    /*@Override
    public void onClick(View view) {
        Alarm entry = (Alarm) view.getTag();
        listAlarm.remove(entry);
        // listPhonebook.remove(view.getId());
        notifyDataSetChanged();

    }*/

    private void showDialog(Alarm entry) {
        // Create and show your dialog
        // Depending on the Dialogs button clicks delete it or do nothing
    }

}


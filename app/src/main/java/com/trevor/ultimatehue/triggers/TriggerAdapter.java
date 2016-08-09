package com.trevor.ultimatehue.triggers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.trevor.ultimatehue.Alarm;
import com.trevor.ultimatehue.R;
import java.util.List;


/**
 * Created by nemo on 4/6/16.
 */
public class TriggerAdapter extends BaseAdapter{

    private static final String TAG = TriggerAdapter.class.toString();

        private Context context;

        private List<Trigger> listTrigger;

        public TriggerAdapter(Context context, List<Trigger> listTrigger) {
            this.context = context;
            this.listTrigger = listTrigger;
        }

        public int getCount() {
            return listTrigger.size();
        }

        public Object getItem(int position) {
            return listTrigger.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup viewGroup) {
            Trigger entry = listTrigger.get(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.trigger_list_item, null);
            }

            TextView txtTriggerName = (TextView) convertView.findViewById(R.id.txtTriggerName);
            txtTriggerName.setText(entry.getName());

            TextView txtTriggerDesc = (TextView) convertView.findViewById(R.id.txtTriggerDescription);
            txtTriggerDesc.setText(entry.getDescription());

            // Set the entry, so that you can capture which item was clicked and
            // then remove it
            // As an alternative, you can use the id/position of the item to capture
            // the item
            // that was clicked.
            //btnDeleteAlarm.setTag(entry);

            return convertView;
        }

        private View.OnClickListener onDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Alarm entry = (Alarm) view.getTag();

                Log.i(TAG, "Delete alarm entry : " + entry.getName());

                listTrigger.remove(entry);
                // listPhonebook.remove(view.getId());
                notifyDataSetChanged();
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


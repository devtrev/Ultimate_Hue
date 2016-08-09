package com.trevor.ultimatehue.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.TriggerActivity;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.factory.TriggerFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.triggers.Trigger;
import com.trevor.ultimatehue.triggers.TriggerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TriggerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TriggerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TriggerFragment extends Fragment {

    public static final String TAG = TriggerFragment.class.toString();

    public static final int NEW_TRIGGER_ACTIVITY = 476;
    private static final String ARG_SECTION_NUMBER = "section_number";

    private View rootView;
    private ListView lvTrigger;

    private SQLiteDatabase db;
    private TriggerAdapter adapter;
    private List<Trigger> triggerList;

    private OnFragmentInteractionListener mListener;

    public TriggerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TriggerFragment.
     */
    public static TriggerFragment newInstance(int sectionNumber) {
        TriggerFragment fragment = new TriggerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_trigger2, container, false);

        // This is needed to populate the Menu at the top
        setHasOptionsMenu(true);

        // Instantiate the layout IDs
        findViewsById();

        triggerList = getTriggers();

        adapter = new TriggerAdapter(getActivity(), triggerList);

        lvTrigger.setOnItemClickListener(listListener);
        lvTrigger.setAdapter(adapter);

        // Show Help Message
        showFirstTimeHelpMessage();

        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        return rootView;
    }

    private List<Trigger> getTriggers() {
        Log.d(TAG, "getTriggers");

        openDatabase();

        List<Trigger> triggerList = TriggerFactory.getTriggerList(db);

        closeDatabase();

        return triggerList;
    }

    private void findViewsById() {
        lvTrigger = (ListView) rootView.findViewById(R.id.lvTrigger);

        lvTrigger.setClickable(true);
    }

    private AdapterView.OnItemClickListener  listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
            startTriggerActivity(triggerList.get(position).getIdentifier());
        }
    };

    private void startTriggerActivity(String triggerIdentifier) {
        Log.d(TAG, "startTriggerActivity");
        Intent intent = new Intent(getContext(), TriggerActivity.class);
        intent.putExtra(Constants.TRIGGER_ID, triggerIdentifier);
        startActivityForResult(intent, NEW_TRIGGER_ACTIVITY);
    }

    private void showFirstTimeHelpMessage() {
        openDatabase();

        String showHelpMessage = SettingsFactory.getSetting(db, Constants.SETTING_TRIGGER_HELP);
        if(showHelpMessage.equalsIgnoreCase("false") || showHelpMessage.equalsIgnoreCase("-1")) {
            // Shows the Beta Message
            showBetaDialog();

            // Show the first time message
            showHelpDialog();

            // Update settings to show the first time message has been displayed
            SettingsFactory.updateOrInsert(db, Constants.SETTING_TRIGGER_HELP, "true");
        }

        closeDatabase();
    }

    private void showHelpDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        // Text for helpMessage
        alert.setTitle("Trigger Help");
        alert.setMessage("This screen will allow you to setup 'Triggers' that will set the lights based off of a particular action. Simply click the " +
                "Trigger from the list below that you would like to setup. This will take you to the Trigger setup screen where you can specify what " +
                "action you want taken when the trigger is activated");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    private void showBetaDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        String betaMessage = "This is currently in BETA. As such there may be some issues with certain functions. There will be more Triggers to come in the future" +
                " but for now they are limited. Should you have any problems or want to suggest a Trigger please send me an email @ dev.trev22@gmail.com";

        // Show Beta Message Text
        alert.setTitle("BETA - Trigger ");
        alert.setMessage(betaMessage);

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

    // Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_trigger_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_help){
            Log.d(TAG, "Help");

            showHelpDialog();
        }

        return super.onOptionsItemSelected(item);
    }
}

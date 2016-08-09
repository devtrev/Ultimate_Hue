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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.trevor.ultimatehue.CustomColorActivity;
import com.trevor.ultimatehue.HueColor;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.CustomGrid;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.OnColorSelectedListener;

import java.util.List;

public class CustomColorFragment extends Fragment {
    public static final String TAG = CustomColorFragment.class.toString();

    private SQLiteDatabase db;
    private Context context;
    private List<HueColor> hueColorList;
    private GridView gridView;
    private Button addCustomColor;
    OnColorSelectedListener mListener;
    private boolean isGroup;
    private String lightIdentifier;

    public static CustomColorFragment newInstance(String lightIdentifier, boolean isGroup) {
        CustomColorFragment f = new CustomColorFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(Constants.LIGHT_IDENTIFIER, lightIdentifier);
        args.putBoolean(Constants.IS_GROUP, isGroup);
        f.setArguments(args);
        return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if(args != null) {
            lightIdentifier = args.getString(Constants.LIGHT_IDENTIFIER, "-1");
            isGroup = args.getBoolean(Constants.IS_GROUP);
        } else {
            lightIdentifier = "-1";
            isGroup = false;
        }

        context = container.getContext();
        final View rootView = inflater.inflate(R.layout.fragment_custom_color, container, false);

        openDatabase();
        hueColorList = ColorFactory.getColorListByKey(db, Constants.CUSTOM_COLOR_TYPE);
        boolean showHelp = Boolean.parseBoolean(SettingsFactory.getSetting(db, Constants.SETTING_CUSTOM_COLOR_HELP));
        closeDatabase();

        // Customer Grid adapter for having an image and text
        CustomGrid adapter = new CustomGrid(rootView.getContext(), hueColorList);

        addCustomColor = (Button) rootView.findViewById(R.id.btnAddCustomColor);
        gridView = (GridView) rootView.findViewById(R.id.gridview_custom);

        registerForContextMenu(gridView);
        setOnClickListeners();

        gridView.setAdapter(adapter);

        Log.i(TAG, "help=" + showHelp);
        if(showHelp && hueColorList.size() > 0) {
            showHelpDialog();
        }

        return rootView;
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners");

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d(TAG, "setOnItemClickListener");

                AnalyticsHelper application = (AnalyticsHelper) getActivity().getApplication();
                String category = "Color Picker";
                String action = "Advanced Color";
                String label = hueColorList.get(position).getName();

                Log.i(TAG, "Custom Color Fragment Analytics");
                AnalyticsHelper.analyticsEvent(application, category, action, label);

                mListener.onColorSelected(hueColorList.get(position));
            }
        });

        addCustomColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                Intent intent = new Intent(context, CustomColorActivity.class);
                intent.putExtra(Constants.LIGHT_IDENTIFIER, lightIdentifier);
                intent.putExtra(Constants.IS_GROUP, isGroup);
                intent.putExtra(Constants.IS_UPDATE, false);
                startActivityForResult(intent, Constants.CUSTOM_COLOR_ACTIVITY);
            }
        });
    }

    private void showHelpDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle("Custom Colors Help");
        alert.setMessage("In order to update or delete the created color simply long press on the item you wish to update or delete.");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
            }
        });

        alert.show();

        openDatabase();
        SettingsFactory.updateSetting(db, Constants.SETTING_CUSTOM_COLOR_HELP, "false");
        closeDatabase();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.CUSTOM_COLOR_ACTIVITY) {
            Log.i(TAG, "Custom Color returned");

            HueColor hueColor = new HueColor();
            hueColor.setHue((int) data.getExtras().get(Constants.HUE));
            hueColor.setSaturation((int) data.getExtras().get(Constants.SATURATION));
            hueColor.setBrightness((int) data.getExtras().get(Constants.BRIGHTNESS));
            hueColor.setName((String) data.getExtras().get(Constants.CUSTOM_COLOR_NAME));
            hueColor.setTimesClicked((int) data.getExtras().get(Constants.TIMES_CLICKED));
            hueColor.setKey(Constants.CUSTOM_COLOR_TYPE);
            hueColor.setFavorite((int) data.getExtras().get(Constants.FAVORITE));
            hueColor.setImageId("ic_custom");

            boolean isUpdate = (boolean) data.getExtras().get(Constants.IS_UPDATE);

            openDatabase();

            if(isUpdate) {
                hueColor.set_id((long) data.getExtras().get(Constants.ID));
                ColorFactory.update(db, hueColor);
            }
            else {
                ColorFactory.insert(db , hueColor);
            }

            closeDatabase();

            mListener.onColorSelected(hueColor);
        }

    }



        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Log.i(TAG, "onCreateContextMenu");
            if (v.getId() == R.id.gridview_custom) {
                String[] menuItems = getResources().getStringArray(R.array.custom_color_menu);
                for (int i = 0; i < menuItems.length; i++) {
                    menu.add(Menu.NONE, i, i, menuItems[i]);
                }
            }
        }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected " + item.getItemId());

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 0 :
                Log.d(TAG, "Doing update");
                updateCustomLight(info.position);
                break;
            case 1 :
                Log.d(TAG, "Doing Delete");
                deleteCustomLight(info.position);
                break;
        }

        //ProfileTile tile = (ProfileTile) info.targetView;//Here we get a grip again of the view that opened the Context Menu
        return super.onContextItemSelected(item);
    }

    private void updateCustomLight(int position) {
        Intent intent = new Intent(context, CustomColorActivity.class);
        intent.putExtra(Constants.LIGHT_IDENTIFIER, lightIdentifier);
        intent.putExtra(Constants.IS_GROUP, isGroup);
        intent.putExtra(Constants.IS_UPDATE, true);
        intent.putExtra(Constants.ID, hueColorList.get(position).get_id());
        intent.putExtra(Constants.TIMES_CLICKED, hueColorList.get(position).getTimesClicked());
        intent.putExtra(Constants.FAVORITE, hueColorList.get(position).getFavorite());
        intent.putExtra(Constants.HUE, hueColorList.get(position).getHue());
        intent.putExtra(Constants.SATURATION, hueColorList.get(position).getSaturation());
        intent.putExtra(Constants.BRIGHTNESS, hueColorList.get(position).getBrightness());
        intent.putExtra(Constants.CUSTOM_COLOR_NAME, hueColorList.get(position).getName());

        startActivityForResult(intent, Constants.CUSTOM_COLOR_ACTIVITY);
    }

    private void deleteCustomLight(int position) {
        Log.i(TAG, "deleteCustomLight");

        openDatabase();
        ColorFactory.delete(db, hueColorList.get(position));

        hueColorList = ColorFactory.getColorListByKey(db, Constants.CUSTOM_COLOR_TYPE);

        gridView.invalidateViews();
        gridView.setAdapter(new CustomGrid(context, hueColorList));

        closeDatabase();
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnColorSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnColorSelectedListener");
        }
    }
}

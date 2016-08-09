package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.trevor.ultimatehue.HueColor;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.ColorFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.CustomGrid;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.OnColorSelectedListener;

import java.util.List;

public class AdvancedColorFragment extends Fragment {
    public static final String TAG = AdvancedColorFragment.class.toString();

    OnColorSelectedListener mListener;

    private SQLiteDatabase db;
    private Context context;
    private List<HueColor> hueColorList;
    //private String[] colors;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = container.getContext();
        final View rootView = inflater.inflate(R.layout.grid_view, container, false);

        //colors = getResources().getStringArray(R.array.advanced_color_text);
        openDatabase();
        hueColorList = ColorFactory.getColorListByKey(db, Constants.ADVANCED_COLOR_TYPE);
        closeDatabase();

        // Customer Grid adapter for having an image and text
        CustomGrid adapter = new CustomGrid(rootView.getContext(), hueColorList);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                AnalyticsHelper application = (AnalyticsHelper) getActivity().getApplication();
                String category = "Color Picker";
                String action = "Advanced Color";
                String label = hueColorList.get(position).getName();

                Log.i(TAG, "Advanced Color Activity Analytics");
                AnalyticsHelper.analyticsEvent(application, category, action, label);

                mListener.onColorSelected(hueColorList.get(position));
            }
        });

        return rootView;
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

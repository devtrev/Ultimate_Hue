package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.EffectsFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.CustomEffectGrid;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.helpers.OnColorSelectedListener;

import java.util.List;


public class BasicEffectColorFragment extends Fragment {
    public static final String TAG = BasicEffectColorFragment.class.toString();

    OnColorSelectedListener mListener;
    private SQLiteDatabase db;
    private List<Effect> effects;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.grid_view, container, false);

        openDatabase();
        effects = EffectsFactory.getEffectListByKey(db, Constants.COMMON_EFFECT_COLOR_TYPE);
        closeDatabase();

        // Customer Grid adapter for having an image and text
        CustomEffectGrid adapter = new CustomEffectGrid(rootView.getContext(), effects);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                AnalyticsHelper application = (AnalyticsHelper) getActivity().getApplication();
                String category = "Effect Picker";
                String action = "Basic Effect";
                String label = effects.get(position).getName();

                Log.i(TAG, "Basic Effect Fragment Analytics");
                AnalyticsHelper.analyticsEvent(application, category, action, label);

                mListener.onEffectSelected(effects.get(position));
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
            db = (DatabaseHelper.getInstance(getContext())).getWritableDatabase();
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

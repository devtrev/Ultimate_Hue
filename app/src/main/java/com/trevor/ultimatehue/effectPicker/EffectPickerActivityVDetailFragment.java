package com.trevor.ultimatehue.effectPicker;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.trevor.ultimatehue.factory.EffectsFactory;
import com.trevor.ultimatehue.fragments.AdvancedColorFragment;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.CustomEffectGrid;
import com.trevor.ultimatehue.helpers.CustomGrid;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.helpers.OnColorSelectedListener;

import java.util.List;

/**
 * A fragment representing a single ColorPickerActivityV2 detail screen.
 * This fragment is either contained in a {@link EffectPickerActivityVListActivity}
 * in two-pane mode (on tablets) or a {@link EffectPickerActivityVDetailActivity}
 * on handsets.
 */
public class EffectPickerActivityVDetailFragment extends Fragment {

    public static final String TAG = EffectPickerActivityVDetailFragment.class.toString();

    OnColorSelectedListener mListener;
    private View rootView;

    private SQLiteDatabase db;
    private Context context;
    private List<HueColor> hueColorList;
    private List<Effect> effectsList;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private EffectPickerContent.EffectItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EffectPickerActivityVDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = EffectPickerContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.name);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.grid_view, container, false);


        // Show the dummy content as text in a TextView.
        if (mItem != null) {


            //((TextView) rootView.findViewById(R.id.colorpickeractivityv_detail)).setText(mItem.image);
            if(mItem.detailType == Constants.DETAIL_TYPE_COLOR) {
                Log.d(TAG, "Loading from colors database table");

                loadColors();
            } else if(mItem.detailType == Constants.DETAIL_TYPE_EFFECT) {
                Log.d(TAG, "Loading from effects database table");

                loadEffects();
            }


        }

        return rootView;
    }

    private void loadColors() {
        openDatabase();
        hueColorList = ColorFactory.getColorListByKey(db, mItem.id);
        closeDatabase();

        Log.i(TAG, "SIZE OF LIST = " + hueColorList.size());

        // Customer Grid adapter for having an image and text
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(new CustomGrid(getActivity(), hueColorList));

        // Set Listener
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
    }

    private void loadEffects() {
        openDatabase();
        effectsList = EffectsFactory.getEffectListByKey(db, mItem.id);
        closeDatabase();

        // Customer Grid adapter for having an image and text
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(new CustomEffectGrid(getActivity(), effectsList));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                AnalyticsHelper application = (AnalyticsHelper) getActivity().getApplication();
                String category = "Effect Picker";
                String action = "Seasonal Effect";
                String label = effectsList.get(position).getName();

                Log.i(TAG, "Seasonal Effect Fragment Analytics");
                AnalyticsHelper.analyticsEvent(application, category, action, label);

                mListener.onEffectSelected(effectsList.get(position));
            }
        });
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

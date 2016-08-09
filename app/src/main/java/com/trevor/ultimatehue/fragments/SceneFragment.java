package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHScene;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;


public class SceneFragment extends Fragment {

    public static final String TAG = BasicColorFragment.class.toString();

    private static final String ARG_SECTION_NUMBER = "section_number";

    private View rootView;
    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SceneFragment.
     */
    public static SceneFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        SceneFragment fragment = new SceneFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SceneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_scene, container, false);

        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        findViewsById();
        populateView();
        setupListeners();

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        // Inflate the layout for this fragment
        return rootView;
    }

    private void findViewsById() {
        Log.d(TAG, "findViewsById");
    }

    private void populateView() {
        Log.d(TAG, "populateView");

        Log.i(TAG, "Total Scenes = " + bridge.getResourceCache().getAllScenes().size());
        for(PHScene scene : bridge.getResourceCache().getAllScenes()) {
            Log.d(TAG, "Scene Name : " + scene.getName());
        }
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners");
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }
}

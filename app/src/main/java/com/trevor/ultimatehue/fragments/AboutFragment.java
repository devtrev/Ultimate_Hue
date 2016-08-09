package com.trevor.ultimatehue.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trevor.ultimatehue.BuildConfig;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.R;

import org.w3c.dom.Text;


public class AboutFragment extends Fragment {

    private static final String TAG = AboutFragment.class.toString();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private TextView txtVersion;

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_about, container, false);

        txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);

        txtVersion.setText("App Version : " + BuildConfig.VERSION_NAME);
        txtVersion.setTypeface(null, Typeface.BOLD);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}

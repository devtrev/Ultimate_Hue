package com.trevor.ultimatehue;


import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.trevor.ultimatehue.factory.EffectsFactory;
import com.trevor.ultimatehue.fragments.AllLightsFragment;
import com.trevor.ultimatehue.fragments.BasicEffectColorFragment;
import com.trevor.ultimatehue.fragments.EffectsFragment;
import com.trevor.ultimatehue.fragments.SeasonalEffectColorFragment;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import android.support.v7.app.AppCompatActivity;

import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.helpers.OnColorSelectedListener;

import java.util.Locale;

public class EffectPickerActivity extends AppCompatActivity implements ActionBar.TabListener, OnColorSelectedListener {

    private static final String TAG = EffectPickerActivity.class.toString();
    private static final int PAGE_COUNT = 2;

    private SQLiteDatabase db;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getApplication(), getClass().getSimpleName());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_effect_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onEffectSelected(Effect effect) {
        Log.i(TAG, "onEffectSelected");

        if(effect != null) {
            openDatabase();
            EffectsFactory.updateTimesClicked(db, effect);
            closeDatabase();

            Intent intent = new Intent(this.getApplicationContext(), AllLightsFragment.class);
            intent.putExtra(Constants.EFFECT_NAME, effect.getName());
            intent.putExtra(Constants.HUE, effect.getHue());
            intent.putExtra(Constants.SATURATION, effect.getSaturation());
            intent.putExtra(Constants.BRIGHTNESS, effect.getBrightness());
            intent.putExtra(Constants.HUE_IMAGE, effect.getImageId());
            intent.putExtra(Constants.SLEEP, effect.getSleep());
            intent.putExtra(Constants.RANDOM_LIGHT, effect.getRandomLight());
            intent.putExtra(Constants.TRANSITION_TIME, effect.getTransitionTime());
            intent.putExtra(Constants.EFFECT_SOUND, effect.getSoundId());
            intent.putExtra(Constants.EFFECT_DESCRIPTION, effect.getDescription());
            setResult(EffectsFragment.EFFECT_PICKER_RESULT, intent);

        } else {
            Intent intent = new Intent(this.getApplicationContext(), AllLightsFragment.class);
            intent.putExtra(Constants.EFFECT_NAME, "NA");
            intent.putExtra(Constants.HUE, -1);
            intent.putExtra(Constants.SATURATION, -1);
            intent.putExtra(Constants.BRIGHTNESS, -1);
            intent.putExtra(Constants.HUE_IMAGE, -1);
            intent.putExtra(Constants.SLEEP, -1);
            intent.putExtra(Constants.EFFECT_SOUND, -1);
            intent.putExtra(Constants.EFFECT_DESCRIPTION, "");
            setResult(EffectsFragment.EFFECT_PICKER_RESULT, intent);
        }

        // Close the Effect Picker View
        finish();
    }

    @Override
    public void onColorSelected(HueColor hueColor) {
        // NOTHING TO DO
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.

            // Call the correct Fragment based off of the tab selected
            switch (position) {
                case 0:
                    // Should return GridView of Basic Colors
                    Log.d(TAG, "Loading BasicEffectColorFragment");
                    return new BasicEffectColorFragment();
                case 1:
                    // Should return GridView of Seasonal Colors
                    Log.d(TAG, "Loading SeasonalEffectColorFragment");
                    return new SeasonalEffectColorFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_common_effect).toUpperCase(l);
                case 1:
                    return getString(R.string.title_seasonal_effect).toUpperCase(l);
            }
            return null;
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
            db = (DatabaseHelper.getInstance(getApplicationContext())).getWritableDatabase();
        }
    }
}


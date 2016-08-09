package com.trevor.ultimatehue;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import com.philips.lighting.data.HueSharedPreferences;
import com.trevor.ultimatehue.fragments.AboutFragment;
import com.trevor.ultimatehue.fragments.AlarmFragment;
import com.trevor.ultimatehue.fragments.AllGroupsFragment;
import com.trevor.ultimatehue.fragments.AllGroupsFragmentV2;
import com.trevor.ultimatehue.fragments.AllLightsFragment;
import com.trevor.ultimatehue.fragments.AllLightsFragmentV2;
import com.trevor.ultimatehue.fragments.EffectsFragment;
import com.trevor.ultimatehue.fragments.EffectsFragmentV2;
import com.trevor.ultimatehue.fragments.GetHueFragment;
import com.trevor.ultimatehue.fragments.HelpFragment;
import com.trevor.ultimatehue.fragments.MemoryFragment;
import com.trevor.ultimatehue.fragments.MusicFragment;
import com.trevor.ultimatehue.fragments.TriggerFragment;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.triggers.BatteryChangeReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = MainActivity.class.toString();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        /*BatteryChangeReceiver batteryChangeReceiver = new BatteryChangeReceiver();
        registerReceiver(batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW)); // register in activity or service
        registerReceiver(batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_OKAY)); // register in activity or service
        //registerReceiver(batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); // register in activity or service
        */

    }

    @Override
    public void onNavigationDrawerItemSelected(int position){
        Log.i(TAG, "onNavigationDrawerItemSelected(" + position + ")");

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {
            case 0 :
                Log.d(TAG, "Trying to load AllGroupsFragment");

                fragmentManager.beginTransaction()
                        .replace(R.id.container, AllGroupsFragment.newInstance(position + 1), getString(R.string.light_groups))
                        .commit();
                break;
            case 1 :
                Log.d(TAG, "Trying to load AllLightsFragment");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, AllLightsFragment.newInstance(position + 1), getString(R.string.individual_lights))
                        .commit();
                break;
            case 2 :
                Log.d(TAG, "Trying to load EffectsFragment");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, EffectsFragment.newInstance(position + 1), getString(R.string.effects))
                        .commit();
                break;
            case 3 :
                Log.d(TAG, "Trying to load MusicFragment");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, MusicFragment.newInstance(position + 1), getString(R.string.music))
                        .commit();
                break;
            /*case 4 :
                Log.d(TAG, "Trying to load Scenes");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SceneFragment.newInstance(position + 1))
                        .commit();
                break;*/
            case 4 :
                Log.d(TAG, "Trying to load Alarms");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, AlarmFragment.newInstance(position + 1), getString(R.string.alarms))
                        .commit();
                break;
            case 5 :
            Log.d(TAG, "Trying to load Memory");
            fragmentManager.beginTransaction()
                        .replace(R.id.container, MemoryFragment.newInstance(position + 1), getString(R.string.memory))
                        .commit();
                break;
            case 6 :
            Log.d(TAG, "Trying to load Triggers Fragment");
            fragmentManager.beginTransaction()
                        .replace(R.id.container, TriggerFragment.newInstance(position + 1), getString(R.string.triggers))
                        .commit();
                break;
            case 7 :
                Log.d(TAG, "Trying to load About");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, AboutFragment.newInstance(position + 1), getString(R.string.about))
                        .commit();
                break;
            /*case 8 :
                Log.d(TAG, "Hue Color Help");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, GetHueFragment.newInstance(position + 1), getString(R.string.hue_color_settings))
                        .commit();
                break;*/
        }
    }

    public void onSectionAttached(int number) {
        Log.d(TAG, "OnSectionAttached " + number);
        switch (number) {
            case 1:
                mTitle = getString(R.string.light_groups);
                break;
            case 2:
                mTitle = getString(R.string.individual_lights);
                break;
            case 3:
                mTitle = getString(R.string.effects);
                break;
            case 4:
                mTitle = getString(R.string.music);
                break;
            /*case 5:
                mTitle = getString(R.string.title_section5);
                break;*/
            case 5:
                mTitle = getString(R.string.alarms);
                break;
            case 6:
                mTitle = getString(R.string.memory);
                break;
            case 7:
                mTitle = getString(R.string.triggers);
                break;
            case 8:
                mTitle = getString(R.string.about);
                break;
            /*case 9:
                mTitle = getString(R.string.hue_color_settings);
                break;
                */
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        //return super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();

        HueSharedPreferences prefs = HueSharedPreferences.getInstance(getApplicationContext());

        if(getSupportFragmentManager().findFragmentByTag(getString(R.string.light_groups)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.light_groups)).isVisible())
            prefs.setFragment(getString(R.string.light_groups));
        else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.individual_lights)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.individual_lights)).isVisible())
            prefs.setFragment(getString(R.string.individual_lights));
        else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.effects)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.effects)).isVisible())
            prefs.setFragment(getString(R.string.effects));
        else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.music)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.music)).isVisible())
            prefs.setFragment(getString(R.string.music));
        else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.alarms)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.alarms)).isVisible())
            prefs.setFragment(getString(R.string.alarms));
        else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.memory)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.memory)).isVisible())
            prefs.setFragment(getString(R.string.memory));
        else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.triggers)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.triggers)).isVisible())
            prefs.setFragment(getString(R.string.triggers));
        else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.about)) != null && getSupportFragmentManager().findFragmentByTag(getString(R.string.about)).isVisible())
            prefs.setFragment(getString(R.string.about));
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        HueSharedPreferences prefs = HueSharedPreferences.getInstance(getApplicationContext());

        String lastFrag = prefs.getFragment();
        Log.i(TAG , "Last fragment was " + lastFrag);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (lastFrag.equals(getString(R.string.light_groups))) {
            onNavigationDrawerItemSelected(0);
        } else if (lastFrag.equals(getString(R.string.individual_lights))) {
            onNavigationDrawerItemSelected(1);
        } else if (lastFrag.equals(getString(R.string.effects))) {
            onNavigationDrawerItemSelected(2);
        } else if (lastFrag.equals(getString(R.string.music))) {
            onNavigationDrawerItemSelected(3);
        } else if (lastFrag.equals(getString(R.string.alarms))) {
            onNavigationDrawerItemSelected(4);
        } else if (lastFrag.equals(getString(R.string.memory))) {
            onNavigationDrawerItemSelected(5);
        } else if (lastFrag.equals(getString(R.string.triggers))) {
            onNavigationDrawerItemSelected(6);
        } else if (lastFrag.equals(getString(R.string.about))) {
            onNavigationDrawerItemSelected(7);
        }

    }
    /*@Override
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
    }*/
}

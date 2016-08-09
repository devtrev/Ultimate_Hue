package com.trevor.ultimatehue.helpers;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.trevor.ultimatehue.R;


/**
 * Created by nemo on 10/4/15.
 */
public class AnalyticsHelper extends Application {

    private static final String TAG = AnalyticsHelper.class.toString();
    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    // Reports Screen capture to Google Analytics
    public static void analyticsScreenCapture(AnalyticsHelper application, String screenName) {
        try {
            // Create local Tracker
            Tracker tracker = application.getDefaultTracker();

            // Send a screen
            Log.i(TAG, "Setting screen name: " + screenName);
            tracker.setScreenName("Ultimate Hue - " + screenName);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());

        } catch (Exception e) {
            Log.e(TAG, "Unable to Load analytics due to following error \n" + e.toString());
        }
    }

    public static void analyticsEvent(AnalyticsHelper application, String category, String action, String label) {
        Log.i(TAG, "category = " + category + ", action = " + action + ", label = " + label);

        Tracker tracker = application.getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }
}

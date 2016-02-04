package com.enamakel.thebigindiannews.activities.base;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.enamakel.thebigindiannews.NewsApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * This is the base activity which extends all other activities.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected Tracker tracker;
    static String TAG = "BaseActivity";


    protected String getTrackingName() {
        return getLocalClassName();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the shared Tracker instance.
        NewsApplication application = (NewsApplication) getApplication();
        tracker = application.getDefaultTracker();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Setting screen name: " + getTrackingName());
        tracker.setScreenName(getTrackingName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
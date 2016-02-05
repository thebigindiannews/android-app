/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enamakel.thebigindiannews;


import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import com.enamakel.thebigindiannews.util.FontCache;
import com.enamakel.thebigindiannews.util.Preferences;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import dagger.ObjectGraph;


public class NewsApplication extends Application {
    public static Typeface TYPE_FACE = null;
    public static Typeface TYPE_FACE_BOLD = null;
    RefWatcher refWatcher;
    Tracker tracker;
    ObjectGraph applicationGraph;
    static Context context;


    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.xml.global_tracker);

            // Enable Display Features.
            tracker.enableAdvertisingIdCollection(true);
        }
        return tracker;
    }


    public static RefWatcher getRefWatcher(Context context) {
        NewsApplication newsApplication = (NewsApplication) context.getApplicationContext();
        return newsApplication.refWatcher;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        refWatcher = LeakCanary.install(this);
        applicationGraph = ObjectGraph.create();
        Preferences.migrate(this);

        TYPE_FACE = FontCache.get(this, Preferences.Theme.getTypeface(this));
        TYPE_FACE_BOLD = FontCache.getBold(this, Preferences.Theme.getTypeface(this));

        AppUtils.registerAccountsUpdatedListener(this);
    }

    public static Context getContext() {
        return context;
    }

    public ObjectGraph getApplicationGraph() {
        return applicationGraph;
    }
}
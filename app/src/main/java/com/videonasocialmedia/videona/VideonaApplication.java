/*
 * Copyright (C) 2015 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas
 */

package com.videonasocialmedia.videona;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.qordoba.sdk.Qordoba;

public class VideonaApplication extends Application {


    Tracker app_tracker;

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        setupGoogleAnalytics();
        setupQordoba();
    }

    private void setupQordoba() {
        Qordoba.init(this, "dev_a2e09b7e-fe87-4ea0-9f44-64ce45922798",
                "43d79114-c163-419f-bb57-275533726cd1");
    }

    private void setupGoogleAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        app_tracker = analytics.newTracker(R.xml.app_tracker);
        app_tracker.enableAdvertisingIdCollection(true);
    }

    /**
     * @return google analytics tracker
     */
    public synchronized Tracker getTracker() {
        return app_tracker;
    }
}

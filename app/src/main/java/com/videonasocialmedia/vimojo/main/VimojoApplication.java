/*
 * Copyright (C) 2015 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas
 */

package com.videonasocialmedia.vimojo.main;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;
import com.squareup.leakcanary.LeakCanary;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.main.modules.ApplicationModule;
import com.videonasocialmedia.vimojo.main.modules.DataRepositoriesModule;
import com.videonasocialmedia.vimojo.main.modules.ActivityPresentersModule;
import com.videonasocialmedia.vimojo.main.modules.TrackerModule;
import com.videonasocialmedia.vimojo.model.VimojoMigration;
import com.videonasocialmedia.vimojo.utils.ConfigPreferences;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class VimojoApplication extends Application {

    private SystemComponent systemComponent;
    private static Context context;

    Tracker appTracker;
    private DataRepositoriesModule dataRepositoriesModule;
    private ApplicationModule applicationModule;

    public static Context getAppContext() {
        return VimojoApplication.context;
    }

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
        initSystemComponent();
        Fabric.with(this, new Crashlytics());
        context = getApplicationContext();
        setupGoogleAnalytics();
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        Dexter.initialize(this);
        setupLeakCanary();
        setupDataBase();
    }

    void initSystemComponent() {
        this.systemComponent = DaggerSystemComponent.builder()
                .applicationModule(getApplicationModule())
                .dataRepositoriesModule(getDataRepositoriesModule())
                .trackerModule(getTrackerModule())
//                .activityPresentersModule(getActivityPresentersModule())
                .build();
    }

    @NonNull
    public ApplicationModule getApplicationModule() {
        if (applicationModule == null) {
            applicationModule = new ApplicationModule(this);
        }
        return applicationModule;
    }

    private TrackerModule getTrackerModule() {
        return new TrackerModule(this);
    }

    private ActivityPresentersModule getActivityPresentersModule() {
        return new ActivityPresentersModule(null);
    }

    public SystemComponent getSystemComponent() {
        return this.systemComponent;
    }

    public DataRepositoriesModule getDataRepositoriesModule() {
        if (dataRepositoriesModule == null) {
            dataRepositoriesModule = new DataRepositoriesModule();
        }
        return dataRepositoriesModule;
    }


    private void setupGoogleAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        if (BuildConfig.DEBUG)
            analytics.setDryRun(true);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        appTracker = analytics.newTracker(R.xml.app_tracker);
        appTracker.enableAdvertisingIdCollection(true);
    }

    private void setupLeakCanary() {
        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }
    }

    protected void setupDataBase() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("vimojoDB")
                .schemaVersion(6) // 25042017 - v0.4.23 21070227
                .migration(new VimojoMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * @return google analytics tracker
     */
    public synchronized Tracker getTracker() {
        return appTracker;
    }

}

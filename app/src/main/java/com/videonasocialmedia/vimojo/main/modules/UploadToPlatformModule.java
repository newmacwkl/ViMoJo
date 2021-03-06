/*
 * Copyright (C) 2018 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.vimojo.main.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.videonasocialmedia.vimojo.auth0.GetUserId;
import com.videonasocialmedia.vimojo.auth0.UserAuth0Helper;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.repository.upload.datasource.UploadRealmDataSource;
import com.videonasocialmedia.vimojo.repository.upload.UploadDataSource;
import com.videonasocialmedia.vimojo.sync.presentation.UploadToPlatform;
import com.videonasocialmedia.vimojo.sync.presentation.ui.UploadNotification;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;
import com.videonasocialmedia.vimojo.vimojoapiclient.UserApiClient;
import com.videonasocialmedia.vimojo.vimojoapiclient.VideoApiClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by alvaro on 28/2/18.
 * Module upload to platform queue
 * Needed for injection queue and add testing.
 */

@Module
public class UploadToPlatformModule {

  private final Context context;

  public UploadToPlatformModule(VimojoApplication application) {
   this.context = application;
  }

  @Provides
  UploadToPlatform provideUploadToPlatform(UploadNotification uploadNotification,
                                           VideoApiClient videoApiClient,
                                           UserApiClient userApiClient,
                                           UserAuth0Helper userAuth0Helper,
                                           UploadDataSource uploadRepository,
                                           GetUserId getUserId) {
    return new UploadToPlatform(context, uploadNotification, videoApiClient, userApiClient,
        userAuth0Helper, uploadRepository, getUserId);
  }

  @Provides
  UploadNotification providesUploadNotification() {
    return new UploadNotification(context);
  }

  @Provides
  VideoApiClient providesVideoApiClient() {
    return new VideoApiClient();
  }

  @Provides
  UserApiClient providesUserApiClient() {
    return new UserApiClient();
  }

  @Provides
  UserAuth0Helper providesUserAuth0Helper(UserApiClient userApiClient,
                                          SharedPreferences sharedPreferences,
                                          UserEventTracker userEventTracker) {
    return new UserAuth0Helper(userApiClient, sharedPreferences, userEventTracker);
  }

  @Singleton @Provides
  UploadDataSource providesUploadRepository() {
    return new UploadRealmDataSource();
  }
}

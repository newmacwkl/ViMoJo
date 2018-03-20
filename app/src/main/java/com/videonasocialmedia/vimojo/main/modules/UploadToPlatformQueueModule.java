/*
 * Copyright (C) 2018 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.vimojo.main.modules;

import android.content.Context;

import com.videonasocialmedia.vimojo.auth.domain.usecase.GetAuthToken;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.sync.UploadNotification;
import com.videonasocialmedia.vimojo.sync.UploadToPlatformQueue;
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
public class UploadToPlatformQueueModule {

  private final Context context;

  public UploadToPlatformQueueModule(VimojoApplication application) {
   this.context = application;
  }

  @Singleton @Provides
  UploadToPlatformQueue provideUploadToPlatformQueue(UploadNotification uploadNotification,
                                                     VideoApiClient videoApiClient,
                                                     GetAuthToken getAuthToken) {
    return new UploadToPlatformQueue(context, uploadNotification, videoApiClient, getAuthToken);
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
  GetAuthToken providesGetAuthToken() {
    return new GetAuthToken();
  }

}
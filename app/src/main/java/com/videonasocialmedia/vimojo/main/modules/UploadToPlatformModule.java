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
import com.videonasocialmedia.vimojo.repository.upload.UploadRealmRepository;
import com.videonasocialmedia.vimojo.repository.upload.UploadRepository;
import com.videonasocialmedia.vimojo.sync.presentation.UploadToPlatform;
import com.videonasocialmedia.vimojo.sync.presentation.ui.UploadNotification;
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
                                           GetAuthToken getAuthToken,
                                           UploadRepository uploadRepository) {
    return new UploadToPlatform(context, uploadNotification, videoApiClient, getAuthToken,
        uploadRepository);
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

  @Singleton @Provides
  UploadRepository providesUploadRepository() {
    return new UploadRealmRepository();
  }
}
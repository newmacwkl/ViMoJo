/*
 * Copyright (C) 2018 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.vimojo.sync;

/**
 * Created by alvaro on 6/2/18.
 */

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.moshi.Moshi;
import com.squareup.tape2.ObjectQueue;
import com.squareup.tape2.QueueFile;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.auth.domain.usecase.GetAuthToken;
import com.videonasocialmedia.vimojo.vimojoapiclient.AssetApiClient;
import com.videonasocialmedia.vimojo.vimojoapiclient.VimojoApiException;
import com.videonasocialmedia.vimojo.vimojoapiclient.model.AssetDto;
import com.videonasocialmedia.vimojo.vimojoapiclient.model.AssetUpload;
import com.videonasocialmedia.vimojo.vimojoapiclient.model.AuthToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class to unify video uploads to platform.
 * Create/init ObjectQueue, add objects and launchQueue.
 * FIFO, atomic ObjectQueue.
 */
public class AssetUploadQueue {
  private final String LOG_TAG = AssetUploadQueue.class.getCanonicalName();
  private final Context context;
  private final AssetApiClient assetApiClient;
  private final GetAuthToken getAuthToken;

  public AssetUploadQueue(Context context, AssetApiClient assetApiClient,
                          GetAuthToken getAuthToken) {
    this.context = context;
    this.assetApiClient = assetApiClient;
    this.getAuthToken = getAuthToken;
    Log.d(LOG_TAG, "Created sync queue...");
  }

  public ObjectQueue<AssetUpload> getQueue() {
    Log.d(LOG_TAG, "getting queue...");
    String uploadQUEUE = "QueueUploads_" + BuildConfig.FLAVOR;
    File file = new File(context.getFilesDir(), uploadQUEUE);
    ObjectQueue<AssetUpload> assetUploadObjectQueue = null;
    try {
      QueueFile queueFile = new QueueFile.Builder(file).build();
      Moshi moshi = new Moshi.Builder().build();
      MoshiConverter converter = new MoshiConverter(moshi, AssetUpload.class);
      assetUploadObjectQueue = ObjectQueue.create(queueFile, converter);
    } catch (IOException ioException) {
      ioException.printStackTrace();
      Log.d(LOG_TAG, ioException.getMessage());
      Crashlytics.log("Error creating queue video to upload");
      Crashlytics.logException(ioException);
    }
    Log.d(LOG_TAG, "...returned queue");
    return assetUploadObjectQueue;
  }

  public void addAssetToUpload(AssetUpload assetUpload) throws IOException {
    ObjectQueue<AssetUpload> queue = getQueue();
    queue.add(assetUpload);
  }

  public void processNextQueueItem() {
    Log.d(LOG_TAG, "processNextQueueItem");
    Log.d(LOG_TAG, "startNotification");
    AssetUpload element = getQueue().iterator().next();
    AuthToken authToken = getAuthToken.getAuthToken(context);
    try {
      String token = authToken.getToken();
      // TODO(jliarte): 27/02/18 check what to do with plaform response
      Log.d(LOG_TAG, "uploading video ... videoApiClient.addAsset");
      AssetDto assetDto = assetApiClient.addAsset(token, element);
      Log.d(LOG_TAG, "uploaded video ... videoApiClient.addAsset");
      removeHeadElement(getQueue());
      Log.d(LOG_TAG, "finish upload success");
    } catch (VimojoApiException vimojoApiException) {
      Log.d(LOG_TAG, "vimojoApiException " + vimojoApiException.getApiErrorCode());
      Crashlytics.log("Error process upload vimojoApiException");
      Crashlytics.logException(vimojoApiException);
      switch (vimojoApiException.getApiErrorCode()) {
        case VimojoApiException.UNAUTHORIZED:
          // TODO: 21/6/18 inform user
          Log.d(LOG_TAG, "VimojoApiException.UNAUTHORIZED");
          break;
        case VimojoApiException.NETWORK_ERROR:
          Log.d(LOG_TAG, "VimojoApiException.NETWORK_ERROR");
          // TODO: 21/6/18 inform user
          break;
        default:
          retryItemUpload(element, authToken.getId());
      }
    } catch (FileNotFoundException fileNotFoundError) {
      if (BuildConfig.DEBUG) {
        fileNotFoundError.printStackTrace();
      }
      Log.d(LOG_TAG, "File " + element.getMediaPath() + " trying to upload does not exists!");
      removeHeadElement(getQueue());
    }
  }


  protected void retryItemUpload(AssetUpload element, String userId) {
    incrementHeadNumTries(getQueue());
    if (element.getNumTries() > AssetUpload.MAX_NUM_TRIES_UPLOAD) {
      removeHeadElement(getQueue());
      Log.d(LOG_TAG, "finishNotification, error");
    }
  }

  private void incrementHeadNumTries(ObjectQueue<AssetUpload> queue) {
    try {
      queue.peek().incrementNumTries();
    } catch (IOException ioException) {
      Log.d(LOG_TAG, ioException.getMessage());
      Crashlytics.log("Error increment num tries head of queue video to upload");
      Crashlytics.logException(ioException);
    }
  }

  private void removeHeadElement(ObjectQueue<AssetUpload> queue) {
    if (queue.iterator().hasNext()) {
      try {
        queue.remove();
      } catch (IOException ioException) {
        Log.d(LOG_TAG, ioException.getMessage());
        Crashlytics.log("Error removing queue video to upload");
        Crashlytics.logException(ioException);
      }
    }
  }
}
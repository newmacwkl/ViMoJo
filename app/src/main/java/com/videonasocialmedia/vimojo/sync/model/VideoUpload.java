/*
 * Copyright (C) 2018 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.vimojo.sync.model;

/**
 * Model for enqueue video uploads to vimojo platform.
 * AuthToken, mediaPath, description info needed for video api service
 * NumTries, to manage a politic of maximum number of tries to upload a video.
 */
public class VideoUpload {

  private final int id;
  private String mediaPath;
  private String title;
  private String productTypeList;
  private String description;
  private int numTries;
  private boolean isAcceptedUploadMobileNetwork;
  public final static int MAX_NUM_TRIES_UPLOAD = 3;

  public VideoUpload(int id, String mediaPath, String title, String description,
                     String productTypeList, boolean isAcceptedUploadMobileNetwork) {
    this.id = id;
    this.mediaPath = mediaPath;
    this.title = title;
    this.description = description;
    this.productTypeList = productTypeList;
    this.numTries = 0;
    this.isAcceptedUploadMobileNetwork = isAcceptedUploadMobileNetwork;
  }

  public int getId() {
    return id;
  }

  public String getMediaPath() {
    return mediaPath;
  }

  public String getTitle() {
    return title;
  }

  public String getProductTypeList() {
    return productTypeList;
  }

  public String getDescription() {
    return description;
  }

  public int getNumTries() {
    return numTries;
  }

  public void incrementNumTries() {
    numTries++;
  }

  public boolean isAcceptedUploadMobileNetwork() {
    return isAcceptedUploadMobileNetwork;
  }
}

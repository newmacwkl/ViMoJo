/*
 * Copyright (C) 2018 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.vimojo.vimojoapiclient.model;

/**
 * Created by alvaro on 21/6/18.
 */

/**
 * Model class for project vimojo API calls.
 */
public class ProjectDto {
  private String id;
  private String title;
  private String date;

  public ProjectDto(String id, String title, String date) {
    this.id = id;
    this.title = title;
    this.date = date;
  }
}
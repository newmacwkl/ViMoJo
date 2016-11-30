package com.videonasocialmedia.vimojo.repository.project;

import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.vimojo.repository.video.RealmVideo;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by jliarte on 20/10/16.
 */
public class RealmProject extends RealmObject {
  @PrimaryKey
  public String title;
  public String projectPath;
  public String quality;
  public String resolution;
  public String frameRate;
  public String musicTitle;
  public float musicVolume = Music.DEFAULT_MUSIC_VOLUME;
  public RealmList<RealmVideo> videos;

  public RealmProject() {
    this.videos = new RealmList<RealmVideo>();
  }

  public RealmProject(String title, String projectPath, String quality, String resolution, String frameRate) {
    this.title = title;
    this.projectPath = projectPath;
    this.quality = quality;
    this.resolution = resolution;
    this.frameRate = frameRate;
    this.videos = new RealmList<RealmVideo>();
  }
}

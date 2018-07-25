package com.videonasocialmedia.vimojo.vimojoapiclient.model;

/**
 * Created by jliarte on 12/07/18.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Track vimojo API calls.
 */
public class TrackDto {
  public TrackDto() {
    this.mediaItems = new ArrayList<MediaDto>();
  }

  @SerializedName("_id") public String id;
  @SerializedName("uuid") public String uuid;
  @SerializedName("trackId") public int trackId;
  @SerializedName("volume") public float volume;
  @SerializedName("mute") public boolean mute;
  @SerializedName("position") public int position;
  @SerializedName("compositionId") public int compositionId;
  @SerializedName("medias") public List<MediaDto> mediaItems;

  public String getId() {
    return id;
  }

  public String getUuid() {
    return uuid;
  }

  public int getTrackId() {
    return trackId;
  }

  public float getVolume() {
    return volume;
  }

  public boolean isMute() {
    return mute;
  }

  public int getPosition() {
    return position;
  }

  public int getCompositionId() {
    return compositionId;
  }

  @Override
  public String toString() {
    return "TrackDto{"
            + "id='"
            + id
            + '\''
            + ", uuid='"
            + uuid
            + '\''
            + ", trackId='"
            + trackId
            + '\''
            + ", volume='"
            + volume
            + '\''
            + ", mute='"
            + mute
            + '\''
            + ", position='"
            + position
            + '\''
            + ", compositionId='"
            + compositionId
            + '\''
            + ", mediaItems='"
            + mediaItems
            +
            '}';
  }
}
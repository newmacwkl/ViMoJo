package com.videonasocialmedia.vimojo.repository.video;

import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.repository.Mapper;

/**
 * Created by jliarte on 22/10/16.
 */
public class VideoToRealmVideoMapper implements Mapper<Video,RealmVideo> {
  @Override
  public RealmVideo map(Video video) {
    RealmVideo realmVideo = new RealmVideo(video.getUuid(), video.getPosition(),
            video.getMediaPath(),video.getVolume(), video.getTempPath(), video.getClipText(),
        video.getClipTextPosition(), video.hasText(), video.isTrimmedVideo(),
        video.getStartTime(), video.getStopTime(), video.getVideoError(),
        video.isTranscodingTempFileFinished());
    return realmVideo;
  }
}
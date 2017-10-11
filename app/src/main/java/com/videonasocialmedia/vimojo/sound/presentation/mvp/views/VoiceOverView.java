package com.videonasocialmedia.vimojo.sound.presentation.mvp.views;

import com.videonasocialmedia.videonamediaframework.model.media.Video;

import java.util.List;

/**
 * Created by ruth on 15/09/16.
 */
public interface VoiceOverView {
    void initVoiceOverView(int startTime, int maxSeekBar);
    void bindVideoList(List<Video> movieList);
    void resetPreview();
    void playVideo();
    void pauseVideo();
    void navigateToSoundVolumeActivity(String voiceOverRecordedPath);
    void showError(String errorMessage);
    void setVideoFadeTransitionAmongVideos();
    void setAudioFadeTransitionAmongVideos();
    void resetVoiceOverRecorded();
    void cleanTempDirectoryPathVoiceOverRecorded(String folderDirectory);
    void disableRecordButton();
}

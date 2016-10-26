package com.videonasocialmedia.vimojo.export;

import com.videonasocialmedia.vimojo.export.domain.ExportSwapAudioToVideoUseCase;
import com.videonasocialmedia.vimojo.export.domain.OnExportEndedListener;
import com.videonasocialmedia.vimojo.export.domain.OnExportEndedSwapAudioListener;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.repository.video.VideoRealmRepository;
import com.videonasocialmedia.vimojo.repository.video.VideoRepository;
import com.videonasocialmedia.vimojo.sound.domain.GetAudioFadeInFadeOutFromVideoUseCase;
import com.videonasocialmedia.vimojo.sound.domain.OnGetAudioFadeInFadeOutFromVideoListener;

import java.io.IOException;

/**
 * Created by alvaro on 23/10/16.
 */

public class ApplyAudioFadeInFadeOutToVideo implements OnExportEndedSwapAudioListener,
    OnGetAudioFadeInFadeOutFromVideoListener {

  private GetAudioFadeInFadeOutFromVideoUseCase getAudioFadeInFadeOutFromVideoUseCase;
  private ExportSwapAudioToVideoUseCase exportSwapAudioToVideoUseCase;
  private Video videoToEdit;
  private int videoId;
  private OnApplyAudioFadeInFadeOutToVideoListener listener;

  public ApplyAudioFadeInFadeOutToVideo(OnApplyAudioFadeInFadeOutToVideoListener listener) {

    getAudioFadeInFadeOutFromVideoUseCase = new GetAudioFadeInFadeOutFromVideoUseCase(this);
    exportSwapAudioToVideoUseCase = new ExportSwapAudioToVideoUseCase(this);
    this.listener = listener;
  }

  public void applyAudioFadeToVideo(Video videoToEdit, int videoId, int timeFadeInMs,
                                    int timeFadeOutMs)
      throws IOException {

    this.videoToEdit = videoToEdit;
    this.videoId = videoId;
    getAudioFadeInFadeOutFromVideoUseCase.getAudioFadeInFadeOutFromVideo(videoToEdit,
        timeFadeInMs, timeFadeOutMs);
  }


  @Override
  public void onExportError(String error) {
    listener.OnGetAudioFadeInFadeOutError(error,videoToEdit,videoId);
  }

  @Override
  public void onExportSuccess() {
    listener.OnGetAudioFadeInFadeOutSuccess(videoToEdit, videoId);
  }

  @Override
  public void onGetAudioFadeInFadeOutFromVideoSuccess(String audioFile) {
    videoToEdit.setTempPath();
    exportSwapAudioToVideoUseCase.export(videoToEdit.getMediaPath(), audioFile,
        videoToEdit.getTempPath());
  }

  @Override
  public void onGetAudioFadeInFadeOutFromVideoError(String message) {
    listener.OnGetAudioFadeInFadeOutError(message, videoToEdit, videoId);

  }
}

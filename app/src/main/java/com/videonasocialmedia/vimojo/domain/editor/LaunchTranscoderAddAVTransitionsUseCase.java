package com.videonasocialmedia.vimojo.domain.editor;

import android.graphics.drawable.Drawable;

import com.videonasocialmedia.transcoder.MediaTranscoder;
import com.videonasocialmedia.transcoder.video.format.VideoTranscoderFormat;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.videonamediaframework.pipeline.TranscoderHelper;
import com.videonasocialmedia.videonamediaframework.pipeline.TranscoderHelperListener;
import com.videonasocialmedia.videonamediaframework.utils.TextToDrawable;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.repository.video.VideoRepository;

/**
 * Created by alvaro on 22/03/17.
 */

public class LaunchTranscoderAddAVTransitionsUseCase {
  protected TextToDrawable drawableGenerator = new TextToDrawable(VimojoApplication.getAppContext());
  protected MediaTranscoder mediaTranscoder = MediaTranscoder.getInstance();
  protected TranscoderHelper transcoderHelper = new TranscoderHelper(drawableGenerator,
      mediaTranscoder);
  protected VideoRepository videoRepository;

  private Project currentProject;

  public LaunchTranscoderAddAVTransitionsUseCase(VideoRepository videoRepository){
    this.currentProject = loadCurrentProject();
    this.videoRepository = videoRepository;
  }


  private Project loadCurrentProject() {
    return Project.getInstance(null, null, null);
  }

  public void launchExportTempFile(Drawable drawableFadeTransition, Video videoToEdit,
                                   VideoTranscoderFormat videoTranscoderFormat,
                                   String intermediatesTempAudioFadeDirectory,
                                   final TranscoderHelperListener
                                 transcoderHelperListener){
    boolean isVideoFadeTransitionActivated = currentProject.isVideoFadeTransitionActivated();
    boolean isAudioFadeTransitionActivated = currentProject.isAudioFadeTransitionActivated();
    updateGeneratedVideo(drawableFadeTransition, isVideoFadeTransitionActivated,
        isAudioFadeTransitionActivated, videoToEdit, videoTranscoderFormat,
        intermediatesTempAudioFadeDirectory, transcoderHelperListener);
  }

  private void updateGeneratedVideo(Drawable drawableFadeTransition,
                                    boolean isVideoFadeTransitionActivated,
                                    boolean isAudioFadeTransitionActivated,
                                    Video videoToEdit,
                                    VideoTranscoderFormat videoTranscoderFormat,
                                    String intermediatesTempAudioFadeDirectory,
                                    TranscoderHelperListener transcoderHelperListener) {

    if(isVideoFadeTransitionActivated) {
      transcoderHelper.generateOutputVideoWithAVTransitions(drawableFadeTransition,
          isVideoFadeTransitionActivated, isAudioFadeTransitionActivated, videoToEdit,
          videoTranscoderFormat, intermediatesTempAudioFadeDirectory, transcoderHelperListener);
    } else {
      if (isAudioFadeTransitionActivated) {
        transcoderHelper.generateOutputVideoWithAudioTransition(videoToEdit,
            intermediatesTempAudioFadeDirectory, transcoderHelperListener);
      }
    }
  }
}

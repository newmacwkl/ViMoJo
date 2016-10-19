package com.videonasocialmedia.vimojo.text.domain;

import android.support.annotation.NonNull;

import com.videonasocialmedia.transcoder.MediaTranscoder;
import com.videonasocialmedia.transcoder.MediaTranscoderListener;
import com.videonasocialmedia.transcoder.format.VideonaFormat;
import com.videonasocialmedia.transcoder.overlay.Image;
import com.videonasocialmedia.vimojo.export.utils.TranscoderHelper;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.text.presentation.views.activity.VideoEditTextActivity;
import com.videonasocialmedia.vimojo.text.util.TextToDrawable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import static org.mockito.Matchers.eq;

/**
 * Created by jliarte on 19/10/16.
 */
@RunWith(RobolectricTestRunner.class)
public class ModifyVideoTextAndPositionUseCaseTest {
  @Mock TextToDrawable mockedDrawableGenerator;
  @Mock MediaTranscoder mockedMediaTranscoder;
//  @Spy TranscoderHelper transcoderHelperSpy = new TranscoderHelper(mockedDrawableGenerator,
//          mockedMediaTranscoder);
  @InjectMocks ModifyVideoTextAndPositionUseCase injectedUseCase;
  private final MediaTranscoderListener mediaTranscoderListener = getMediaTranscoderListener();
  private final VideonaFormat videonaFormat = new VideonaFormat();

  @Before
  public void injectDoubles() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAddTextToVideoCallsTranscodeTrimAndOverlayImageToVideoIfVideoIsTrimmed()
          throws IOException {
    Video video = getVideoTrimmedWithText();
    // TODO(jliarte): 19/10/16 should not use a boolead here
    assert video.isTrimmedVideo();
    injectedUseCase.transcoderHelper = new TranscoderHelper(mockedDrawableGenerator,
            mockedMediaTranscoder);

    injectedUseCase.addTextToVideo(video, videonaFormat, video.getClipText(),
            video.getClipTextPosition(), mediaTranscoderListener);

    Mockito.verify(mockedMediaTranscoder).transcodeTrimAndOverlayImageToVideo(
            eq(video.getMediaPath()), eq(video.getTempPath()), eq(videonaFormat),
            eq(mediaTranscoderListener), Matchers.any(Image.class), eq(0), eq(10));
  }

  @Test
  public void testAddTextToVideoCallsGenerateOutputVideoWithOverlayImageAndTrimmingIfVideoIsTrimmed()
          throws IOException {
    Video video = getVideoTrimmedWithText();
    assert video.isTrimmedVideo();
    TranscoderHelper spy = Mockito.spy(new TranscoderHelper(mockedDrawableGenerator,
            mockedMediaTranscoder));
    injectedUseCase.transcoderHelper = spy;

    injectedUseCase.addTextToVideo(video, videonaFormat, video.getClipText(),
            video.getClipTextPosition(), mediaTranscoderListener);

    Mockito.verify(spy).generateOutputVideoWithOverlayImageAndTrimming(video,
            videonaFormat, mediaTranscoderListener);
  }

  @Test
  public void testAddTextToVideoCallsTranscodeAndOverlayImageToVideoIfVideoIsNotTrimmed()
          throws IOException {
    Video video = getVideoUntrimmedWithText();
    assert video.hasText();
    assert ! video.isTrimmedVideo();
    injectedUseCase.transcoderHelper = new TranscoderHelper(mockedDrawableGenerator,
            mockedMediaTranscoder);

    injectedUseCase.addTextToVideo(video, videonaFormat, video.getClipText(),
            video.getClipTextPosition(), mediaTranscoderListener);

    Mockito.verify(mockedMediaTranscoder).transcodeAndOverlayImageToVideo(eq(video.getMediaPath()),
            eq(video.getTempPath()), eq(videonaFormat), eq(mediaTranscoderListener),
            Matchers.any(Image.class));
  }

  @Test
  public void testAddTextToVideoCallsGenerateOutputVideoWithOverlayImageIfVideoIsNotTrimmed()
          throws IOException {
    Video video = getVideoUntrimmedWithText();
    assert video.hasText();
    assert ! video.isTrimmedVideo();
    TranscoderHelper spy = Mockito.spy(new TranscoderHelper(mockedDrawableGenerator,
            mockedMediaTranscoder));
    injectedUseCase.transcoderHelper = spy;

    injectedUseCase.addTextToVideo(video, videonaFormat, video.getClipText(),
            video.getClipTextPosition(), mediaTranscoderListener);

    Mockito.verify(spy).generateOutputVideoWithOverlayImage(video, videonaFormat,
            mediaTranscoderListener);
  }

  @NonNull
  private Video getVideoUntrimmedWithText() {
    Video video = new Video("media/path");
    video.setClipText("text");
    video.setClipTextPosition(VideoEditTextActivity.TextPosition.CENTER.name());
    video.setTextToVideoAdded(true);
    return video;
  }

  @NonNull
  private Video getVideoTrimmedWithText() {
    Video video = getVideoUntrimmedWithText();
    video.setStartTime(0);
    video.setStopTime(10);
    video.setTrimmedVideo(true);
    return video;
  }

  @NonNull
  private MediaTranscoderListener getMediaTranscoderListener() {
    return new MediaTranscoderListener() {
      @Override
      public void onTranscodeProgress(double v) {

      }

      @Override
      public void onTranscodeCompleted() {

      }

      @Override
      public void onTranscodeCanceled() {

      }

      @Override
      public void onTranscodeFailed(Exception e) {

      }
    };
  }
}
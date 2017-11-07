package com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters;

import android.content.Context;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.videonasocialmedia.videonamediaframework.model.Constants;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.videonamediaframework.model.media.Profile;
import com.videonasocialmedia.videonamediaframework.model.media.exceptions.IllegalItemOnTrack;
import com.videonasocialmedia.videonamediaframework.model.media.track.AudioTrack;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoFrameRate;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoQuality;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoResolution;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnAddMediaFinishedListener;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnRemoveMediaFinishedListener;
import com.videonasocialmedia.vimojo.settings.mainSettings.domain.GetPreferencesTransitionFromProjectUseCase;
import com.videonasocialmedia.vimojo.sound.domain.AddAudioUseCase;
import com.videonasocialmedia.vimojo.sound.domain.RemoveAudioUseCase;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.views.VoiceOverRecordView;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by alvaro on 9/10/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class VoiceOverRecordPresenterTest {

  @InjectMocks VoiceOverRecordPresenter injectedPresenter;

  @Mock Context mockedContext;
  @Mock VoiceOverRecordView mockedVoiceOverRecordView;
  @Mock GetMediaListFromProjectUseCase mockedGetMediaListFromProjectUseCase;
  @Mock GetPreferencesTransitionFromProjectUseCase mockedGetPreferencesTransitionFromProjectUseCase;
  @Mock Log mockedLog;
  @Mock File mockedFile;
  @Mock AddAudioUseCase mockedAddAudioUseCase;
  @Mock RemoveAudioUseCase mockedRemoveAudioUseCase;
  @Mock UserEventTracker mockedUserEventTracker;
  @Mock ListenableFuture<Void> mockedListenableFuture;

  @Before
  public void injectTestDoubles() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void setVoiceOverShowErrorIfThereAreNotAudiosRecorded() throws IOException {

    injectedPresenter.setVoiceOver("somePath");

    verify(mockedVoiceOverRecordView).showError(null);
    assertThat(injectedPresenter.isVoiceOverRecorded(), is(false));
  }

  @Test
  public void setVoiceOverCallsApplyVoiceOverIfThereIsAudioRecorded() {

    VoiceOverRecordPresenter voiceOverRecordPresenter =
            Mockito.spy(getVoiceOverRecorderPresenter());
    when(voiceOverRecordPresenter.isVoiceOverRecorded()).thenReturn(true);

    voiceOverRecordPresenter.setVoiceOver("somePath");

    verify(voiceOverRecordPresenter).applyVoiceOver(anyString());
    verify(mockedVoiceOverRecordView).showProgressDialog();
  }

  @Test
  public void addVoiceOverCallsTrackingAndNavigateOnAddMediaItemToTrackSuccess() {
    Project project = getAProject();
    final float defaultVolume = 0.5f;
    int defaultDuration = 100;
    String mediaPath = "somePath";
    final Music voiceOver = new Music(mediaPath, defaultVolume, defaultDuration);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        OnAddMediaFinishedListener listener = invocation.getArgument(2);
        listener.onAddMediaItemToTrackSuccess(voiceOver);
        return null;
      }
    }).when(mockedAddAudioUseCase).addMusic(eq(voiceOver),
            eq(Constants.INDEX_AUDIO_TRACK_VOICE_OVER),
            any(OnAddMediaFinishedListener.class));

    injectedPresenter.addVoiceOver(voiceOver);

    verify(mockedVoiceOverRecordView).navigateToVoiceOverVolumeActivity(mediaPath);
    verify(mockedUserEventTracker).trackVoiceOverSet(project);
  }

  @Test
  public void addVoiceOverCallsShowErrorOnAddMediaItemToTrackError() {
    final float defaultVolume = 0.5f;
    int defaultDuration = 100;
    final Music voiceOver = new Music("somePath", defaultVolume, defaultDuration);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        OnAddMediaFinishedListener listener = invocation.getArgument(2);
        listener.onAddMediaItemToTrackError();
        return null;
      }
    }).when(mockedAddAudioUseCase).addMusic(eq(voiceOver),
            eq(Constants.INDEX_AUDIO_TRACK_VOICE_OVER),
            any(OnAddMediaFinishedListener.class));

    injectedPresenter.addVoiceOver(voiceOver);

    verify(mockedVoiceOverRecordView).showError(null);
  }

  @Test
  public void removePreviousVoiceOverCallsShowErrorOnRemoveMediaItemFromTrackError()
          throws IllegalItemOnTrack {
    Project project = getAProject();
    final float defaultVolume = 0.5f;
    int defaultDuration = 100;
    final Music voiceOver = new Music("somePath", defaultVolume, defaultDuration);
    project.getAudioTracks().add(new AudioTrack(Constants.INDEX_AUDIO_TRACK_VOICE_OVER));
    project.getAudioTracks().get(Constants.INDEX_AUDIO_TRACK_VOICE_OVER).insertItem(voiceOver);
    assertThat("Project has voice over", project.hasVoiceOver(), is(true));
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        OnRemoveMediaFinishedListener listener = invocation.getArgument(2);
        listener.onRemoveMediaItemFromTrackError();
        return null;
      }
    }).when(mockedRemoveAudioUseCase).removeMusic(eq(voiceOver),
            eq(Constants.INDEX_AUDIO_TRACK_VOICE_OVER),
            Matchers.any(OnRemoveMediaFinishedListener.class));

    injectedPresenter.deletePreviousVoiceOver();

    verify(mockedVoiceOverRecordView).showError(null);
  }

  @Test
  public void getVoiceOverAsMusicCreateVoiceOverObject() {
    Music voiceOver = injectedPresenter.getVoiceOverAsMusic("media/path");

    assertThat("Voice over has correct title", voiceOver.getMusicTitle(),
            is(com.videonasocialmedia.vimojo.utils.Constants.MUSIC_AUDIO_VOICEOVER_TITLE));
    assertThat("Voice over has icon resource", voiceOver.getIconResourceId(),
            is(R.drawable.activity_edit_audio_voice_over_icon));
    assertThat("Voice over volume is default", voiceOver.getVolume(),
            is(Music.DEFAULT_VOLUME));
  }

  public Project getAProject() {
    return Project.getInstance("title", "/path", "private/path",
        Profile.getInstance(VideoResolution.Resolution.HD720,
            VideoQuality.Quality.HIGH, VideoFrameRate.FrameRate.FPS25));
  }

  private VoiceOverRecordPresenter getVoiceOverRecorderPresenter(){
    return new VoiceOverRecordPresenter(mockedContext, mockedVoiceOverRecordView,
            mockedGetMediaListFromProjectUseCase, mockedGetPreferencesTransitionFromProjectUseCase,
            mockedAddAudioUseCase, mockedRemoveAudioUseCase,
            mockedUserEventTracker);
  }
}
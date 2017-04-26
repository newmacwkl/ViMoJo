package com.videonasocialmedia.vimojo.presentation.mvp.presenters;

import com.google.common.util.concurrent.ListenableFuture;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.videonamediaframework.model.media.exceptions.IllegalItemOnTrack;
import com.videonasocialmedia.videonamediaframework.model.media.track.MediaTrack;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.GetMusicFromProjectUseCase;
import com.videonasocialmedia.videonamediaframework.model.media.Profile;
import com.videonasocialmedia.vimojo.domain.editor.RemoveVideoFromProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.ReorderMediaItemUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;

import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoFrameRate;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoQuality;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoResolution;
import com.videonasocialmedia.vimojo.presentation.mvp.views.EditActivityView;
import com.videonasocialmedia.videonamediaframework.playback.VideonaPlayer;
import com.videonasocialmedia.vimojo.settings.domain.GetPreferencesTransitionFromProjectUseCase;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters.SoundPresenter;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by jliarte on 31/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class EditPresenterTest {

  @Mock private GetMediaListFromProjectUseCase mockedGetMediaListFromProjectUseCase;
  @Mock private EditActivityView mockedEditorView;
  @Mock private VideonaPlayer mockedVideonaPlayer;
  @Mock private MixpanelAPI mockedMixpanelApi;
  @Mock private UserEventTracker mockedUserEventTracker;
  @Mock private RemoveVideoFromProjectUseCase mockedVideoRemover;
  @Mock private ReorderMediaItemUseCase mockedMediaItemReorderer;
  @Mock private GetPreferencesTransitionFromProjectUseCase mockedGetPreferencesTransitionsFromProject;
  @Mock private GetMusicFromProjectUseCase mockedGetMusicFromProjectUseCase;
  @Mock ListenableFuture<Void> mockedTranscodingTask;

  @InjectMocks private EditPresenter injectedEditPresenter;

  @Before
  public void injectTestDoubles() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void clearProjectInstance() {
    Project.INSTANCE.clear();
  }

  @Test
  public void constructorSetsUserTracker() {
    assertThat(injectedEditPresenter.userEventTracker, is(mockedUserEventTracker));
  }

  @Test
  public void loadProjectCallsGetMediaListFromProjectUseCase() {
    injectedEditPresenter.obtainVideos();
    verify(mockedGetMediaListFromProjectUseCase).getMediaListFromProject(injectedEditPresenter);
  }

    @Test
  public void trackClipsReorderedIsCalledOnMediaReordered() {
    Project videonaProject = getAProject();
    injectedEditPresenter.onMediaReordered(null, 2);
    verify(mockedUserEventTracker).trackClipsReordered(videonaProject);
  }

  @Test
  public void loadProjectCallsEditorViewSetMusicIfProjectHasVoiceOver()
          throws IllegalItemOnTrack {
    Project project = getAProject();
    String musicPath = "voice/over/path";
    float musicVolume = 0.6f;
    Music voiceOver = new Music(musicPath, musicVolume, 0);
    project.getVMComposition().getAudioTracks().get(0).insertItem(voiceOver);
    GetMusicFromProjectUseCase getMusicFromProjectUseCase = new GetMusicFromProjectUseCase();
    EditPresenter presenter = new EditPresenter(mockedEditorView, mockedUserEventTracker,
        mockedVideoRemover, mockedMediaItemReorderer, getMusicFromProjectUseCase,
        mockedGetMediaListFromProjectUseCase,mockedGetPreferencesTransitionsFromProject);

    presenter.init();

    verify(mockedEditorView).setMusic(voiceOver);
  }

  @Test
  public void ifProjectHasSomeVideoWithErrorsCallsShowWarningTempFile() throws IllegalItemOnTrack {
    Project project = getAProject();
    Video video1 = new Video("video/path");
    Video video2 = new Video("video/path");
    video2.setVideoError(Constants.ERROR_TRANSCODING_TEMP_FILE_TYPE.TRIM.name());

    assertThat("video1 has not error", video1.getVideoError() == null, is(true));
    assertThat("video2 has error", video2.getVideoError() == null, is(false));


    video1.setTranscodingTask(mockedTranscodingTask);
    video2.setTranscodingTask(mockedTranscodingTask);

    when(mockedTranscodingTask.isCancelled()).thenReturn(true);

    List<Video> videoList = new ArrayList<>();
    videoList.add(video1);
    videoList.add(video2);
    MediaTrack mediaTrack = project.getMediaTrack();
    mediaTrack.insertItem(video1);

    EditPresenter presenter = new EditPresenter(mockedEditorView, mockedUserEventTracker,
        mockedVideoRemover, mockedMediaItemReorderer, mockedGetMusicFromProjectUseCase,
        mockedGetMediaListFromProjectUseCase,mockedGetPreferencesTransitionsFromProject);

    presenter.checkWarningMessageVideosRetrieved(videoList);

    verify(mockedEditorView).showWarningTempFile();

  }

  public Project getAProject() {
    Profile profile = new Profile(VideoResolution.Resolution.HD720, VideoQuality.Quality.HIGH,
            VideoFrameRate.FrameRate.FPS25);
    return Project.getInstance("title", "/path", profile);
  }
}

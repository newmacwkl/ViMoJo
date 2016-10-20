package com.videonasocialmedia.vimojo.presentation.mvp.presenters;

import android.support.annotation.NonNull;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.GetMusicFromProjectUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Profile;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;

import com.videonasocialmedia.vimojo.model.entities.editor.utils.VideoFrameRate;
import com.videonasocialmedia.vimojo.model.entities.editor.utils.VideoQuality;
import com.videonasocialmedia.vimojo.model.entities.editor.utils.VideoResolution;
import com.videonasocialmedia.vimojo.presentation.mvp.views.EditorView;
import com.videonasocialmedia.vimojo.presentation.mvp.views.VideonaPlayerView;
import com.videonasocialmedia.vimojo.presentation.views.customviews.ToolbarNavigator;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by jliarte on 31/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class EditPresenterTest {

    @Mock GetMusicFromProjectUseCase getMusicFromProjectUseCase;
    @Mock GetMediaListFromProjectUseCase getMediaListFromProjectUseCase;
    @InjectMocks private EditPresenter editPresenter;
    @Mock private EditorView mockedEditorView;
    @Mock private VideonaPlayerView mockedVideonaPlayerView;
    @Mock private MixpanelAPI mockedMixpanelApi;
    @Mock private UserEventTracker mockedUserEventTracker;
    @Mock private ToolbarNavigator.ProjectModifiedCallBack mockedProjectModifiedCallback;

    @Before
    public void injectTestDoubles() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void constructorSetsUserTracker() {
        assertThat(editPresenter.userEventTracker, is(mockedUserEventTracker));
    }

    @Test
    public void constructorSetsCurrentProject() {
        Project videonaProject = getAProject();
        assertThat(editPresenter.currentProject, is(videonaProject));
    }

    @Test
    public void loadProjectCallsGetMediaListFromProjectUseCase() {
        editPresenter.loadProject();
        Mockito.verify(getMediaListFromProjectUseCase).getMediaListFromProject(editPresenter);
    }

    @Test
    public void loadProjectCallsGetMusicFromProjectUseCaseIfProjectHasMusic() {
        Project videonaProject = getAProject();
        // TODO:(alvaro.martinez) 10/10/16 Check and improve isMusicOnProject, setter not needed.
    }

    @Test
    public void trackClipsReorderedIsCalledOnMediaReordered() {
        Project videonaProject = Project.getInstance("title", "/path", Profile.getInstance(null, null, null));

        editPresenter.onMediaReordered(null, 2);

        Mockito.verify(mockedUserEventTracker).trackClipsReordered(videonaProject);
    }

    // Seems not needed since we already use @InjectMocks annotation
    @NonNull
    public EditPresenter getEditPresenter() {
        return new EditPresenter(mockedEditorView, mockedProjectModifiedCallback, mockedUserEventTracker);
    }

    public Project getAProject() {
        return Project.getInstance("title", "/path", Profile.getInstance(VideoResolution.Resolution.HD720,
                VideoQuality.Quality.HIGH, VideoFrameRate.FrameRate.FPS25));
    }
}

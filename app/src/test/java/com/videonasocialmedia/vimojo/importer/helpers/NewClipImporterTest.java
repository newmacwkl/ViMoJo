package com.videonasocialmedia.vimojo.importer.helpers;

import android.os.Environment;
import android.util.Log;

import com.videonasocialmedia.transcoder.video.format.VideonaFormat;
import com.videonasocialmedia.videonamediaframework.model.media.Profile;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.videonamediaframework.model.media.exceptions.IllegalItemOnTrack;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoFrameRate;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoQuality;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoResolution;
import com.videonasocialmedia.vimojo.domain.editor.ApplyAVTransitionsUseCase;
import com.videonasocialmedia.vimojo.export.domain.GetVideoFormatFromCurrentProjectUseCase;
import com.videonasocialmedia.vimojo.export.domain.RelaunchTranscoderTempBackgroundUseCase;
import com.videonasocialmedia.vimojo.importer.model.entities.VideoToAdapt;
import com.videonasocialmedia.vimojo.importer.repository.VideoToAdaptRealmRepository;
import com.videonasocialmedia.vimojo.importer.repository.VideoToAdaptRepository;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.model.entities.editor.ProjectInfo;
import com.videonasocialmedia.vimojo.record.domain.AdaptVideoToFormatUseCase;
import com.videonasocialmedia.vimojo.repository.project.ProjectRepository;
import com.videonasocialmedia.vimojo.repository.video.VideoRealmRepository;
import com.videonasocialmedia.vimojo.repository.video.VideoRepository;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Alvaro on 03/10/2017.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, Environment.class})
public class NewClipImporterTest {


    @Mock AdaptVideoToFormatUseCase mockedAdaptVideoToFormatUseCase;
    @Mock VideoToAdapt mockedVideoToAdapt;
    @Mock VideonaFormat mockedVideonaFormat;
    @Mock AdaptVideoToFormatUseCase.AdaptListener mockedAdaptListener;
    private File mockedStorageDir;
    @Mock GetVideoFormatFromCurrentProjectUseCase mockedGetVideoFormatFromCurrentProjectUseCase;
    @Mock VideoToAdaptRepository mockedVideoToAdaptRepository;
    @Mock ApplyAVTransitionsUseCase mockedLaunchTranscoderAddAVTransitionUseCase;
    @Mock RelaunchTranscoderTempBackgroundUseCase mockedRelaunchTranscoderTempBackgroundUseCase;
    @Mock VideoRepository mockedVideoRepository;
    @Mock ProjectRepository mockedProjectRepository;
    private Project currentProject;

    @Before
    public void injectMocks() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Log.class);
        PowerMockito.mockStatic(Environment.class);
        mockedStorageDir = PowerMockito.mock(File.class);
        when(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)).
                thenReturn(mockedStorageDir);
        when(Environment.getExternalStorageDirectory()).thenReturn(mockedStorageDir);
        getAProject();
        when(mockedProjectRepository.getCurrentProject()).thenReturn(currentProject);
    }

    @Test
    public void adaptVideoToVideonaFormatCallsAdaptVideoToFormatUseCase() throws IOException {
        Video video = new Video(".temporal/Vid1234.mp4", Video.DEFAULT_VOLUME);
        int position = 0;
        int cameraRotation =0;
        int retries = 0;
        VideonaFormat videonaFormat = currentProject.getVMComposition().getVideoFormat();
        when(mockedGetVideoFormatFromCurrentProjectUseCase
                .getVideonaFormatToAdaptVideoRecordedAudioAndVideo(currentProject)).thenReturn(videonaFormat);
        int videoPosition = 0;
        String destVideoPath = "DCIM/ViMoJo/Masters";
        VideoToAdapt videoToAdapt = new VideoToAdapt(video, destVideoPath, position, cameraRotation,
            retries);
        mockedVideoToAdaptRepository.add(videoToAdapt);
        List<VideoToAdapt> videosToAdapt = mockedVideoToAdaptRepository.getAllVideos();
        videosToAdapt.add(videoToAdapt);
        NewClipImporter spyNewClipImporter = Mockito.spy(getInjectedNewClipImporter());
        spyNewClipImporter.videoToAdaptRepository = mockedVideoToAdaptRepository;
        when(mockedVideoToAdaptRepository.getAllVideos()).thenReturn(videosToAdapt);
        when(spyNewClipImporter.getVideoToAdapt(video, videoPosition, cameraRotation, retries,
            "destVideoRecoded")).thenReturn(videoToAdapt);
        //spyNewClipImporter.videoToAdapt = videoToAdapt;

        spyNewClipImporter.adaptVideoToVideonaFormat(currentProject, video, position,
                cameraRotation, retries);

        Mockito.verify(mockedAdaptVideoToFormatUseCase).adaptVideo(eq(currentProject),
            any(VideoToAdapt.class), eq(videonaFormat), any(AdaptVideoToFormatUseCase.AdaptListener.class));
    }

    @Test
    public void relaunchUnfinishedAdaptTasksCallsNewClipImporterAdaptVideoToVideonaFormat()
            throws IllegalItemOnTrack, IOException {
        Video video = new Video(".temp/path", Video.DEFAULT_VOLUME);
        currentProject.getVMComposition().getMediaTrack().insertItem(video);
        String destVideoPath = "DCIM/ViMoJo/Masters";
        int position = 0;
        int cameraRotation = 0;
        int retries = 0;
        VideoToAdapt videoToAdapt = new VideoToAdapt(video, destVideoPath, position, cameraRotation,
                retries);
        mockedVideoToAdaptRepository.add(videoToAdapt);
        List<VideoToAdapt> videosToAdapt = mockedVideoToAdaptRepository.getAllVideos();
        videosToAdapt.add(videoToAdapt);
        NewClipImporter newClipImporterSpy = Mockito.spy(getInjectedNewClipImporter());
        newClipImporterSpy.videoToAdaptRepository = mockedVideoToAdaptRepository;
        when(mockedVideoToAdaptRepository.getAllVideos()).thenReturn(videosToAdapt);

        newClipImporterSpy.relaunchUnfinishedAdaptTasks(currentProject);

        Mockito.verify(newClipImporterSpy).adaptVideoToVideonaFormat(currentProject, video, position,
                cameraRotation,
                ++retries);
        assertThat(videosToAdapt.get(0).getVideo().getIdentifier(), is(video.getIdentifier()));
    }

    private NewClipImporter getInjectedNewClipImporter() {
        return new NewClipImporter(mockedGetVideoFormatFromCurrentProjectUseCase,
            mockedAdaptVideoToFormatUseCase, mockedLaunchTranscoderAddAVTransitionUseCase,
            mockedRelaunchTranscoderTempBackgroundUseCase, mockedProjectRepository,
            mockedVideoRepository, mockedVideoToAdaptRepository);
    }

    private void getAProject() {
        Profile compositionProfile = new Profile(VideoResolution.Resolution.HD720,
                VideoQuality.Quality.HIGH, VideoFrameRate.FrameRate.FPS25);
        List<String> productType = new ArrayList<>();
        ProjectInfo projectInfo = new ProjectInfo("title", "description", productType);
        currentProject = new Project(projectInfo, "/path", "private/path", compositionProfile);
    }
}

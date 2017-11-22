package com.videonasocialmedia.vimojo.domain;


import com.videonasocialmedia.videonamediaframework.model.media.Profile;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.cameraSettings.model.CameraSettings;
import com.videonasocialmedia.vimojo.cameraSettings.model.FrameRateSetting;
import com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting;
import com.videonasocialmedia.vimojo.cameraSettings.repository.CameraSettingsRepository;
import com.videonasocialmedia.vimojo.domain.project.CreateDefaultProjectUseCase;
import com.videonasocialmedia.vimojo.main.VimojoTestApplication;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoFrameRate;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoQuality;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoResolution;
import com.videonasocialmedia.vimojo.repository.project.ProjectRepository;
import com.videonasocialmedia.vimojo.test.shadows.ShadowMultiDex;
import com.videonasocialmedia.vimojo.utils.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by jliarte on 23/10/16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = VimojoTestApplication.class, constants = BuildConfig.class, sdk = 21,
        shadows = {ShadowMultiDex.class}, packageName = "com.videonasocialmedia.vimojo.debug")
public class CreateDefaultProjectUseCaseTest {

  @Mock ProjectRepository mockedProjectRepository;
  @Mock VimojoApplication mockedVimojoApplication;
  @Mock
  CameraSettingsRepository mockedCameraSettingsRepository;
  @InjectMocks CreateDefaultProjectUseCase injectedUseCase;
  private CameraSettings cameraSettings;

  @Before
  public void injectDoubles() {
    MockitoAnnotations.initMocks(this);
    initCameraPreferences();
    when(mockedCameraSettingsRepository.getCameraPreferences()).thenReturn(cameraSettings);
  }

  @Before
  public void setupProjectInstance() {
    if (Project.INSTANCE != null) {
      Project.INSTANCE.clear();
    }
  }

  private void initCameraPreferences() {
    String defaultResolution = Constants.DEFAULT_CAMERA_PREF_RESOLUTION;
    boolean resolutionBack720pSupported = true;
    boolean resolutionBack1080pSupported = true;
    boolean resolutionBack2160pSupported = false;
    boolean resolutionFront720pSupported = true;
    boolean resolutionFront1080pSupported = true;
    boolean resolutionFront2160pSupported = false;
    ResolutionSetting resolutionSetting = new ResolutionSetting(defaultResolution,
        resolutionBack720pSupported, resolutionBack1080pSupported,
        resolutionBack2160pSupported, resolutionFront720pSupported,
        resolutionFront1080pSupported, resolutionFront2160pSupported);
    String defaultFrameRate = Constants.DEFAULT_CAMERA_PREF_FRAME_RATE;
    boolean frameRate24FpsSupported = false;
    boolean frameRate25FpsSupported = false;
    boolean frameRate30FpsSupported = true;
    FrameRateSetting frameRateSetting = new FrameRateSetting(defaultFrameRate,
        frameRate24FpsSupported, frameRate25FpsSupported, frameRate30FpsSupported);
    String quality = Constants.DEFAULT_CAMERA_PREF_QUALITY;
    boolean interfaceProSelected = false;
    cameraSettings = new CameraSettings(resolutionSetting,
            frameRateSetting, quality, interfaceProSelected);
  }

//  @Test
//  public void loadOrCreateProjectCallsGetCurrentProjectIfInstanceIsNull() {
//    Profile profile = new Profile(VideoResolution.Resolution.HD720, VideoQuality.Quality.EXCELLENT,
//            -1, Profile.ProfileType.pro);
//    Project currentProject = new Project("title", "root/path", profile);
//    assert Project.INSTANCE == null;
//    doReturn(currentProject).when(mockedProjectRepository).getCurrentProject();
//
//    injectedUseCase.loadOrCreateProject("root/path");
//
//    verify(mockedProjectRepository).getCurrentProject();
//    assertThat(Project.INSTANCE, is(currentProject));
//    verify(mockedProjectRepository).update(currentProject);
//  }

  @Test
  public void loadOrCreateProjectCallsGetCurrentProjectIfInstanceIsNull() {
    assert Project.INSTANCE == null;
    Profile profile = new Profile(VideoResolution.Resolution.HD720, VideoQuality.Quality.HIGH,
        VideoFrameRate.FrameRate.FPS25);
    injectedUseCase.loadOrCreateProject("root/path", "private/path", false);

    verify(mockedProjectRepository).getCurrentProject();
  }

  @Test
  public void startLoadingProjectDoesNotCallGetCurrentProjectIfNonNullInstance() {
    Project project = Project.INSTANCE = new Project(null, null, null, null);

    injectedUseCase.loadOrCreateProject("root/path", "private/path", false);

    verify(mockedProjectRepository, never()).getCurrentProject();
    assertThat(Project.getInstance(null, null, null, null), is(project));
  }

  @Test
  public void startLoadingProjectSetsProjectInstanceToCurrentProjectRetrieved() {
    assert Project.INSTANCE == null;
    Profile profile = new Profile(VideoResolution.Resolution.HD720, VideoQuality.Quality.HIGH,
            VideoFrameRate.FrameRate.FPS25);
    Project currentProject = new Project("current project title", "current/path", "private/path",
            profile);
    doReturn(currentProject).when(mockedProjectRepository).getCurrentProject();

    injectedUseCase.loadOrCreateProject("root/path", "private/path", false);

    assertThat(Project.getInstance(null, null, null, null), is(currentProject));
  }

  @Test
  public void loadOrCreateUpdatesProjectRepository() {
    injectedUseCase.loadOrCreateProject("root/path", "private/path", false);

    Project actualProject = Project.getInstance(null, null, null, null);

    verify(mockedProjectRepository).update(actualProject);
  }

  @Test
  public void createProjectUpdatesProjectRepository() {
    injectedUseCase.createProject("root/path", "private/path", false);

    Project actualProject = Project.getInstance(null, null, null, null);

    verify(mockedProjectRepository).update(actualProject);
  }

  @Test
  public void createProjectUpdatesProjectInstance() {
    assert Project.INSTANCE == null;
    injectedUseCase.createProject("root/path", "private/path", false);

    Project actualProject = Project.getInstance(null, null, null, null);
  }

  @Test
  public void createProjectActivatesWatermarkIfIsFeatured() {
    boolean isWatermarkFeatured = true;

    injectedUseCase.createProject("root/path", "private/path", isWatermarkFeatured);

    assertThat("Watermark is activated", Project.getInstance(null, null, null, null)
            .getVMComposition().hasWatermark(), is(true));
  }

  @Test
  public void loadOrCreateProjectActivatesWatermarkIfIsFeatured() {
    boolean isWatermarkFeatured = true;

    injectedUseCase.loadOrCreateProject("root/path", "private/path", isWatermarkFeatured);

    assertThat("Watermark is activated", Project.getInstance(null, null, null, null)
            .getVMComposition().hasWatermark(), is(true));
  }
}

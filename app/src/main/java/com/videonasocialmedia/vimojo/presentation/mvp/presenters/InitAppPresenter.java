package com.videonasocialmedia.vimojo.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import com.videonasocialmedia.camera.utils.Camera2Settings;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.cameraSettings.model.CameraSettings;
import com.videonasocialmedia.vimojo.cameraSettings.model.FrameRateSetting;
import com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting;
import com.videonasocialmedia.vimojo.cameraSettings.repository.CameraSettingsRepository;
import com.videonasocialmedia.vimojo.domain.project.CreateDefaultProjectUseCase;
import com.videonasocialmedia.vimojo.main.ProjectInstanceCache;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.presentation.mvp.views.InitAppView;
import com.videonasocialmedia.vimojo.repository.project.ProjectRepository;
import com.videonasocialmedia.vimojo.sync.helper.RunSyncAdapterHelper;
import com.videonasocialmedia.vimojo.utils.ConfigPreferences;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.FileUtils;
import com.videonasocialmedia.vimojo.view.VimojoPresenter;

import java.util.HashMap;

import javax.inject.Inject;

import static com.videonasocialmedia.vimojo.cameraSettings.model.FrameRateSetting.CAMERA_SETTING_FRAME_RATE_24_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.FrameRateSetting.CAMERA_SETTING_FRAME_RATE_25_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.FrameRateSetting.CAMERA_SETTING_FRAME_RATE_30_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_1080_BACK_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_1080_FRONT_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_2160_BACK_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_2160_FRONT_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_720_BACK_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_720_FRONT_ID;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_H_720;
import static com.videonasocialmedia.vimojo.cameraSettings.model.ResolutionSetting.CAMERA_SETTING_RESOLUTION_V_720;
import static com.videonasocialmedia.vimojo.utils.Constants.BACK_CAMERA_ID;
import static com.videonasocialmedia.vimojo.utils.Constants.DEFAULT_WATERMARK_STATE;
import static com.videonasocialmedia.vimojo.utils.Constants.FRONT_CAMERA_ID;

ashlytics.android.Crashlytics;

/**
 * Created by jliarte on 22/10/16.
 */
public class InitAppPresenter extends VimojoPresenter {
  private final Context context;
  private final InitAppView initAppView;
  private final CameraSettingsRepository cameraSettingsRepository;
  private final ProjectInstanceCache projectInstanceCache;
  private final ProjectRepository projectRepository;
  private RunSyncAdapterHelper runSyncAdapterHelper;
  private CreateDefaultProjectUseCase createDefaultProjectUseCase;
  private SharedPreferences sharedPreferences;
  private CameraSettings cameraSettings;
  private String LOG_TAG = InitAppPresenter.class.getCanonicalName();


  @Inject
  public InitAppPresenter(Context context, InitAppView initAppView,
                          SharedPreferences sharedPreferences,
                          CreateDefaultProjectUseCase createDefaultProjectUseCase,
                          CameraSettingsRepository cameraSettingsRepository,
                          RunSyncAdapterHelper runSyncAdapterHelper,
                          ProjectRepository projectRepository,
                          ProjectInstanceCache projectInstanceCache) {
    this.context = context;
    this.initAppView = initAppView;
    this.sharedPreferences = sharedPreferences;
    this.createDefaultProjectUseCase = createDefaultProjectUseCase;
    this.cameraSettingsRepository = cameraSettingsRepository;
    this.runSyncAdapterHelper = runSyncAdapterHelper;
    this.projectRepository = projectRepository;
    this.projectInstanceCache = projectInstanceCache;
  }

  public void onAppPathsCheckSuccess(String rootPath, String privatePath,
                                     Drawable drawableFadeTransitionVideo) {
    if (projectInstanceCache.getCurrentProject() == null) {
      // TODO(jliarte): 23/04/18 in fact, there will be always a project instance, consider removing
      Project project = createDefaultProjectUseCase.createProject(rootPath, privatePath,
              isWatermarkActivated(), drawableFadeTransitionVideo,
              BuildConfig.FEATURE_VERTICAL_VIDEOS);
      projectRepository.add(project);
      projectInstanceCache.setCurrentProject(project);
    }
  }

  public boolean isWatermarkActivated() {
    return BuildConfig.FEATURE_FORCE_WATERMARK
        || sharedPreferences.getBoolean(ConfigPreferences.WATERMARK, DEFAULT_WATERMARK_STATE);
  }

  public void checkCamera2FrameRateAndResolutionSupported() {
    Camera2Settings camera2Settings = null;
    try {
      camera2Settings = new Camera2Settings(context);
      checkCamera2ResolutionSupported(camera2Settings);
      checkCamera2FrameRateSupported(camera2Settings.getFPSRange(BACK_CAMERA_ID));
    } catch (CameraAccessException e) {
      e.printStackTrace();
      Log.d(LOG_TAG, "CameraAccessException " + e.getMessage());
      // TODO: 15/11/2017 Manage Error ¿?
      return;
    }

  }

  private void checkCamera2ResolutionSupported(Camera2Settings camera2Settings)
          throws CameraAccessException {

    String defaultResolution = Constants.DEFAULT_CAMERA_SETTING_RESOLUTION;
    HashMap<Integer, Boolean> resolutionsSupportedMap = new HashMap<>();

    boolean resolutionBack720pSupported = false;
    boolean resolutionBack1080pSupported = false;
    boolean resolutionBack2160pSupported = false;
    boolean resolutionFront720pSupported = false;
    boolean resolutionFront1080pSupported = false;
    boolean resolutionFront2160pSupported = false;

    for(Size sizeBackCamera: camera2Settings.getSupportedVideoSizes(BACK_CAMERA_ID)){
      if(sizeBackCamera.getWidth() == 1280 && sizeBackCamera.getHeight() == 720){
        resolutionBack720pSupported = true;
        }
      if(sizeBackCamera.getWidth() == 1920 && sizeBackCamera.getHeight() == 1080){
        resolutionBack1080pSupported = true;
        }
      if(sizeBackCamera.getWidth() == 3840 && sizeBackCamera.getHeight() == 2160){
        resolutionBack2160pSupported = true;
        }
    }

    if(camera2Settings.hasFrontCamera()) {
      for(Size sizeFrontCamera: camera2Settings.getSupportedVideoSizes(FRONT_CAMERA_ID)){
        if(sizeFrontCamera.getWidth() == 1280 && sizeFrontCamera.getHeight() == 720){
          resolutionFront720pSupported = true;
        }
        if(sizeFrontCamera.getWidth() == 1920 && sizeFrontCamera.getHeight() == 1080){
          resolutionFront1080pSupported = true;
        }
        if(sizeFrontCamera.getWidth() == 3840 && sizeFrontCamera.getHeight() == 2160){
          resolutionFront2160pSupported = true;
        }
      }
    }

    if(!resolutionBack1080pSupported){
      defaultResolution = BuildConfig.FEATURE_VERTICAL_VIDEOS ? CAMERA_SETTING_RESOLUTION_V_720
          : CAMERA_SETTING_RESOLUTION_H_720;
      //resolutionBack1080pSupported = true;
    }

    resolutionsSupportedMap.put(CAMERA_SETTING_RESOLUTION_720_BACK_ID, resolutionBack720pSupported);
    resolutionsSupportedMap.put(CAMERA_SETTING_RESOLUTION_1080_BACK_ID, resolutionBack1080pSupported);
    resolutionsSupportedMap.put(CAMERA_SETTING_RESOLUTION_2160_BACK_ID, resolutionBack2160pSupported);
    resolutionsSupportedMap.put(CAMERA_SETTING_RESOLUTION_720_FRONT_ID, resolutionFront720pSupported);
    resolutionsSupportedMap.put(CAMERA_SETTING_RESOLUTION_1080_FRONT_ID, resolutionFront1080pSupported);
    resolutionsSupportedMap.put(CAMERA_SETTING_RESOLUTION_2160_FRONT_ID, resolutionFront2160pSupported);

    ResolutionSetting resolutionSetting = new ResolutionSetting(defaultResolution,
            resolutionsSupportedMap);

    cameraSettings = cameraSettingsRepository.getCameraSettings();
    if(cameraSettings != null) {
      cameraSettingsRepository.setResolutionSettingSupported(cameraSettings, resolutionSetting);
    }

  }

  private void checkCamera2FrameRateSupported(Range<Integer>[] fpsRange)
          throws CameraAccessException {

    HashMap<Integer, Boolean> frameRateMap = new HashMap<>();
    boolean frameRate24FpsSupported = false;
    boolean frameRate25FpsSupported = false;
    boolean frameRate30FpsSupported = false;
    String defaultFrameRate = Constants.DEFAULT_CAMERA_SETTING_FRAME_RATE;
    Range<Integer> fps24 = new Range<>(24, 24);
    Range<Integer> fps25 = new Range<>(25, 25);
    Range<Integer> fps30 = new Range<>(30, 30);

    for(Range<Integer> fps: fpsRange) {
      if (fps.equals(fps24)) {
        frameRate24FpsSupported = true;
      } else {
        if (fps.equals(fps25)) {
          frameRate25FpsSupported = true;
        } else {
          if (fps.equals(fps30)) {
            frameRate30FpsSupported = true;
          }
        }
      }
    }

    frameRateMap.put(CAMERA_SETTING_FRAME_RATE_24_ID, frameRate24FpsSupported);
    frameRateMap.put(CAMERA_SETTING_FRAME_RATE_25_ID, frameRate25FpsSupported);
    frameRateMap.put(CAMERA_SETTING_FRAME_RATE_30_ID, frameRate30FpsSupported);

    FrameRateSetting frameRateSetting = new FrameRateSetting(defaultFrameRate, frameRateMap);
    cameraSettings = cameraSettingsRepository.getCameraSettings();
    if(cameraSettings != null) {
      cameraSettingsRepository.setFrameRateSettingSupported(cameraSettings, frameRateSetting);
    }
  }

  public void init() {
    runSyncAdapterHelper.runSyncAdapterPeriodically();
    if (BuildConfig.FEATURE_VERTICAL_VIDEOS) {
      initAppView.screenOrientationPortrait();
    } else {
      initAppView.screenOrientationLandscape();
    }
    executeUseCaseCall(() -> deleteCache());
  }

  protected void deleteCache() {
    FileUtils.cleanDirectory(context.getCacheDir());
  }
}

package com.videonasocialmedia.vimojo.cameraSettings.domain;

import android.content.Context;

import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.cameraSettings.model.CameraSettingsItem;
import com.videonasocialmedia.vimojo.cameraSettings.model.CameraSettingsPackage;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.record.domain.AdaptVideoToFormatUseCase;
import com.videonasocialmedia.vimojo.record.domain.GetCameraPreferencesUseCase;
import com.videonasocialmedia.vimojo.repository.camerapref.CameraPrefRealmRepository;
import com.videonasocialmedia.vimojo.utils.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ruth on 14/11/17.
 */

public class GetCameraSettingsListUseCase {
  private GetCameraPreferencesUseCase getCameraPreferencesUseCase;
  private Context context;
  private boolean DEFAULT_INTERFACE_PRO_BASIC_AVAILABLE = true;
  private Project currentProject;
  private WeakReference<GetCameraSettingsListUseCase.CameraSettingListUseCaseListener>
      cameraSettingListListener;

  @Inject
  public GetCameraSettingsListUseCase(Context context, GetCameraPreferencesUseCase
      getCameraPreferencesUseCase) {
    this.context = context;
    this.getCameraPreferencesUseCase = getCameraPreferencesUseCase;
    currentProject = loadCurrentProject();
  }

  private Project loadCurrentProject() {
    return Project.getInstance(null, null, null, null);
  }

  public void checkCameraSettingsList(CameraSettingListUseCaseListener listener) {
    this.cameraSettingListListener = new WeakReference<>(listener);
    List <CameraSettingsPackage> preferenceList = new ArrayList();

    List<CameraSettingsItem> interfaceProList = new ArrayList();

    interfaceProList.add(new CameraSettingsItem(Constants.CAMERA_PREF_INTERFACE_PRO_ID,
        context.getString(R.string.camera_pro), getCameraPreferencesUseCase.isInterfaceProSelected()));
    interfaceProList.add(new CameraSettingsItem(Constants.CAMERA_PREF_INTERFACE_BASIC_ID,
        context.getString(R.string.camera_basic), !getCameraPreferencesUseCase.isInterfaceProSelected()));
    DEFAULT_INTERFACE_PRO_BASIC_AVAILABLE = true;
    preferenceList.add(new CameraSettingsPackage(context.getString(R.string.camera_pro_o_basic),
        interfaceProList, DEFAULT_INTERFACE_PRO_BASIC_AVAILABLE));

    List<CameraSettingsItem> resolutionList = new ArrayList();
    String resolutionSelected = getCameraPreferencesUseCase.getResolutionPreference().getResolution();
    if(getCameraPreferencesUseCase.getResolutionPreference().isResolutionBack720pSupported()) {
      resolutionList.add(new CameraSettingsItem(Constants.CAMERA_PREF_RESOLUTION_720_ID,
          context.getString(R.string.low_resolution_name),
          isResolution720Selected(resolutionSelected)));
    }
    if(getCameraPreferencesUseCase.getResolutionPreference().isResolutionBack1080pSupported()) {
      resolutionList.add(new CameraSettingsItem(Constants.CAMERA_PREF_RESOLUTION_1080_ID,
          context.getString(R.string.good_resolution_name),
          isResolution1080Selected(resolutionSelected)));
    }
    if(getCameraPreferencesUseCase.getResolutionPreference().isResolutionBack2160pSupported()) {
      resolutionList.add(new CameraSettingsItem(Constants.CAMERA_PREF_RESOLUTION_2160_ID,
          context.getString(R.string.high_resolution_name),
          isResolution2160Selected(resolutionSelected)));
    }
    preferenceList.add(new CameraSettingsPackage(context.getString(R.string.resolution),
        resolutionList, isCameraSettingAvailable(currentProject)));

    List<CameraSettingsItem> frameRateList = new ArrayList();
    String frameRateSelected = getCameraPreferencesUseCase.getFrameRatePreference().getFrameRate();
    if(getCameraPreferencesUseCase.getFrameRatePreference().isFrameRate24FpsSupported()) {
      frameRateList.add(new CameraSettingsItem(Constants.CAMERA_PREF_FRAME_RATE_24_ID,
          context.getString(R.string.low_frame_rate_name), frameRateSelected.compareTo(Constants.CAMERA_PREF_FRAME_RATE_24) == 0));
    }
    if(getCameraPreferencesUseCase.getFrameRatePreference().isFrameRate25FpsSupported()) {
      frameRateList.add(new CameraSettingsItem(Constants.CAMERA_PREF_FRAME_RATE_25_ID,
          context.getString(R.string.good_frame_rate_name), frameRateSelected.compareTo(Constants.CAMERA_PREF_FRAME_RATE_25) == 0));
    }
    if(getCameraPreferencesUseCase.getFrameRatePreference().isFrameRate30FpsSupported()) {
      frameRateList.add(new CameraSettingsItem(Constants.CAMERA_PREF_FRAME_RATE_30_ID,
          context.getString(R.string.high_frame_rate_name), frameRateSelected.compareTo(Constants.CAMERA_PREF_FRAME_RATE_30) == 0));
    }
    preferenceList.add(new CameraSettingsPackage(context.getString(R.string.frame_rate), frameRateList,
        isCameraSettingAvailable(currentProject)));

    List<CameraSettingsItem> qualityList = new ArrayList();
    String qualitySelected = getCameraPreferencesUseCase.getQualityPreference();
    qualityList.add(new CameraSettingsItem(Constants.CAMERA_PREF_QUALITY_16_ID,
        context.getString(R.string.low_quality_name), qualitySelected.compareTo(Constants.CAMERA_PREF_QUALITY_16) == 0));
    qualityList.add(new CameraSettingsItem(Constants.CAMERA_PREF_QUALITY_32_ID,
        context.getString(R.string.good_quality_name), qualitySelected.compareTo(Constants.CAMERA_PREF_QUALITY_32) == 0));
    qualityList.add(new CameraSettingsItem(Constants.CAMERA_PREF_QUALITY_50_ID,
        context.getString(R.string.high_quality_name), qualitySelected.compareTo(Constants.CAMERA_PREF_QUALITY_50) == 0));
    preferenceList.add(new CameraSettingsPackage(context.getString(R.string.quality), qualityList,
        isCameraSettingAvailable(currentProject)));

    CameraSettingListUseCaseListener listUseCaseListener = cameraSettingListListener.get();
    listUseCaseListener.onSuccessGettingList(preferenceList);
  }

  private boolean isResolution2160Selected(String resolutionSelected) {
    return resolutionSelected.compareTo(Constants.CAMERA_PREF_RESOLUTION_2160) == 0;
  }

  private boolean isResolution1080Selected(String resolutionSelected) {
    return resolutionSelected.compareTo(Constants.CAMERA_PREF_RESOLUTION_1080) == 0;
  }

  private boolean isResolution720Selected(String resolutionSelected) {
    return resolutionSelected.compareTo(Constants.CAMERA_PREF_RESOLUTION_720) == 0;
  }

  private boolean isCameraSettingAvailable(Project project) {
    return !project.getVMComposition().hasVideos();
  }

  public interface CameraSettingListUseCaseListener {
    void onSuccessGettingList(List<CameraSettingsPackage> cameraSettingsPackages);
    void onErrorGettingList(String message);
  }

}

/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.vimojo.record.presentation.mvp.presenters;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.videonasocialmedia.camera.camera2.Camera2Wrapper;
import com.videonasocialmedia.camera.camera2.Camera2WrapperListener;
import com.videonasocialmedia.camera.customview.AutoFitTextureView;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.domain.editor.AddVideoToProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.export.domain.GetVideoFormatFromCurrentProjectUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.record.presentation.mvp.views.RecordCamera2View;

import java.util.List;

/**
 *  Created by alvaro on 16/01/17.
 */

public class RecordCamera2Presenter implements Camera2WrapperListener {

  // TODO:(alvaro.martinez) 26/01/17  ADD TRACKING TO RECORD ACTIVITY. Update from RecordActivity
  private static final String LOG_TAG = "RecordPresenter";
  private final Context context;
  private final boolean isRightControlsViewSelected;
  private final boolean isPrincipalViewSelected;
  private RecordCamera2View recordView;
  private AddVideoToProjectUseCase addVideoToProjectUseCase;
  private int recordedVideosNumber;
  protected Project currentProject;
  private int height = 720;
  private boolean externalIntent;
  private Camera2Wrapper camera;

  public RecordCamera2Presenter(Context context, RecordCamera2View recordView,
                                boolean isFrontCameraSelected, boolean isPrincipalViewSelected,
                                boolean isRightControlsViewSelected, AutoFitTextureView textureView,
                                boolean externalIntent, String directorySaveVideos,
                                GetVideoFormatFromCurrentProjectUseCase
                                    getVideoFormatFromCurrentProjectUseCase,
                                AddVideoToProjectUseCase addVideoToProjectUseCase) {

    this.recordView = recordView;
    this.context = context;
    this.isPrincipalViewSelected = isPrincipalViewSelected;
    this.isRightControlsViewSelected = isRightControlsViewSelected;
    this.externalIntent = externalIntent;
    int cameraId = 0;
    if(isFrontCameraSelected)
      cameraId = 1;
    // TODO:(alvaro.martinez) 25/01/17 Support camera1, api <21 or combine both. Make Camera1Wrapper
    camera = new Camera2Wrapper(context, this, cameraId, textureView, directorySaveVideos,
        getVideoFormatFromCurrentProjectUseCase.getVideoRecordedFormatFromCurrentProjectUseCase());

    this.addVideoToProjectUseCase = addVideoToProjectUseCase;

  }

  public void initViews() {
    recordView.setResolutionSelected(height);
    recordView.hideChronometer();
    if(isPrincipalViewSelected) {
      recordView.showPrincipalViews();
    }else {
      recordView.hidePrincipalViews();
    }
    if(isRightControlsViewSelected) {
      recordView.showRightControlsView();
    }else {
      recordView.hideRightControlsView();
    }
  }

  public void onResume() {
    if (!externalIntent)
      showThumbAndNumber();
    Log.d(LOG_TAG, "resume presenter");
    camera.onResume();
  }

  public void onPause() {
    camera.onPause();
  }

  private void showThumbAndNumber() {
    GetMediaListFromProjectUseCase getMediaListFromProjectUseCase =
        new GetMediaListFromProjectUseCase();
    final List mediaInProject = getMediaListFromProjectUseCase.getMediaListFromProject();
    if (mediaInProject != null && mediaInProject.size() > 0) {
      int lastItemIndex = mediaInProject.size() - 1;
      final Video lastItem = (Video) mediaInProject.get(lastItemIndex);
      this.recordedVideosNumber = mediaInProject.size();
      recordView.showVideosRecordedNumber(recordedVideosNumber);
      recordView.showRecordedVideoThumb(lastItem.getMediaPath());
    } else {
      recordView.hideVideosRecordedNumber();
    }
  }

  public void startRecord() {

    camera.startRecordingVideo();

    recordView.showStopButton();
    recordView.startChronometer();
    recordView.showChronometer();
    recordView.hideNavigateToSettingsActivity();
    recordView.hideVideosRecordedNumber();
    recordView.hideRecordedVideoThumb();
    recordView.hideChangeCamera();
  }

  public void stopRecord() {
    camera.stopRecordVideo();
    restartPreview();
  }


  @Override
  public void setFlashSupport() {
    if (camera.isFlashSupported()) {
      recordView.setFlashSupported(true);
      Log.d(LOG_TAG, "checkSupportFlash flash Supported camera");
    } else {
       recordView.setFlashSupported(false);
      Log.d(LOG_TAG, "checkSupportFlash flash NOT Supported camera");
    }
  }

  @Override
  public void videoRecorded(String path){
    if (externalIntent) {
      recordView.finishActivityForResult(path);
    } else {
      addVideoToProjectUseCase.addVideoToTrack(path);
      recordView.showRecordButton();
      recordView.showNavigateToSettingsActivity();
      recordView.stopChronometer();
      recordView.hideChronometer();
      recordView.showChangeCamera();
      recordView.showRecordedVideoThumb(path);
      recordView.showVideosRecordedNumber(++recordedVideosNumber);
    }
  }

  @Override
  public void setZoom(Rect rectValue) {
    // TODO:(alvaro.martinez) 27/01/17 Convert zoom from 0 to 1 and show on RecordView
    //recordView.setZoom(0.5f);
  }

  public void restartPreview(){
    camera.onPause();
    camera.onResume();
  }

  public void setFlashOff() {
    camera.setFlashOff();
    recordView.setFlash(false);
  }

  public void toggleFlash(boolean isSelected) {

    if(isSelected){
      camera.setFlashOff();
    } else {
      camera.setFlashOn();
    }
    recordView.setFlash(!isSelected);
  }

  public void onTouchZoom(MotionEvent event) {
    camera.onTouchZoom(getFingerSpacing(event));
    // RecordView show slide zoom, from 0 to 1
  }

  //Determine the space between the first two fingers
  @SuppressWarnings("deprecation")
  private float getFingerSpacing(MotionEvent event) {

    float x = event.getX(0) - event.getX(1);
    float y = event.getY(0) - event.getY(1);
    return (float) Math.sqrt(x * x + y * y);
  }

  public void showRightControls() {
    recordView.showRightControlsView();
  }

  public void hideRightControls() {
    recordView.hideRightControlsView();
  }

  public void bottomSettingsCamera(boolean isSelected) {
    if(isSelected) {
      recordView.hideBottomControlsView();
    } else {
      recordView.showBottomControlsView();
    }
  }

  public void onTouchFocus(MotionEvent event) {
    int x = Math.round(event.getX());
    int y = Math.round(event.getY());
    camera.setFocus(calculateBounds(x, y), 100);
    recordView.setFocus(event);
  }

  private Rect calculateBounds(int x, int y) {
    Rect focusIconBounds = new Rect();
    // TODO:(alvaro.martinez) 24/01/17 Define area to calculate autofocus
    int halfHeight = 100; // focusIcon.getIntrinsicHeight();
    int halfWidth = 100; //focusIcon.getIntrinsicWidth();
    focusIconBounds.set(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
    return focusIconBounds;
  }
}


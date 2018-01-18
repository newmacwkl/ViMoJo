package com.videonasocialmedia.vimojo.userProfile.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;

import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.domain.ObtainLocalVideosUseCase;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnVideosRetrieved;
import com.videonasocialmedia.vimojo.userProfile.presentation.mvp.views.UserProfileView;
import com.videonasocialmedia.vimojo.utils.ConfigPreferences;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by ruth on 13/10/17.
 */

public class UserProfilePresenter {

  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor preferencesEditor;
  private UserProfileView userProfileView;
  private ObtainLocalVideosUseCase obtainLocalVideosUseCase;
  private UserEventTracker userEventTracker;

  @Inject
  public UserProfilePresenter(UserProfileView view, UserEventTracker userEventTracker,
                              SharedPreferences sharedPreferences, ObtainLocalVideosUseCase
                              obtainLocalVideosUseCase){
    this.userEventTracker = userEventTracker;
    this.userProfileView =view;
    this.sharedPreferences = sharedPreferences;
    this.obtainLocalVideosUseCase = obtainLocalVideosUseCase;
  }

  public void getUserNameFromPreferences() {
    String userNamePreference = sharedPreferences.getString(ConfigPreferences.USERNAME, null);
    if(userNamePreference!=null && !userNamePreference.isEmpty())
      userProfileView.showPreferenceUserName(userNamePreference);
  }

  public void getEmailFromPreferences() {
    // TODO:(alvaro.martinez) 17/01/18 Get email from user register
    String emailPreference=sharedPreferences.getString(ConfigPreferences.EMAIL,null);
    if(emailPreference!=null && !emailPreference.isEmpty())
      userProfileView.showPreferenceEmail(emailPreference);
  }

  public void updateUserNamePreference(String userNamePreference) {
    preferencesEditor = sharedPreferences.edit();
    preferencesEditor.putString(ConfigPreferences.USERNAME, userNamePreference);
    preferencesEditor.apply();
    userProfileView.showPreferenceUserName(userNamePreference);
    String userName = sharedPreferences.getString(ConfigPreferences.USERNAME, null);
    if (userName != null && !userName.isEmpty()) {
      userEventTracker.trackUpdateUserName(userName);
    }
  }

  public void updateUserEmailPreference(String userEmailPreference) {
    if(!isValidEmail(userEmailPreference)) {
      userProfileView.showError(R.string.invalid_email);
      return;
    }
    preferencesEditor = sharedPreferences.edit();
    preferencesEditor.putString(ConfigPreferences.EMAIL, userEmailPreference);
    preferencesEditor.apply();
    userProfileView.showPreferenceEmail(userEmailPreference);
    String email = sharedPreferences.getString(ConfigPreferences.EMAIL, null);
    if (email != null && !email.isEmpty()) {
      userEventTracker.trackUpdateUserEmail(email);
    }
  }

  public void getInfoVideosRecordedEditedShared() {
    userProfileView.showLoading();

    int videosRecorded = sharedPreferences
        .getInt(ConfigPreferences.TOTAL_VIDEOS_RECORDED, 0);
    userProfileView.showVideosRecorded(Integer.toString(videosRecorded));

    obtainLocalVideosUseCase.obtainEditedVideos(new OnVideosRetrieved() {
      @Override
      public void onVideosRetrieved(List<Video> videoList) {
        userProfileView.showVideosEdited(Integer.toString(videoList.size()));
        userProfileView.hideLoading();
      }

      @Override
      public void onNoVideosRetrieved() {
        userProfileView.hideLoading();
      }
    });

    int videosShared = sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0);
    userProfileView.showVideosShared(Integer.toString(videosShared));

  }

  protected boolean isValidEmail(String email) {
    return android.util.Patterns.EMAIL_ADDRESS
        .matcher(email).matches();
  }

}

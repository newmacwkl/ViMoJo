package com.videonasocialmedia.vimojo.presentation.mvp.views;

/**
 * Created by ruth on 23/11/16.
 */

public interface EditorActivityView {

  void showPreferenceUserName(String data);

  void showPreferenceEmail(String emailPreference);

  void updateViewResetProject();

  void showError(int causeTextResource);

  void showMessage(int stringToast);

  void expandFabMenu();

  void restartShareActivity(String extraDataIntent);

  void restartActivity();
}

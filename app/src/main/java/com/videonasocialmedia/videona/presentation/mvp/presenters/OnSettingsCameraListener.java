/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.videona.presentation.mvp.presenters;

public interface OnSettingsCameraListener {

    void onSettingsCameraSuccess(boolean isChangeCameraSupported, boolean isFlashSupported);
}
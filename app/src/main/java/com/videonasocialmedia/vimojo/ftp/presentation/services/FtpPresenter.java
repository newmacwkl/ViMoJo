package com.videonasocialmedia.vimojo.ftp.presentation.services;

import com.videonasocialmedia.vimojo.ftp.domain.FtpController;
import com.videonasocialmedia.vimojo.ftp.domain.ProgressListener;

/**
 *
 */
public class FtpPresenter implements ProgressListener {

    public static final String FTP_USER = "videona";
    public static final String FTP_PASSWORD = "v1d3onapa$$";
    FtpUploaderView view;

    public void onCreate(FtpUploaderView view) {
        this.view = view;
    }

    public void startUpload(String videoPath) {
        FtpController ftpController = new FtpController();
//        ftpController.uploadVideo("jca", "AxZm0473", videoPath, this);
//        ftpController.uploadVideo("videonaftp", "passv1d30n4", videoPath, this);
        ftpController.uploadVideo(FTP_USER, FTP_PASSWORD, videoPath, this);
        view.showNotification(true);
    }

    @Override
    public void onSuccessFinished() {
        view.setNotificationProgress(200);
    }

    @Override
    public void onErrorFinished() {

    }

    @Override
    public void onProgressUpdated(int progress) {
        view.setNotificationProgress(progress);
    }
}

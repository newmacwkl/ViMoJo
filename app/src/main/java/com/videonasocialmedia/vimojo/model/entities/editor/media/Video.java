/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas
 * Álvaro Martínez Marco
 * Danny R. Fonseca Arboleda
 */
package com.videonasocialmedia.vimojo.model.entities.editor.media;

import android.media.MediaMetadataRetriever;

import com.videonasocialmedia.vimojo.utils.Constants;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A media video item that represents a file (or part of a file) that can be used in project video
 * track.
 *
 * @see com.videonasocialmedia.vimojo.model.entities.editor.media.Media
 */
public class Video extends Media {

    private static final AtomicInteger count = new AtomicInteger(0);

    public static String VIDEO_FOLDER_PATH;

    /**
     * The total duration of the file media resource
     */
    private int fileDuration;

    private String tempPath;

    private boolean isTempPathFinished = false;

    // TODO(jliarte): 14/06/16 this entity should not depend on MediaMetadataRetriever as it is part of android
    /* Needed to allow mockito inject it */
    private MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    private int duration;


    /**
     * protected default empty constructor, trying to get injectMocks working
     */
    protected Video() {
        super();
    }

    /**
     * Constructor of minimum number of parameters. Default constructor.
     *
     * @see com.videonasocialmedia.vimojo.model.entities.editor.media.Media
     */
    public Video(String mediaPath) {
        super(-1, null, mediaPath, 0, 0, null, null);
        try {
            retriever.setDataSource(mediaPath);

            fileDuration = Integer.parseInt(retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION));
            startTime = 0;
            stopTime = fileDuration;
        } catch (Exception e) {
            fileDuration = 0;
            stopTime = 0;
        }
    }

    public Video(String mediaPath, int fileStartTime, int duration) {
        super(-1, null, mediaPath, fileStartTime, duration, null, null);
        fileDuration = getFileDuration(mediaPath);
    }

    public Video(Video video) {
        super(-1, null, video.getMediaPath(), video.getStartTime(),
                video.getDuration(), null, null);
        fileDuration = getFileDuration(video.getMediaPath());
        stopTime = video.getStopTime();
        if(video.isEdited()) {
            tempPath = video.getTempPath();
        }
    }

    public int getFileDuration() {
        return fileDuration;
    }

    private int getFileDuration(String path) {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        return Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath() {
        tempPath = Constants.PATH_APP_TEMP + File.separator + "temp_" + System.currentTimeMillis() + ".mp4";
    }

    public boolean outputVideoIsFinished() {
        return isTempPathFinished;
    }

    public void setTempPathFinished(boolean tempPathFinished) {
        isTempPathFinished = tempPathFinished;
    }

    public void deleteTempVideo() {
        if (tempPath != null) {
            File f = new File(tempPath);
            f.delete();
            tempPath = null;
        }
    }

    public void setIdentifier() {
        if (identifier < 1)
            this.identifier = count.addAndGet(1);
    }

    public boolean isEdited() {
        return tempPath!=null;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
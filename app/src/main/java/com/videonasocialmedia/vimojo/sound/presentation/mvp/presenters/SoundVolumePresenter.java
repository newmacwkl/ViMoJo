package com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters;


import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnVideosRetrieved;
import com.videonasocialmedia.vimojo.sound.domain.MixAudioUseCase;
import com.videonasocialmedia.vimojo.sound.domain.OnMixAudioListener;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.views.SoundVolumeView;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import java.io.File;
import java.util.List;

/**
 * Created by ruth on 19/09/16.
 */
public class SoundVolumePresenter implements OnVideosRetrieved, OnMixAudioListener {

    private GetMediaListFromProjectUseCase getMediaListFromProjectUseCase;
    private MixAudioUseCase mixAudioUseCase;
    private SoundVolumeView soundVolumeView;

    public UserEventTracker userEventTracker;
    public Project currentProject;

    private String voiceOverRecordedPath;
    private String tempVideoProjectExportedPath;

    public SoundVolumePresenter(SoundVolumeView soundVolumeView){
        this.soundVolumeView=soundVolumeView;
        getMediaListFromProjectUseCase = new GetMediaListFromProjectUseCase();
        mixAudioUseCase = new MixAudioUseCase(this);
        this.currentProject = loadCurrentProject();
    }

    private Project loadCurrentProject() {
        return Project.getInstance(null, null, null);
    }

    public void init() {
        getMediaListFromProjectUseCase.getMediaListFromProject(this);
    }

    @Override
    public void onVideosRetrieved(List<Video> videoList) {
        soundVolumeView.bindVideoList(videoList);
    }

    @Override
    public void onNoVideosRetrieved() {
        soundVolumeView.resetPreview();
    }

    public void setVolume(String voiceOverPath, String videoTemPathMixAudio, float volume){
        currentProject.setMusicOnProject(false);
        mixAudioUseCase.mixAudio(voiceOverPath, videoTemPathMixAudio,volume);
        voiceOverRecordedPath = voiceOverPath;
        tempVideoProjectExportedPath = videoTemPathMixAudio;
    }

    @Override
    public void onMixAudioSuccess() {
        cleanTempFiles();
        currentProject.setMusicOnProject(true);
        soundVolumeView.goToEditActivity();
    }

    @Override
    public void onMixAudioError() {
        cleanTempFiles();
        currentProject.setMusicOnProject(false);
    }

    private void cleanTempFiles(){

        File fVoiceOver = new File(voiceOverRecordedPath);
        if(fVoiceOver.exists()){
            fVoiceOver.delete();
        }
        File fTempVideoExported = new File(tempVideoProjectExportedPath);
        if(fTempVideoExported.exists()){
            fTempVideoExported.delete();
        }
    }

}
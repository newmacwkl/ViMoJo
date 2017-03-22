package com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters;

import com.videonasocialmedia.videonamediaframework.model.Constants;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.domain.editor.GetMusicFromProjectUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.GetMusicFromProjectCallback;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnVideosRetrieved;
import com.videonasocialmedia.vimojo.settings.domain.GetPreferencesTransitionFromProjectUseCase;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.views.SoundView;

import java.util.ArrayList;
import java.util.List;

import static com.videonasocialmedia.videonamediaframework.model.Constants.*;

import javax.inject.Inject;

/**
 * Created by ruth on 13/09/16.
 */
public class SoundPresenter implements OnVideosRetrieved, GetMusicFromProjectCallback {


    private SoundView soundView;
    private GetMediaListFromProjectUseCase getMediaListFromProjectUseCase;
    private GetMusicFromProjectUseCase getMusicFromProjectUseCase;
    private GetPreferencesTransitionFromProjectUseCase getPreferencesTransitionFromProjectUseCase;
    private final Project currentProject;

   @Inject
    public SoundPresenter(SoundView soundView, GetMediaListFromProjectUseCase
        getMediaListFromProjectUseCase, GetMusicFromProjectUseCase getMusicFromProjectUseCase,
        GetPreferencesTransitionFromProjectUseCase getPreferencesTransitionFromProjectUseCase) {
        this.getMediaListFromProjectUseCase = getMediaListFromProjectUseCase;
        this.getMusicFromProjectUseCase = getMusicFromProjectUseCase;
        this.soundView = soundView;
        this.currentProject = loadCurrentProject();
        this.getPreferencesTransitionFromProjectUseCase = getPreferencesTransitionFromProjectUseCase;
    }

    public Project loadCurrentProject() {
        // TODO(jliarte): this should make use of a repository or use case to load the Project
        return Project.getInstance(null, null, null);
    }

    public void init() {
      obtainVideos();
      retrieveCompositionMusic();
      // TODO:(alvaro.martinez) 22/03/17 Player should be in charge of these checks from VMComposition 
      checkAVTransitionsActivated();
    }

  private void checkAVTransitionsActivated() {
    if(getPreferencesTransitionFromProjectUseCase.isVideoFadeTransitionActivated()){
      soundView.setVideoFadeTransitionAmongVideos();
    }
    if(getPreferencesTransitionFromProjectUseCase.isAudioFadeTransitionActivated() &&
        !currentProject.getVMComposition().hasMusic()){
      soundView.setAudioFadeTransitionAmongVideos();
    }
  }

  private void retrieveCompositionMusic() {
      if(currentProject.hasMusic()){
        getMusicFromProjectUseCase.getMusicFromProject(this);
      }
    }

    private void obtainVideos() {
        getMediaListFromProjectUseCase.getMediaListFromProject(this);
    }

    @Override
    public void onVideosRetrieved(List<Video> videoList) {
        soundView.bindVideoList(videoList);
    }

    @Override
    public void onNoVideosRetrieved() {
        //TODO Show error
        soundView.resetPreview();
    }

    @Override
    public void onMusicRetrieved(Music music) {
        // TODO:(alvaro.martinez) 7/03/17 Get from project use case list<Music> instead music
        List<Music> musicList = new ArrayList<>();
        musicList.add(music);
        if (music.getMusicTitle()
            .compareTo(com.videonasocialmedia.vimojo.utils.Constants.MUSIC_AUDIO_VOICEOVER_TITLE) == 0) {
            soundView.bindVoiceOverList(musicList);
        } else {
            soundView.bindMusicList(musicList);
        }
    }
}

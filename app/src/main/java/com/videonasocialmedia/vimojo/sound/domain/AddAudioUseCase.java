package com.videonasocialmedia.vimojo.sound.domain;

import com.videonasocialmedia.videonamediaframework.model.Constants;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.videonamediaframework.model.media.exceptions.IllegalItemOnTrack;
import com.videonasocialmedia.videonamediaframework.model.media.track.AudioTrack;
import com.videonasocialmedia.videonamediaframework.model.media.track.Track;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnAddMediaFinishedListener;
import com.videonasocialmedia.vimojo.repository.music.MusicRepository;
import com.videonasocialmedia.vimojo.repository.project.ProjectRepository;
import com.videonasocialmedia.vimojo.repository.track.TrackRepository;

import javax.inject.Inject;

/**
 * Created by alvaro on 1/06/17.
 */

public class AddAudioUseCase {

  private Project currentProject;
  private ProjectRepository projectRepository;
  private final int SECOND_POSITION = 2;
  private final int FIRST_POSITION = 1;

  @Inject
  public AddAudioUseCase(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
    currentProject = Project.getInstance(null,null,null);
  }

  public void addMusic(Music music, int trackIndex, OnAddMediaFinishedListener listener) {
    Track audioTrack = createOrGetTrackFromProject(trackIndex);
    updateTrack(audioTrack, trackIndex, music);
    addMusicToTrack(music, listener, audioTrack);
    updateProject();
  }


  private AudioTrack createOrGetTrackFromProject(int trackIndex) {
    //Trick to manage correct index in AudioTracks arrayList. VMComposition always will have at
    // least one audio track, musicTrack with index INDEX_AUDIO_TRACK_MUSIC. It it would be exist
    // voice overTrack will be in next position in index
    if(currentProject.getAudioTracks().size() == 0) {
      if(trackIndex == Constants.INDEX_AUDIO_TRACK_MUSIC){
        AudioTrack audioTrack = new AudioTrack(trackIndex);
        currentProject.getAudioTracks().add(0, audioTrack);
        return audioTrack;
      } else {
        AudioTrack audioTrack = new AudioTrack(Constants.INDEX_AUDIO_TRACK_VOICE_OVER);
        currentProject.getAudioTracks().add(0, new AudioTrack(Constants.INDEX_AUDIO_TRACK_MUSIC));
        currentProject.getAudioTracks().add(1, audioTrack);
        return audioTrack;
      }
    }
    if(trackIndex != Constants.INDEX_AUDIO_TRACK_MUSIC){
      AudioTrack audioTrack = new AudioTrack(Constants.INDEX_AUDIO_TRACK_VOICE_OVER);
      currentProject.getAudioTracks().add(1, audioTrack);
    }
    return currentProject.getAudioTracks().get(trackIndex);
  }

  private void updateTrack(Track audioTrack, int trackIndex, Music music) {
    audioTrack.setPosition(getTrackPositionByUserInteraction(audioTrack, trackIndex));
    audioTrack.setVolume(music.getVolume());
  }

  // Only add one item at the beginning, index 0.
  private void addMusicToTrack(Music music, OnAddMediaFinishedListener listener, Track audioTrack) {
    try {
      audioTrack.insertItemAt(0,music);
      listener.onAddMediaItemToTrackSuccess(music);
    } catch (IndexOutOfBoundsException | IllegalItemOnTrack exception) {
      listener.onAddMediaItemToTrackError();
    }
  }

  private void updateProject() {
    projectRepository.update(currentProject);
  }

  private int getTrackPositionByUserInteraction(Track audioTrack, int trackIndex) {
    if(audioTrack.getPosition()!=0){
      return audioTrack.getPosition();
    }
    switch (trackIndex){
      case Constants.INDEX_AUDIO_TRACK_MUSIC:
        if(currentProject.hasVoiceOver()){
          return SECOND_POSITION;
        } else {
          return FIRST_POSITION;
        }
      case Constants.INDEX_AUDIO_TRACK_VOICE_OVER:
        if(currentProject.hasMusic()){
          return SECOND_POSITION;
        } else {
          return FIRST_POSITION;
        }
      default:
        return 0;
    }
  }
}
package com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.videonasocialmedia.transcoder.MediaTranscoder;
import com.videonasocialmedia.transcoder.video.format.VideonaFormat;
import com.videonasocialmedia.videonamediaframework.model.media.Media;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.videonamediaframework.pipeline.TranscoderHelper;
import com.videonasocialmedia.videonamediaframework.utils.TextToDrawable;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnAddMediaFinishedListener;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnRemoveMediaFinishedListener;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnVideosRetrieved;
import com.videonasocialmedia.vimojo.settings.mainSettings.domain.GetPreferencesTransitionFromProjectUseCase;
import com.videonasocialmedia.vimojo.sound.domain.AddAudioUseCase;
import com.videonasocialmedia.vimojo.sound.domain.RemoveAudioUseCase;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.views.VoiceOverRecordView;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.FileUtils;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

import static com.videonasocialmedia.videonamediaframework.model.Constants.*;


/**
 * Created by ruth on 15/09/16.
 */
public class VoiceOverRecordPresenter implements OnVideosRetrieved {

    private final String LOG_TAG = getClass().getSimpleName();
    private Context context;
    private GetMediaListFromProjectUseCase getMediaListFromProjectUseCase;
    private GetPreferencesTransitionFromProjectUseCase getPreferencesTransitionFromProjectUseCase;
    private AddAudioUseCase addAudioUseCase;
    private RemoveAudioUseCase removeAudioUseCase;
    private VoiceOverRecordView voiceOverRecordView;
    public UserEventTracker userEventTracker;
    public Project currentProject;
    private boolean isRecording = false;
    private Recorder audioRecorder;
    private String directoryVoiceOverRecorded;
    private boolean voiceOverRecorded = false;
    private final TextToDrawable drawableGenerator =
            new TextToDrawable(VimojoApplication.getAppContext());
    private final MediaTranscoder mediaTranscoder = MediaTranscoder.getInstance();
    protected TranscoderHelper transcoderHelper =
            new TranscoderHelper(drawableGenerator, mediaTranscoder);

    @Inject
    public VoiceOverRecordPresenter(Context context, VoiceOverRecordView voiceOverRecordView,
                                    GetMediaListFromProjectUseCase getMediaListFromProjectUseCase,
                                    GetPreferencesTransitionFromProjectUseCase
                                            getPreferencesTransitionFromProjectUseCase,
                                    AddAudioUseCase addAudioUseCase, RemoveAudioUseCase
                                    removeAudioUseCase, UserEventTracker userEventTracker) {
        this.context = context;
        this.voiceOverRecordView = voiceOverRecordView;
        this.getMediaListFromProjectUseCase = getMediaListFromProjectUseCase;
        this.getPreferencesTransitionFromProjectUseCase =
                getPreferencesTransitionFromProjectUseCase;
        this.addAudioUseCase = addAudioUseCase;
        this.removeAudioUseCase = removeAudioUseCase;
        this.userEventTracker = userEventTracker;
        this.currentProject = loadCurrentProject();
        directoryVoiceOverRecorded =
                currentProject.getProjectPathIntermediateAudioFilesVoiceOverRecord();
    }

    public void init() {
        obtainVideos();
        if (getPreferencesTransitionFromProjectUseCase.isVideoFadeTransitionActivated()) {
            voiceOverRecordView.setVideoFadeTransitionAmongVideos();
        }
        if (getPreferencesTransitionFromProjectUseCase.isAudioFadeTransitionActivated() &&
                !currentProject.getVMComposition().hasMusic()) {
            voiceOverRecordView.setAudioFadeTransitionAmongVideos();
        }
    }

    private void obtainVideos() {
        getMediaListFromProjectUseCase.getMediaListFromProject(this);
    }

    private Project loadCurrentProject() {
        return Project.getInstance(null, null, null, null);
    }

    @Override
    public void onVideosRetrieved(List<Video> videoList) {
      List<Video> copyVideoList = new ArrayList<>();
      for(Video video: videoList){
        Video copyVideo = new Video(video);
        copyVideo.setVolume(0f);
        copyVideoList.add(copyVideo);
      }
      voiceOverRecordView.bindVideoList(copyVideoList);
      voiceOverRecordView.initVoiceOverView(0, currentProject.getDuration());
    }

    @Override
    public void onNoVideosRetrieved() {
        voiceOverRecordView.resetPreview();
    }

    public void setVoiceOver(String finalNamePathAudioMerge) {
      if(isRecording()) {
        stopAudioRecorded();
      }
      if (isVoiceOverRecorded()) {
        applyVoiceOver(finalNamePathAudioMerge);
      } else {
          voiceOverRecordView.showError(context
                  .getString(R.string.alert_dialog_title_message_start_record_voice_over));
      }
    }

    protected void applyVoiceOver(String finalNamePathAudioMerge) {
      voiceOverRecordView.showProgressDialog();
      String voiceOverAbsolutePath = directoryVoiceOverRecorded + File.separator +
              finalNamePathAudioMerge;
      ListenableFuture<String> exportVoiceOverTask = transcoderHelper
              .generateOutputAudioVoiceOver(fileRecordedPcm().getAbsolutePath(),
                      voiceOverAbsolutePath);
      Futures.addCallback(exportVoiceOverTask, new
          VoiceOverTranscodingTaskCallback(voiceOverAbsolutePath));
    }

    protected void trackVoiceOverVideo() {
        userEventTracker.trackVoiceOverSet(currentProject);
    }

    public void startRecording() {
        Log.d(LOG_TAG, "startRecording");
        setupAudioRecorder();
        isRecording = true;
        voiceOverRecordView.playVideo();
        audioRecorder.startRecording();
        voiceOverRecorded = true;
    }

    public void pauseRecording() {
        Log.d(LOG_TAG, "pauseRecording");
        audioRecorder.pauseRecording();
        voiceOverRecordView.pauseVideo();
    }

    public void resumeRecording() {
        Log.d(LOG_TAG, "resumeRecording");
        audioRecorder.resumeRecording();
        voiceOverRecordView.playVideo();
    }

    public void stopRecording() {
        Log.d(LOG_TAG, "stopRecording");
        if(isRecording()){
          stopAudioRecorded();
        }
        voiceOverRecordView.disableRecordButton();
    }

    public void cancelVoiceOverRecorded() {
        voiceOverRecordView.resetVoiceOverRecorded();
        if(isRecording()){
            stopAudioRecorded();
        }
        voiceOverRecorded = false;
    }

    private void stopAudioRecorded() {
      isRecording = false;
      try {
        audioRecorder.stopRecording();
      } catch (IOException e) {
        e.printStackTrace();
        voiceOverRecordView.showError(context.getString(R.string.error_record_voice_over));
      }
    }

    private void setupAudioRecorder() {
        cleanFileRecordedPcm();
        audioRecorder = OmRecorder.pcm(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override
                    public void onAudioChunkPulled(AudioChunk audioChunk) {
                    }
                }), fileRecordedPcm());
    }

    private void cleanFileRecordedPcm() {
        if(fileRecordedPcm().exists()) {
            fileRecordedPcm().delete();
        }
    }

    private PullableSource mic() {
        VideonaFormat videonaFormat = currentProject.getVMComposition().getVideoFormat();
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        videonaFormat.getAudioSource(), videonaFormat.getAudioEncodingFormat(),
                        videonaFormat.getAudioChannelFormat(), videonaFormat.getAudioSampleRate()
                )
        );
    }

    @NonNull
    private File fileRecordedPcm() {
        return new File(directoryVoiceOverRecorded,
                Constants.AUDIO_TEMP_RECORD_VOICE_OVER_RAW_FILE_NAME);
    }

    public boolean isRecording() {
        return isRecording;
    }

    protected void addVoiceOver(final Music voiceOver) {
        addAudioUseCase.addMusic(voiceOver,
                INDEX_AUDIO_TRACK_VOICE_OVER,
                new OnAddMediaFinishedListener() {
                    @Override
                    public void onAddMediaItemToTrackSuccess(Media media) {
                      trackVoiceOverVideo();
                      voiceOverRecordView
                              .navigateToVoiceOverVolumeActivity(voiceOver.getMediaPath());
                    }
                    @Override
                    public void onAddMediaItemToTrackError() {
                      voiceOverRecordView.showError(context.getString(R.string
                                .alert_dialog_title_message_adding_voice_over));
                    }
                });
    }

    protected void deletePreviousVoiceOver(){
        removeAudioUseCase.removeMusic((Music) currentProject.getAudioTracks()
                        .get(INDEX_AUDIO_TRACK_VOICE_OVER).getItems().get(0),
                INDEX_AUDIO_TRACK_VOICE_OVER, new OnRemoveMediaFinishedListener() {
                    @Override
                    public void onRemoveMediaItemFromTrackSuccess() {

                    }

                    @Override
                    public void onRemoveMediaItemFromTrackError() {
                        voiceOverRecordView.showError(context.getString(R.string
                                .alert_dialog_title_message_adding_voice_over));
                    }
                });
    }

    @NonNull
    protected Music getVoiceOverAsMusic(String voiceOverPath) {
        Music voiceOver = new Music(voiceOverPath, FileUtils.getDuration(voiceOverPath));
        voiceOver.setMusicTitle(com.videonasocialmedia.vimojo.utils.Constants
                .MUSIC_AUDIO_VOICEOVER_TITLE);
        voiceOver.setMusicAuthor(" ");
        voiceOver.setIconResourceId(R.drawable.activity_edit_audio_voice_over_icon);
        return voiceOver;
    }

    protected boolean isVoiceOverRecorded() {
        return voiceOverRecorded;
    }

  private class VoiceOverTranscodingTaskCallback implements FutureCallback<String> {

    private String outputFilePath;

    private VoiceOverTranscodingTaskCallback(String outputFilePath) {
      this.outputFilePath = outputFilePath;
    }

    @Override
    public void onSuccess(String outputFilePath) {
      handleTranscodingSuccess(outputFilePath);
    }

    @Override
    public void onFailure(@NonNull Throwable t) {
      handleTranscodingError(outputFilePath, t.getMessage());
    }
  }

  private void handleTranscodingError(String outputFilePath, String message) {
    voiceOverRecordView.hideProgressDialog();
    voiceOverRecordView.showError(context.getString(R.string.error_transcoding_voice_over));
  }

  private void handleTranscodingSuccess(String outputFilePath) {
    voiceOverRecordView.hideProgressDialog();
    Music voiceOver = getVoiceOverAsMusic(outputFilePath);
    // TODO: 19/10/2017 Delete voice over from UI, not recording a new one.
    if(currentProject.hasVoiceOver()) {
      deletePreviousVoiceOver();
    }
    addVoiceOver(voiceOver);
  }
}
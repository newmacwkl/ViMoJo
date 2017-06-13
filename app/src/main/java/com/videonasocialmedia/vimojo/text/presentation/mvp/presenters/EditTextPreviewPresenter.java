package com.videonasocialmedia.vimojo.text.presentation.mvp.presenters;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.videonasocialmedia.transcoder.video.format.VideonaFormat;
import com.videonasocialmedia.videonamediaframework.model.media.effects.TextEffect;
import com.videonasocialmedia.videonamediaframework.pipeline.TranscoderHelperListener;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.domain.video.UpdateVideoRepositoryUseCase;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.export.domain.GetVideoFormatFromCurrentProjectUseCase;
import com.videonasocialmedia.vimojo.export.domain.RelaunchTranscoderTempBackgroundUseCase;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.videonamediaframework.model.media.Media;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnVideosRetrieved;
import com.videonasocialmedia.vimojo.text.domain.ModifyVideoTextAndPositionUseCase;
import com.videonasocialmedia.vimojo.text.presentation.mvp.views.EditTextView;
import com.videonasocialmedia.videonamediaframework.utils.TextToDrawable;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ruth on 1/09/16.
 */

public class EditTextPreviewPresenter implements OnVideosRetrieved, TranscoderHelperListener {

    private final String LOG_TAG = getClass().getSimpleName();

    private TextToDrawable drawableGenerator;

    private Video videoToEdit;

    private GetMediaListFromProjectUseCase getMediaListFromProjectUseCase;
    private ModifyVideoTextAndPositionUseCase modifyVideoTextAndPositionUseCase;
    private GetVideoFormatFromCurrentProjectUseCase getVideonaFormatFromCurrentProjectUseCase;
    private UpdateVideoRepositoryUseCase updateVideoRepositoryUseCase;
    private RelaunchTranscoderTempBackgroundUseCase relaunchTranscoderTempBackgroundUseCase;


    private EditTextView editTextView;
    private Context context;
    protected UserEventTracker userEventTracker;
    protected Project currentProject;


    @Inject
    public EditTextPreviewPresenter(EditTextView editTextView, Context context,
                                    UserEventTracker userEventTracker,
                                    GetMediaListFromProjectUseCase getMediaListFromProjectUseCase,
                                    ModifyVideoTextAndPositionUseCase
                                            modifyVideoTextAndPositionUseCase,
                                    GetVideoFormatFromCurrentProjectUseCase
                                            getVideonaFormatFromCurrentProjectUseCase,
                                    UpdateVideoRepositoryUseCase updateVideoRepositoryUseCase,
                                    RelaunchTranscoderTempBackgroundUseCase
                                            relaunchTranscoderTempBackgroundUseCase) {
        this.editTextView = editTextView;
        this.context = context;
        this.userEventTracker = userEventTracker;
        this.getMediaListFromProjectUseCase = getMediaListFromProjectUseCase;
        this.modifyVideoTextAndPositionUseCase = modifyVideoTextAndPositionUseCase;
        this.getVideonaFormatFromCurrentProjectUseCase = getVideonaFormatFromCurrentProjectUseCase;
        this.updateVideoRepositoryUseCase = updateVideoRepositoryUseCase;
        this.relaunchTranscoderTempBackgroundUseCase = relaunchTranscoderTempBackgroundUseCase;
        this.currentProject = loadCurrentProject();
    }

    private Project loadCurrentProject() {
        // TODO(jliarte): this should make use of a repository or use case to load the Project
        return Project.getInstance(null,null,null, null);
    }

    public void init(int videoToEditTextIndex) {
        List<Media> videoList = getMediaListFromProjectUseCase.getMediaListFromProject();
        if (videoList != null) {
            ArrayList<Video> v = new ArrayList<>();
            videoToEdit = (Video) videoList.get(videoToEditTextIndex);
            v.add(videoToEdit);
            onVideosRetrieved(v);
        }
    }

    @Override
    public void onVideosRetrieved(List<Video> videoList) {
        editTextView.showPreview(videoList);
    }

    @Override
    public void onNoVideosRetrieved() {
        editTextView.showError("No videos");
    }

    public void createDrawableWithText(String text, String position, int width, int height) {
        drawableGenerator = new TextToDrawable(context);
        Drawable drawable = drawableGenerator.createDrawableWithTextAndPosition(text, position,
            width, height);
        editTextView.showText(drawable);
    }

    public void setTextToVideo(String text, TextEffect.TextPosition textPositionSelected) {
        VideonaFormat videoFormat = currentProject.getVMComposition().getVideoFormat();
        // TODO:(alvaro.martinez) 22/02/17 This drawable saved in app or sdk?
        Drawable drawableFadeTransitionVideo =
            ContextCompat.getDrawable(context, R.drawable.alpha_transition_white);

        modifyVideoTextAndPositionUseCase.addTextToVideo(drawableFadeTransitionVideo, videoToEdit,
            videoFormat, text, textPositionSelected.name(),
            currentProject.getProjectPathIntermediateFileAudioFade(), this);

        userEventTracker.trackClipAddedText("center", text.length(), currentProject);
    }

    @Override
    public void onSuccessTranscoding(Video video) {
        Log.d(LOG_TAG, "onSuccessTranscoding " + video.getTempPath());
        updateVideoRepositoryUseCase.succesTranscodingVideo(video);
    }

    @Override
    public void onErrorTranscoding(Video video, String message) {
        Log.d(LOG_TAG, "onErrorTranscoding " + video.getTempPath() + " - " + message);
        if(video.getNumTriesToExportVideo() < Constants.MAX_NUM_TRIES_TO_EXPORT_VIDEO){
            videoToEdit.increaseNumTriesToExportVideo();
            Project currentProject = Project.getInstance(null, null, null, null);
            VideonaFormat videoFormat = getVideonaFormatFromCurrentProjectUseCase.getVideonaFormatFromCurrentProject();
            Drawable drawableFadeTransitionVideo = VimojoApplication.getAppContext()
                .getDrawable(R.drawable.alpha_transition_white);

            relaunchTranscoderTempBackgroundUseCase.relaunchExport(drawableFadeTransitionVideo,
                video, videoFormat, currentProject.getProjectPathIntermediateFileAudioFade(), this);
        } else {

            updateVideoRepositoryUseCase.errorTranscodingVideo(video,
                Constants.ERROR_TRANSCODING_TEMP_FILE_TYPE.TEXT.name());
        }
    }
}


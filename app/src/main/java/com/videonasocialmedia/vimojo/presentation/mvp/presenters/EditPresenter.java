/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas Abascal
 * Veronica Lago Fominaya
 */

package com.videonasocialmedia.vimojo.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;

import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.VimojoApplication;
import com.videonasocialmedia.vimojo.domain.editor.GetMediaListFromProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.GetMusicFromProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.RemoveVideoFromProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.ReorderMediaItemUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Media;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Music;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;

import com.videonasocialmedia.vimojo.presentation.mvp.views.EditorView;
import com.videonasocialmedia.vimojo.presentation.views.customviews.ToolbarNavigator;
import com.videonasocialmedia.vimojo.utils.ConfigPreferences;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import java.util.ArrayList;
import java.util.List;

public class EditPresenter implements OnAddMediaFinishedListener, OnRemoveMediaFinishedListener,
        OnVideosRetrieved, OnReorderMediaListener, GetMusicFromProjectCallback {

    /**
     * LOG_TAG
     */
    private final String LOG_TAG = getClass().getSimpleName();
    /**
     * UseCases
     */
    private RemoveVideoFromProjectUseCase remoVideoFromProjectUseCase;
    private ReorderMediaItemUseCase reorderMediaItemUseCase;
    private GetMediaListFromProjectUseCase getMediaListFromProjectUseCase;
    private ToolbarNavigator.ProjectModifiedCallBack projectModifiedCallBack;
    private GetMusicFromProjectUseCase getMusicFromProjectUseCase;
    /**
     * Editor View
     */
    private EditorView editorView;
    private List<Video> videoList;
    protected UserEventTracker userEventTracker;
    protected Project currentProject;

    public EditPresenter(EditorView editorView,
                         ToolbarNavigator.ProjectModifiedCallBack projectModifiedCallBack,
                         UserEventTracker userEventTracker) {
        this.editorView = editorView;
        this.projectModifiedCallBack = projectModifiedCallBack;

        getMediaListFromProjectUseCase = new GetMediaListFromProjectUseCase();
        remoVideoFromProjectUseCase = new RemoveVideoFromProjectUseCase();
        reorderMediaItemUseCase = new ReorderMediaItemUseCase();
        getMusicFromProjectUseCase = new GetMusicFromProjectUseCase();
        this.userEventTracker = userEventTracker;
        this.currentProject = loadCurrentProject();
    }

    public Project loadCurrentProject() {
        // TODO(jliarte): this should make use of a repository or use case to load the Project
        return Project.getInstance(null, null, null);
    }


    public String getResolution() {
        SharedPreferences sharedPreferences = VimojoApplication.getAppContext().getSharedPreferences(
                ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);

        return sharedPreferences.getString(ConfigPreferences.RESOLUTION, "1280x720");
    }



    public void moveItem(int fromPosition, int toPositon) {
        reorderMediaItemUseCase.moveMediaItem(videoList.get(fromPosition), toPositon, this);
    }

    @Override
    public void onAddMediaItemToTrackError() {
        //TODO modify error message
        editorView.showError(R.string.addMediaItemToTrackError);
    }

    @Override
    public void onAddMediaItemToTrackSuccess(Media media) {

    }

    @Override
    public void onRemoveMediaItemFromTrackError() {
        //TODO modify error message
        editorView.showError(R.string.addMediaItemToTrackError);
    }

    @Override
    public void onRemoveMediaItemFromTrackSuccess() {
        editorView.updateProject();
        projectModifiedCallBack.onProjectModified();

    }

    @Override
    public void onVideosRetrieved(List<Video> videoList) {
        this.videoList = videoList;
        List<Video> videoCopy = new ArrayList<>(videoList);
        editorView.enableEditActions();
        //videonaPlayerView.bindVideoList(videoList);
        editorView.bindVideoList(videoCopy);
        projectModifiedCallBack.onProjectModified();
    }

    @Override
    public void onNoVideosRetrieved() {
        editorView.disableEditActions();
        editorView.hideProgressDialog();
        editorView.showMessage(R.string.add_videos_to_project);
        editorView.expandFabMenu();
        editorView.resetPreview();
        projectModifiedCallBack.onProjectModified();
    }

    public void removeVideoFromProject(int selectedVideoRemove) {
        Video videoToRemove = this.videoList.get(selectedVideoRemove);
        ArrayList<Media> mediaToDeleteFromProject = new ArrayList<>();
        mediaToDeleteFromProject.add(videoToRemove);
        remoVideoFromProjectUseCase.removeMediaItemsFromProject(mediaToDeleteFromProject, this);
    }

    @Override
    public void onMediaReordered(Media media, int newPosition) {
        //If everything was right the UI is already updated since the user did the reordering
        userEventTracker.trackClipsReordered(currentProject);
        // (jliarte): 24/08/16 probando fix del reorder. Si actualizamos el proyecto al
        //          reordenar, como se reordena en cada cambio de celda, no sólo al final,
        //          generamos overhead innecesario en la actividad y además de esto, se para el
        //          preview y se corta el movimiento que estemos haciendo de reordenado
//        editorView.updateProject();
    }

    @Override
    public void onErrorReorderingMedia() {
        //The reordering went wrong so we ask the project for the actual video list
        obtainVideos();
    }

    public void obtainVideos() {
        getMediaListFromProjectUseCase.getMediaListFromProject(this);
    }

    public void loadProject() {
        getMediaListFromProjectUseCase.getMediaListFromProject(this);
        if(currentProject.hasMusic())
            getMusicFromProjectUseCase.getMusicFromProject(this);
    }

    @Override
    public void onMusicRetrieved(Music music) {
        editorView.setMusic(music);
    }

}
